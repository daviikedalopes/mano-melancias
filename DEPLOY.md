# Deploy — Mano Melancias (VPS + Docker)

Tudo roda num único servidor (VPS, ex.: Hostinger) com Docker Compose, na pasta [`deploy/`](deploy/):

```
Internet ──443──► caddy ──/api/*──► api (Spring Boot) ──► db (PostgreSQL)
                    └── demais caminhos: arquivos do frontend/
```

- **caddy**: HTTPS automático (Let's Encrypt), serve o frontend e repassa `/api/*` ao backend (removendo o prefixo `/api`).
- **api**: construído a partir do [`api/Dockerfile`](api/Dockerfile), com a memória da JVM limitada.
- **db**: PostgreSQL com volume persistente, **sem porta exposta** na internet.

Frontend e API ficam no mesmo domínio, então **não há CORS** nem URL de backend para configurar: o `config.js` usa `/api` em qualquer domínio que não seja `localhost`.

## Como funciona o banco de dados e as migrações

- **Você não precisa criar tabelas.** O container `db` cria um banco **vazio** (`manomelancias`) na primeira vez que sobe.
- Quando o backend inicia, o **Flyway** olha a pasta `api/src/main/resources/db/migration/` (arquivos `V1`, `V2`, … `V10`) e roda, em ordem, os que ainda não foram aplicados. Ele anota o que já rodou numa tabela própria (`flyway_schema_history`). No primeiro boot roda tudo (V1 a V10); nos próximos, só as migrações **novas**.
- Para mudar o banco no futuro, crie um arquivo novo (`V11__descricao.sql`), faça `git pull` no servidor e `docker compose up -d --build`: a migração roda sozinha. **Nunca edite** uma migração que já foi aplicada.
- O banco do seu computador **não vai junto** — são bancos separados. O do servidor nasce vazio e sem usuários (veja o passo "Primeiro administrador"). Se quiser levar os dados que já tem localmente, veja "Levar o banco local" mais abaixo.
- Os dados ficam no volume Docker `pgdata`: sobrevivem a reinício, a `docker compose down` e a atualizações. Só somem com `docker compose down -v`, **nunca use `-v` em produção**.

## 1. Servidor (Hostinger ou outro)

- Plano de VPS com **2 GB de RAM ou mais**, em data center no **Brasil** se disponível (confira no checkout). Sistema: **Ubuntu 24.04 com Docker** (a Hostinger oferece esse modelo ao criar o VPS) ou Ubuntu LTS puro.
- Acesse por SSH (`ssh root@IP_DO_SERVIDOR`), de preferência com chave SSH.
- Firewall liberando só `22`, `80` e `443`:
  ```bash
  ufw allow 22/tcp && ufw allow 80/tcp && ufw allow 443/tcp && ufw allow 443/udp && ufw enable
  ```
- Se tiver 2 GB, crie swap (o build do Maven usa bastante memória):
  ```bash
  fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
  ```
- Se o Docker não veio instalado: `curl -fsSL https://get.docker.com | sh`.

## 2. Domínio

Registre um domínio (`.com.br` custa cerca de R$ 40/ano) e crie um registro **A** apontando para o IP do servidor. Para testar sem domínio, use `IP-COM-TRACOS.sslip.io` (ex.: `203-0-113-5.sslip.io`): o Caddy emite o certificado normalmente.

## 3. Subir o sistema

```bash
git clone https://github.com/daviikedalopes/mano-melancias.git
cd mano-melancias/deploy
cp .env.example .env
nano .env        # preencha DOMINIO, POSTGRES_PASSWORD e JWT_SECRET
docker compose up -d --build
docker compose logs -f api     # espere "Started ApiApplication"; Ctrl+C sai dos logs
```

Gere os segredos com `openssl rand -hex 24` (senha do banco) e `openssl rand -hex 48` (`JWT_SECRET`). O `.env` fica só no servidor e nunca vai para o git. O primeiro boot demora alguns minutos (build do Maven).

## 4. Primeiro administrador

O banco começa sem usuários e só um admin logado cria outros, então o primeiro entra direto no banco. Gere o hash BCrypt da senha (precisa de 8+ caracteres, com maiúscula, número e caractere especial):

```bash
apt install -y apache2-utils
htpasswd -bnBC 10 "" 'SUA_SENHA' | tr -d ':\n'
```

Copie o resultado (começa com `$2y$`, que o Spring aceita) e rode, trocando e-mail e hash:

```bash
docker compose exec db psql -U manomelancias_user manomelancias -c \
"INSERT INTO usuario (id, nome, email, senha_hash, papel, ativo)
 VALUES (gen_random_uuid(), 'Administrador', 'seu@email.com.br', 'COLE_O_HASH_AQUI', 'ADMIN', TRUE);"
```

Depois entre no sistema e crie os demais usuários pela tela **Usuários**.

## 5. Backup (não pule)

```bash
chmod +x backup.sh
crontab -e   # adicione:  0 3 * * * /root/mano-melancias/deploy/backup.sh >> /var/log/mano-backup.log 2>&1
```

O script grava um `.sql.gz` por dia em `deploy/backups/` (guarda 14 dias). Para ter uma cópia **fora do servidor**, configure o [rclone](https://rclone.org) com um bucket gratuito (Cloudflare R2 ou Backblaze B2) e defina `BACKUP_RCLONE_REMOTE` no `.env`. **Teste um restore ao menos uma vez** (comando no cabeçalho do `backup.sh`).

## 6. Atualizar o sistema

```bash
cd mano-melancias && git pull && cd deploy && docker compose up -d --build
```

Migrações novas rodam sozinhas no boot.

## 7. Conferir

- `https://SEU_DOMINIO` abre a tela de login; entre com o admin.
- `curl -i https://SEU_DOMINIO/api/auth/login` responde 4xx do backend (não 404 do Caddy).
- A porta 5432 do IP público **não** pode responder.
- Reinicie o servidor e confirme que os containers voltam sozinhos e os dados continuam lá.
- Console do navegador (F12) sem erros de CSP ou rede.

Dica: cadastre o domínio num monitor gratuito (UptimeRobot) para ser avisado se cair.

## Levar o banco local (opcional)

Só se você quiser os dados que já tem no seu computador. Faça **antes** do primeiro `docker compose up` da API (ou com a API parada), com o banco do servidor ainda vazio:

```bash
# No seu computador (gera o arquivo; ajuste usuário/banco se necessário):
pg_dump -U manomelancias_user -h localhost --no-owner manomelancias > meu-banco.sql

# Envie para o servidor (scp) e, lá, suba só o banco e importe:
docker compose up -d db
docker compose exec -T db psql -U manomelancias_user manomelancias < meu-banco.sql
docker compose up -d --build
```

O dump inclui a tabela de controle do Flyway, então o backend entende que as migrações já foram aplicadas e segue de onde parou. Use isso só com dados que podem ir para produção.
