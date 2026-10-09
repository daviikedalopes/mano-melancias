#!/usr/bin/env bash
# Cria o primeiro administrador direto no banco (o sistema começa sem usuários).
# Rodar no servidor, dentro de deploy/, com o sistema já no ar:
#   bash criar-admin.sh
#
# O administrador também precisa confirmar o e-mail: a conta nasce "não confirmada"
# e a API envia um link para o endereço informado. Só depois de clicar nele o login funciona.
set -euo pipefail
cd "$(dirname "$0")"

read -r -p "Nome do administrador: " NOME
read -r -p "E-mail: " EMAIL
read -r -s -p "Senha (8+ caracteres, com maiúscula, número e caractere especial): " SENHA; echo
read -r -s -p "Repita a senha: " SENHA2; echo

[ "$SENHA" = "$SENHA2" ] || { echo "As senhas não conferem." >&2; exit 1; }
if [ "${#SENHA}" -lt 8 ] || ! [[ "$SENHA" =~ [A-Z] ]] || ! [[ "$SENHA" =~ [0-9] ]] || ! [[ "$SENHA" =~ [^A-Za-z0-9] ]]; then
  echo "Senha fraca: use 8+ caracteres, com maiúscula, número e caractere especial." >&2
  exit 1
fi
[[ "$EMAIL" =~ ^[^@\ \'\"\\]+@[^@\ \'\"\\]+\.[^@\ \'\"\\]+$ ]] || { echo "E-mail inválido." >&2; exit 1; }
case "$NOME" in *\'*|*\"*|*\\*) echo "Não use aspas nem barra invertida no nome." >&2; exit 1 ;; esac

EMAIL=$(printf '%s' "$EMAIL" | tr 'A-Z' 'a-z')

# Hash BCrypt gerado num container descartável (a senha vai por stdin, não aparece na lista de processos)
HASH=$(printf '%s' "$SENHA" | docker run --rm -i httpd:2-alpine htpasswd -inBC 10 "" | tr -d ':\n')
case "$HASH" in '$2'*) ;; *) echo "Falha ao gerar o hash da senha." >&2; exit 1 ;; esac

docker compose exec -T db psql -v ON_ERROR_STOP=1 -U manomelancias_user manomelancias \
  -c "INSERT INTO usuario (id, nome, email, senha_hash, papel, ativo, email_confirmado) VALUES (gen_random_uuid(), '$NOME', '$EMAIL', '$HASH', 'ADMIN', TRUE, FALSE);"

# Pede à API (por dentro da rede do compose) o envio do link de confirmação.
if docker compose exec -T caddy wget -q -O /dev/null \
     --header 'Content-Type: application/json' \
     --post-data "{\"email\":\"$EMAIL\"}" \
     http://api:8080/auth/reenviar-confirmacao; then
  echo "Administrador criado. Enviamos um link de confirmação para $EMAIL."
  echo "Abra o e-mail e clique no link; só depois disso o login funciona."
  echo "(Sem SMTP configurado o e-mail não sai: o link aparece em 'docker compose logs api'.)"
else
  echo "Administrador criado, mas não consegui pedir o envio do e-mail (a API está no ar?)." >&2
  echo "Use 'Não recebi o e-mail de confirmação' na tela de login para reenviar." >&2
fi
