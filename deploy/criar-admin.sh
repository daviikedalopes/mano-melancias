#!/usr/bin/env bash
# Cria o primeiro administrador direto no banco (o sistema começa sem usuários).
# Rodar no servidor, dentro de deploy/, com o sistema já no ar:
#   bash criar-admin.sh
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
[[ "$EMAIL" =~ ^[^@\ \']+@[^@\ \']+\.[^@\ \']+$ ]] || { echo "E-mail inválido." >&2; exit 1; }
case "$NOME$EMAIL" in *\'*) echo "Não use aspas simples no nome ou e-mail." >&2; exit 1 ;; esac

# Hash BCrypt gerado num container descartável (a senha vai por stdin, não aparece na lista de processos)
HASH=$(printf '%s' "$SENHA" | docker run --rm -i httpd:2-alpine htpasswd -inBC 10 "" | tr -d ':\n')
case "$HASH" in '$2'*) ;; *) echo "Falha ao gerar o hash da senha." >&2; exit 1 ;; esac

docker compose exec -T db psql -v ON_ERROR_STOP=1 -U manomelancias_user manomelancias \
  -c "INSERT INTO usuario (id, nome, email, senha_hash, papel, ativo) VALUES (gen_random_uuid(), '$NOME', lower('$EMAIL'), '$HASH', 'ADMIN', TRUE);"

echo "Administrador criado. Entre no sistema com o e-mail informado."
