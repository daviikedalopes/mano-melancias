#!/usr/bin/env bash
# Cria o arquivo .env com segredos aleatórios (rodar no servidor, dentro de deploy/):
#   bash gerar-env.sh exemplo.com.br
#   bash gerar-env.sh 203-0-113-5.sslip.io     (teste sem domínio: IP com traços + .sslip.io)
# Não sobrescreve um .env existente, para não trocar a senha do banco por engano.
set -euo pipefail
cd "$(dirname "$0")"

DOMINIO="${1:-}"
if [ -z "$DOMINIO" ]; then
  echo "Uso: bash gerar-env.sh SEU_DOMINIO" >&2
  exit 1
fi
case "$DOMINIO" in
  http://*|https://*|*/*) echo "Informe só o domínio, sem http:// nem barras." >&2; exit 1 ;;
esac

if [ -e .env ]; then
  echo "Já existe um .env aqui. Apague-o antes se quiser gerar outro (isso troca os segredos!)." >&2
  exit 1
fi

umask 077
cat > .env <<EOF
DOMINIO=$DOMINIO
POSTGRES_PASSWORD=$(openssl rand -hex 24)
JWT_SECRET=$(openssl rand -hex 48)
JWT_EXPIRATION_MINUTES=480

# E-mail de confirmação de cadastro (SMTP). Preencha antes de subir; veja o DEPLOY.md.
MAIL_HOST=
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=
EOF
chmod 600 .env
echo ".env criado para $DOMINIO (segredos aleatórios, permissão 600)."
echo "Antes de subir, preencha as linhas MAIL_* do .env (servidor SMTP para o e-mail de confirmação):"
echo "  nano .env"
echo "Depois: docker compose up -d --build"
