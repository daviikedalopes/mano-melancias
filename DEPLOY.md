# Deploy — Mano Melancias

Backend e frontend na Railway (dois serviços no mesmo projeto), banco no Supabase.

## 1. Banco de dados (Supabase)

1. Crie um projeto no [Supabase](https://supabase.com) (escolha a região mais próxima, ex. São Paulo).
2. Anote a senha do banco definida na criação do projeto.
3. No painel do projeto, vá em **Project Settings → Database → Connection string** e use a opção de **conexão direta** (porta `5432`), não o pooler de "Transaction mode" (porta `6543`) — o backend já mantém seu próprio pool de conexões (HikariCP) e misturar os dois causa erro de prepared statement.
4. A URL fica no formato:
   ```
   jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require
   ```
5. Não é preciso criar tabelas manualmente — o Flyway aplica as migrações (V1 a V8) sozinho no primeiro boot do backend.

**Atenção (plano gratuito):** o projeto pausa automaticamente depois de ~1 semana sem acesso. Para reativar: painel do Supabase → o projeto aparece como "Paused" → botão **Restore project**. O sistema volta a funcionar poucos minutos depois.

## 2. Backend (Railway)

1. Crie um projeto na [Railway](https://railway.app) e adicione um serviço a partir do repositório GitHub, apontando para a pasta `api/` (ela tem um `Dockerfile` — a Railway detecta e usa ele automaticamente).
2. Configure as variáveis de ambiente do serviço:

   | Variável | Valor |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DATABASE_URL` | a connection string direta do Supabase (passo 1.4) |
   | `DATABASE_USERNAME` | `postgres` |
   | `DATABASE_PASSWORD` | a senha do banco definida no Supabase |
   | `JWT_SECRET` | um valor novo e forte — gere com `openssl rand -base64 48`. **Não reaproveite** o segredo de desenvolvimento que existe no `application.properties`. |
   | `CORS_ALLOWED_ORIGINS` | por enquanto, qualquer placeholder (ex. `http://localhost`) — volte aqui depois do passo 3 |

3. Faça o deploy e anote a URL pública que a Railway atribui ao serviço (ex. `https://api-mano-melancias.up.railway.app`).
4. Teste rapidamente: `curl https://<url-do-backend>/auth/login` deve responder (mesmo que com erro de validação, confirma que subiu).

## 3. Frontend (Railway)

1. No mesmo projeto Railway, adicione outro serviço apontando para a pasta `frontend/` (também tem `Dockerfile`, nginx servindo os arquivos estáticos).
2. **Antes de fazer o deploy**, atualize no código a URL do backend (feito no passo 2.3):
   - `frontend/assets/js/config.js` → `API_BASE_URL`.
   - O `connect-src` da tag `<meta http-equiv="Content-Security-Policy">` em todas as páginas HTML de `frontend/` (são 11 arquivos).
   
   Commite essa alteração antes do deploy do frontend.
3. Faça o deploy e anote a URL pública do frontend (ex. `https://app-mano-melancias.up.railway.app`).

## 4. Fechar o CORS

Volte nas variáveis de ambiente do serviço de **backend** na Railway e atualize `CORS_ALLOWED_ORIGINS` com a URL real do frontend (passo 3.3). A Railway reinicia o serviço automaticamente.

## 5. Conferir que subiu certo

- Acesse a URL do frontend no navegador, faça login com um usuário existente.
- No painel do Supabase (**Table Editor**), confirme que as tabelas (`cliente`, `produtor`, `usuario`, `venda`) existem — motorista e veículo não são mais tabelas próprias, só colunas dentro de `venda`.
- Abra o console do navegador (F12) e confirme que não há nenhum erro de CORS ou de CSP ("Refused to...") ao navegar pelo sistema.
