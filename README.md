# Mano Melancias — Sistema de Gestão de Vendas

Sistema interno para uma empresa que compra melancias de produtores rurais e revende a clientes (mercados, feiras, atacadistas), controlando pesagem, precificação, frete, contas a receber e emissão de PDFs.

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Domínio e regras de negócio](#domínio-e-regras-de-negócio)
- [Relatórios](#relatórios)
- [Segurança](#segurança)
- [Estrutura de pacotes (backend)](#estrutura-de-pacotes-backend)
- [Frontend](#frontend)
- [Banco de dados](#banco-de-dados)
- [Casos de uso principais](#casos-de-uso-principais)
- [Limitações conhecidas](#limitações-conhecidas)
- [Como rodar localmente](#como-rodar-localmente)
- [Deploy](#deploy)

## Visão geral

Dois papéis de usuário:

- **ADMIN** — acesso completo: tudo que o operador faz, mais gestão de usuários e exclusão definitiva de cadastros.
- **OPERADOR** — lança e gerencia vendas e cadastros do dia a dia (clientes, produtores), mas não gerencia usuários nem exclui nada definitivamente.

O fluxo central do sistema é a **venda**: um carregamento de melancia comprado de um produtor, transportado por um motorista num veículo, entregue e faturado a um cliente. Motorista e veículo não são cadastros — são só campos preenchidos na própria venda (ver [Venda](#venda--o-núcleo-do-sistema)).

## Arquitetura

| Camada | Tecnologia |
|---|---|
| Backend | Java 25, Spring Boot 4.1 (Web, Data JPA, Security, Validation), Maven |
| Autenticação | JWT assinado com HMAC, stateless (sem sessão no servidor) |
| Banco de dados | PostgreSQL, migrações versionadas com Flyway |
| Geração de PDF | OpenPDF |
| Frontend | HTML + CSS + JavaScript puro, sem framework e sem build step |
| Hospedagem prevista | VPS com Docker Compose (PostgreSQL + API + Caddy) — ver [DEPLOY.md](DEPLOY.md) |

O frontend é uma página HTML por tela (sem roteamento client-side); cada uma carrega seu próprio script e alguns módulos JS compartilhados. Toda comunicação com o backend é via `fetch`, enviando o JWT no header `Authorization: Bearer <token>`.

## Domínio e regras de negócio

Duas entidades de cadastro (**Cliente**, **Produtor**) seguem o mesmo padrão:

- Podem ser criadas diretamente pela tela de cadastro, ou **"on the fly"** direto do formulário de venda — o operador digita os dados de quem ainda não existe e o sistema cria o registro na hora (`buscarOuCriar`, casado por um campo natural: nome+município+estado para cliente, nome+cidade para produtor).
- Têm um campo `ativo` (soft delete): **inativar** (`DELETE /{recurso}/{id}`) tira o registro das buscas padrão e do seletor de vendas, sem apagar o histórico; **reativar** desfaz. Qualquer usuário autenticado pode inativar/reativar.
- Têm **exclusão definitiva** (`DELETE /{recurso}/{id}/excluir`), **restrita a ADMIN**. Se o registro tiver vendas vinculadas, a exclusão falha (409) por violação de chave estrangeira — o sistema não deixa apagar algo referenciado em uma venda.

### Cliente

Campos: nome, município, estado (UF, 2 letras), telefone (opcional). Validação: nome/município até 255 caracteres, telefone até 20.

### Produtor

Campos: nome, cidade, telefone (opcional). Mesmos limites de tamanho do Cliente.

### Motorista e veículo não são cadastros

Diferente de cliente e produtor, motorista e veículo **não têm tela própria nem histórico independente** — são só campos preenchidos em cada venda (sem busca, sem id, sem soft-delete). Isso foi uma decisão deliberada: cadastrar motorista/veículo antes de cada venda dava mais trabalho do que valia, já que a empresa lida com motoristas avulsos/terceirizados com frequência. Ver as regras exatas em [Venda](#venda--o-núcleo-do-sistema).

### Usuário

- Papéis: `ADMIN`, `OPERADOR`.
- Login por e-mail + senha (hash BCrypt), retorna um JWT válido por `app.jwt.expiration-minutes` (padrão 480 min = 8h).
- **Senha**: mínimo 8 caracteres, 1 letra maiúscula, 1 número, 1 caractere especial (validador `SenhaForte`); máximo 72 caracteres (limite de que o BCrypt trunca silenciosamente bytes além disso).
- **E-mail**: além do formato (exige domínio com TLD), o backend confirma que o **domínio existe de verdade** consultando o DNS (registro MX, ou A/AAAA como fallback) — validador `ValidEmail`. Rejeita algo como `admin@empresa` (sem TLD) e também domínios inventados.
- **Confirmação de e-mail**: validar o domínio não prova que a caixa existe. Por isso todo usuário novo (operador **ou administrador**) nasce com `email_confirmado = false`, recebe por e-mail um link (token assinado, válido por 48 h, com chave derivada diferente da do login) e só consegue entrar depois de clicar nele. Se o envio falhar, a criação do usuário é desfeita (503). O administrador pode reenviar o link pela tela de Usuários; quem não recebeu pode pedir de novo na tela de login. Exige um servidor SMTP (`MAIL_*`, ver [DEPLOY.md](DEPLOY.md)); sem `MAIL_HOST` o e-mail não sai e o link vai para o log da API (útil só em desenvolvimento).
- **Rate limiting de login**: 5 tentativas com senha errada para o mesmo e-mail bloqueiam esse e-mail por 15 minutos (`LoginRateLimiter`, contador em memória).
- **Contas ADMIN nunca podem ser inativadas ou excluídas** — só contas `OPERADOR` podem (regra em `UsuarioService.validarOperador`). Isso evita que alguém remova o próprio acesso de administrador do sistema.
- Usuário inativo **não consegue mais logar** (checagem em `autenticar`, depois de validar a senha).
- **Revogação imediata de acesso**: diferente do padrão comum de JWT, o filtro de autenticação (`JwtAuthenticationFilter`) reconsulta o usuário no banco a cada requisição em vez de confiar cegamente nas claims do token. Por isso, inativar/excluir um usuário — ou mudar o papel dele — tem efeito imediato, sem esperar o token expirar.

### Venda — o núcleo do sistema

Uma venda liga cliente, produtor, motorista e veículo, e registra pesagem, preço, frete e pagamento.

**Campos:** data, cliente, produtor, motorista, veículo, peso bruto, desconto de tara, desconto de palha, total de frutas, preço/kg, tipo de frete (`NEGOCIADO` ou `POR_KG`), preço do frete/kg ou valor de frete negociado, vencimento, NF, status de pagamento (`PENDENTE`, `PAGO_PARCIAL`, `PAGO`), observações.

**Motorista e veículo** são só campos de texto da própria venda, preenchidos na hora, sem cadastro prévio:
- Nome do motorista: obrigatório.
- CPF do motorista: **opcional**, mas se for informado precisa ser um CPF de verdade (dígito verificador validado) — não passa qualquer sequência de números.
- Placa e cidade do veículo: **ambos obrigatórios**.
- Nada disso tem unicidade nem histórico próprio: o mesmo motorista/placa aparece livremente em quantas vendas quiser, já que não é mais um cadastro com id.

**Cálculos** (feitos no backend, `VendaService`, cobertos por testes com números reais conferidos numa ficha de venda em papel):

| Cálculo | Fórmula |
|---|---|
| Peso líquido | peso bruto − desconto de tara − desconto de palha |
| Valor da mercadoria | peso líquido × preço/kg |
| Valor do frete | peso líquido × preço do frete/kg (se `POR_KG`) **ou** o valor negociado informado (se `NEGOCIADO`) |
| Restante a pagar | valor da mercadoria − valor do frete |
| Média de peso por fruta | peso líquido ÷ total de frutas |

Exemplo conferido: peso bruto 31.180 kg, tara 11.300 kg, palha 400 kg → líquido **19.480 kg**; preço R$ 1,45/kg → mercadoria **R$ 28.246,00**; frete R$ 8.350,00 → restante **R$ 19.896,00**; 1.280 frutas → média **15,22 kg/fruta**.

**Numeração:** cada venda recebe um número sequencial único (`numero`). Ao excluir uma venda, o número dela fica livre, e a **próxima venda criada reaproveita a menor lacuna livre** — excluir a venda nº 2 faz a próxima nascer com número 2, não pular para o topo da sequência. Sem lacunas, continua normalmente do maior número + 1.

**Exclusão:** hard delete de verdade (sem soft-delete) — qualquer usuário autenticado pode excluir uma venda; hoje não existe checagem de "dono da venda" (ver [Limitações conhecidas](#limitações-conhecidas)).

**PDF:** dois tipos, gerados em `PdfService` — o recibo individual de uma venda, e o relatório de vendas por período (abaixo).

## Relatórios

- **Vendas por período**: total de vendas, valor de mercadoria, frete e restante a pagar num intervalo de datas, com filtro opcional por produtor e/ou cliente — filtrando os dois ao mesmo tempo, mostra só as vendas em comum entre eles. Pode ser exportado em PDF, com o resumo e a lista das vendas do período.
- **Ranking por produtor / por cliente**: totais agregados de todo o histórico (sem filtro de período), ordenados pelo valor comprado.
- **Contas a receber**: vendas com saldo pendente (restante > 0 e status ≠ `PAGO`), ordenadas por vencimento (sem vencimento por último).

## Segurança

- **Autenticação**: JWT assinado com HMAC (`app.jwt.secret`, sobrescrevível pela variável de ambiente `JWT_SECRET` em produção — nunca deve usar o valor padrão de desenvolvimento fora do ambiente local).
- **Autorização**: `/usuarios/**` e as rotas `.../excluir` de cliente/produtor exigem papel `ADMIN`; o restante exige apenas estar autenticado.
- **CORS**: restrito às origens listadas em `app.cors.allowed-origins` (sobrescrevível por `CORS_ALLOWED_ORIGINS`).
- **Headers HTTP**: `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy` e `HSTS` configurados explicitamente no backend; `Content-Security-Policy` aplicada via `<meta>` em cada página do frontend, restrita a `'self'` (as fontes do site são hospedadas localmente, não vêm do Google Fonts).
- **Validação de entrada**: Bean Validation em todos os DTOs de request, com limites de tamanho alinhados às colunas do banco (evita tanto erro feio de truncamento quanto payloads gigantes).
- **Segredos**: `application*.properties` com credenciais reais não são versionados; em produção, banco e segredo JWT vêm de variáveis de ambiente.

## Estrutura de pacotes (backend)

```
com.manomelancias.api/
├── cliente/     Cliente, ClienteController, ClienteService, ClienteRepository, dto/
├── produtor/    (mesmo padrão do cliente)
├── usuario/     Usuario, Papel, UsuarioController, UsuarioService, UsuarioRepository
│                dto/ LoginRequest, LoginResponse, UsuarioRequest, UsuarioResponse
├── venda/       Venda (inclui motorista/veículo como campos), TipoFrete, StatusPagamento,
│                VendaController, VendaService, VendaRepository
│                dto/ VendaRequestDTO, VendaResponseDTO, VendaFiltroDTO
├── relatorio/   RelatorioController, RelatorioService
│                dto/ ClienteRelatorioDTO, ProdutorRelatorioDTO, ContaReceberDTO, VendasPeriodoResponseDTO
└── shared/
    ├── config/      SecurityConfig
    ├── security/    JwtService, JwtAuthenticationFilter, LoginRateLimiter
    ├── validation/  ValidEmail + EmailDomainValidator, SenhaForte + PasswordStrengthValidator, CpfValidator
    ├── exception/   BusinessException, ResourceNotFoundException, GlobalExceptionHandler
    └── pdf/         PdfService
```

Padrão repetido nas duas entidades de cadastro: **Controller** (REST) → **Service** (regra de negócio) → **Repository** (Spring Data JPA) → **Entity**. DTOs de request/response separam o que a API aceita e expõe do modelo interno.

### Endpoints

| Recurso | Rotas |
|---|---|
| Cliente | `GET/POST /clientes`, `GET/PUT /clientes/{id}`, `DELETE /clientes/{id}` (inativar), `POST /clientes/{id}/reativar`, `DELETE /clientes/{id}/excluir` (admin) |
| Produtor | mesmo padrão em `/produtores` |
| Usuário | `POST /auth/login`, `POST/GET /usuarios` (admin), `DELETE /usuarios/{id}` (excluir, admin), `POST /usuarios/{id}/inativar`, `POST /usuarios/{id}/reativar` |
| Venda | `GET/POST /vendas`, `GET/PUT /vendas/{id}`, `DELETE /vendas/{id}`, `GET /vendas/{id}/pdf` |
| Relatórios | `GET /relatorios/vendas-periodo`, `GET /relatorios/vendas-periodo/pdf`, `GET /relatorios/por-produtor`, `GET /relatorios/por-cliente`, `GET /relatorios/contas-a-receber` |

## Frontend

Cada tela é um `.html` independente, com seu próprio script em `assets/js/pages/`. Módulos compartilhados:

| Módulo | Responsabilidade |
|---|---|
| `auth.js` | Guarda o token JWT, decodifica claims, checagem de sessão/papel |
| `api.js` | Wrapper de `fetch`, injeta o header de autenticação, trata erros da API |
| `format.js` | Máscaras (CPF, telefone, placa), formatação de moeda/data no padrão BR |
| `toast.js` | Notificações de sucesso/erro |
| `confirm-modal.js` | Modal de confirmação do próprio sistema (não usa `confirm()` nativo do navegador) |
| `entity-picker.js` | Busca/seleciona/cria cliente ou produtor dentro do formulário de venda |
| `shell.js` | Sidebar, boot de autenticação, controle de acesso das páginas admin-only |

Páginas: `login`, `index` (painel), `vendas`, `venda-form`, `venda-detalhe`, `clientes`, `produtores`, `usuarios` (admin only), `relatorios`. Motorista e veículo não têm tela de cadastro — só aparecem como campos dentro de `venda-form`.

## Banco de dados

PostgreSQL, com 5 migrações Flyway (`api/src/main/resources/db/migration`):

| Migração | Tabela |
|---|---|
| V1 | `cliente` |
| V2 | `produtor` |
| V3 | `usuario` |
| V4 | `venda` — inclui direto os dados de motorista (`motorista_nome`, `motorista_cpf`) e veículo (`veiculo_placa`, `veiculo_cidade`), que não são cadastros próprios |
| V5 | `usuario.email_confirmado` — confirmação de e-mail no cadastro (usuários que já existiam ficam confirmados) |

Para mudar o banco no futuro, crie um arquivo novo (`V6__descricao.sql`); nunca edite uma migração já aplicada.

**Banco que já existia antes da simplificação das migrações** (ex.: o banco local de desenvolvimento, criado quando eram 10 migrações): o schema é idêntico, mas o histórico do Flyway não bate mais. Alinhe uma única vez, sem perder dados:

```sql
DROP TABLE flyway_schema_history;
```

e suba a API uma vez com `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` e `SPRING_FLYWAY_BASELINE_VERSION=4` (variáveis de ambiente). O Flyway registra o banco como já estando na V4 e roda só a V5 (que adiciona `email_confirmado`, mantendo os usuários atuais como confirmados); depois pode remover as variáveis.

## Casos de uso principais

1. **Login** — e-mail + senha → JWT. Bloqueio após 5 tentativas erradas seguidas.
2. **Cadastro de cliente/produtor** — direto na tela do recurso, ou on-the-fly durante o lançamento de uma venda.
3. **Lançamento de venda** — escolhe/cadastra cliente e produtor, digita motorista e veículo na hora, informa pesagem/preço/frete, o sistema calcula os valores.
4. **Edição de venda** — recalcula os valores a partir dos novos dados.
5. **Baixa de pagamento** — muda o status (`PENDENTE` → `PAGO_PARCIAL` → `PAGO`).
6. **Geração de PDF** — recibo de uma venda, ou relatório de vendas por período.
7. **Inativação/reativação/exclusão de cadastro** — qualquer usuário inativa/reativa; só admin exclui de vez.
8. **Gestão de usuários** (admin) — criar, inativar/reativar/excluir operadores; contas admin são protegidas.
9. **Consulta de relatórios** — período (com filtro por produtor/cliente), rankings, contas a receber.

## Limitações conhecidas

Documentadas de propósito, para não passar a impressão de que o sistema cobre tudo:

- Qualquer usuário autenticado pode editar ou excluir vendas lançadas por outro usuário — não há checagem de "dono do registro".
- O CPF do motorista fica em texto puro no banco (sem criptografia em repouso), agora como campo da própria venda.
- Motorista e veículo, por não serem mais cadastros, não têm soft-delete/histórico próprio nem impedem nomes/placas duplicados ou digitados de forma inconsistente entre vendas diferentes (ex.: "Tiago" numa venda e "Tiago Souza" noutra não são reconhecidos como a mesma pessoa).
- O reaproveitamento do número de venda tem uma janela de corrida rara: duas vendas salvas no exato mesmo instante podem competir pelo mesmo número (uma falha e precisa tentar de novo).
- O rascunho de venda salvo no navegador (localStorage) não expira sozinho.
- O bloqueio de login por tentativas é em memória: reseta se a aplicação reiniciar e não é compartilhado entre múltiplas instâncias.

## Como rodar localmente

**Backend** (requer um PostgreSQL local — ajuste `application.properties` se necessário):

```bash
cd api
./mvnw spring-boot:run
```

Sobe em `http://localhost:8080`. Sem nenhuma variável de ambiente, usa os valores padrão de desenvolvimento já em `application.properties` (banco local, segredo JWT de dev).

**Frontend** (estático, qualquer servidor HTTP serve):

```bash
cd frontend
npx serve -l 5500
```

O `assets/js/config.js` escolhe a API sozinho: em `localhost` usa `http://localhost:8080`; em qualquer outro domínio usa a mesma origem, em `/api` (o Caddy repassa ao backend). Ajuste só se o backend local não estiver na porta 8080.

**E-mail de confirmação no ambiente local.** Todo usuário novo recebe um link por e-mail. No `application.properties` local (ignorado pelo git) configure:

- `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password` e `app.mail.from` (ex.: Gmail com senha de app; ver [DEPLOY.md](DEPLOY.md)). Sem eles o e-mail **não é enviado**: o link aparece no console da API e a tela avisa.
- `app.public-url`: o endereço onde o seu frontend está sendo servido, senão o link do e-mail abre uma página inexistente. O padrão é `http://localhost:5500` (o do `npx serve -l 5500`); se você abre o frontend pelo servidor do IntelliJ, use `http://localhost:63342/NOME_DO_PROJETO/frontend`.

## Deploy

Passo a passo completo (VPS com Docker Compose) em [DEPLOY.md](DEPLOY.md).
