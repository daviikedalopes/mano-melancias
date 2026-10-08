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

## Resumo do passo a passo (para consulta rápida)

No servidor, como `root`:

```bash
git clone https://github.com/daviikedalopes/mano-melancias.git
cd mano-melancias/deploy
bash setup-servidor.sh                 # Docker, swap e firewall
bash gerar-env.sh SEU_DOMINIO          # cria o .env com segredos aleatórios
docker compose up -d --build           # sobe tudo (alguns minutos na 1ª vez)
bash criar-admin.sh                    # cria o primeiro administrador
```

Depois: agendar o backup (seção 5) e conferir (seção 7). Os detalhes de cada etapa estão abaixo.

## Como funciona o banco de dados e as migrações

- **Você não cria tabelas.** O container `db` cria um banco **vazio** (`manomelancias`) na primeira vez que sobe.
- Quando o backend inicia, o **Flyway** olha a pasta `api/src/main/resources/db/migration/` (arquivos `V1` a `V4`, um por tabela) e roda, em ordem, os que ainda não foram aplicados. Ele anota o que já rodou na tabela `flyway_schema_history`. No primeiro boot roda tudo; nos próximos, só as migrações **novas**.
- Para mudar o banco no futuro, crie um arquivo novo (`V5__descricao.sql`), faça `git pull` no servidor e `docker compose up -d --build`. **Nunca edite** uma migração que já foi aplicada.
- O banco do seu computador **não vai junto**: são bancos separados. O do servidor nasce vazio e sem usuários (por isso o `criar-admin.sh`). Para levar dados locais, veja "Levar o banco local".
- Os dados ficam no volume Docker `pgdata`: sobrevivem a reinício, a `docker compose down` e a atualizações. Só somem com `docker compose down -v`, **nunca use `-v` em produção**.

## 1. Servidor (VPS Hostinger)

- Hostinger: plano **KVM 1** (4 GB) ou maior, data center **São Paulo**, modelo de sistema **Docker** (Ubuntu 24.04 LTS já com Docker instalado). Confira o preço de **renovação** no checkout.
- Acesse por SSH: `ssh root@IP_DO_SERVIDOR`.
- Rode `bash setup-servidor.sh` (depois do `git clone`, seção 3). Ele confere o Docker, cria 2 GB de swap se não houver e liga o firewall liberando só `22`, `80` e `443`.
- Se o painel da Hostinger tiver um firewall próprio (hPanel → VPS → Firewall), confirme que ele também libera `80` e `443` (TCP). Sem isso o certificado HTTPS não é emitido.

## 2. Domínio

Registre um domínio (`.com.br` custa cerca de R$ 40/ano) e crie um registro **A** apontando para o IP do servidor. A propagação pode levar de minutos a horas.

Para testar sem domínio, use `IP-COM-TRACOS.sslip.io` (ex.: `203-0-113-5.sslip.io` para o IP `203.0.113.5`): o Caddy emite o certificado normalmente.

## 3. Subir o sistema

```bash
git clone https://github.com/daviikedalopes/mano-melancias.git
cd mano-melancias/deploy
bash setup-servidor.sh
bash gerar-env.sh SEU_DOMINIO
docker compose up -d --build
docker compose logs -f api     # espere "Started ApiApplication"; Ctrl+C sai dos logs
```

O `gerar-env.sh` cria o `.env` com a senha do banco e o segredo do login **aleatórios** (e não sobrescreve um `.env` existente). O `.env` fica só no servidor e nunca vai para o git. O primeiro boot demora alguns minutos (build do Maven).

## 4. Primeiro administrador

```bash
bash criar-admin.sh
```

Pergunta nome, e-mail e senha (8+ caracteres, com maiúscula, número e caractere especial), gera o hash BCrypt num container descartável e insere o usuário no banco. Depois entre no sistema e crie os demais usuários pela tela **Usuários**.

## 5. Backup (não pule)

```bash
chmod +x backup.sh
crontab -e   # adicione:  0 3 * * * /root/mano-melancias/deploy/backup.sh >> /var/log/mano-backup.log 2>&1
```

O script grava um `.sql.gz` por dia em `deploy/backups/` (guarda 14 dias). Para ter uma cópia **fora do servidor**, configure o [rclone](https://rclone.org) com um bucket gratuito (Cloudflare R2 ou Backblaze B2) e defina `BACKUP_RCLONE_REMOTE` no `.env`.

Restaurar (num banco vazio): o comando está no cabeçalho do `backup.sh`. Já testado: um dump restaurado num banco descartável voltou com os mesmos usuários e as 4 migrações.

## 6. Atualizar o sistema

```bash
cd mano-melancias && git pull && cd deploy && docker compose up -d --build
```

Migrações novas rodam sozinhas no boot.

## 7. Conferir

- `https://SEU_DOMINIO` abre a tela de login; entre com o admin.
- `curl -i https://SEU_DOMINIO/api/auth/login` responde 4xx do backend (não 404 do Caddy).
- A porta 5432 do IP público **não** pode responder.
- Reinicie o servidor (`reboot`) e confirme que os containers voltam sozinhos e os dados continuam lá.
- Console do navegador (F12) sem erros de CSP ou rede.

Dica: cadastre o domínio num monitor gratuito (UptimeRobot) para ser avisado se cair.

## Problemas comuns

| Sintoma | Causa provável |
|---|---|
| Navegador avisa "conexão não segura" / Caddy não emite certificado | Domínio ainda não aponta para o IP, ou portas 80/443 bloqueadas (ufw ou firewall do painel). Veja `docker compose logs caddy` |
| `502 Bad Gateway` logo após subir | A API ainda está iniciando (1–2 min). Veja `docker compose logs -f api` |
| API reinicia em loop com `password authentication failed` | O `.env` foi trocado depois do banco ser criado. Restaure o `.env` antigo, ou, só se não houver dados, `docker compose down -v` e suba de novo |
| `Could not resolve placeholder 'JWT_SECRET'` | `.env` ausente ou vazio: rode `bash gerar-env.sh SEU_DOMINIO` |

## Levar o banco local (opcional)

Só se você quiser os dados que já tem no seu computador. Leve **só os dados**: o servidor cria as tabelas sozinho (Flyway) e o histórico do Flyway do seu banco local não deve ir junto.

```bash
# 1. No seu computador (gera o arquivo; ajuste usuário/banco se necessário):
pg_dump -U manomelancias_user -h localhost --data-only --no-owner --exclude-table=flyway_schema_history manomelancias > dados.sql

# 2. Envie dados.sql para o servidor (scp). Lá, suba o sistema normalmente
#    (docker compose up -d --build) e espere a API iniciar: ela cria as tabelas.

# 3. Importe os dados (com as tabelas vazias):
docker compose exec -T db psql -U manomelancias_user manomelancias < dados.sql
```

Use isso só com dados que podem ir para produção. Depois de importar, crie o administrador com `bash criar-admin.sh` apenas se o seu usuário local não tiver vindo junto.
