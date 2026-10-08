#!/usr/bin/env bash
# Prepara um servidor Ubuntu novo (rodar UMA vez, como root):
#   bash setup-servidor.sh
# Faz: confere o Docker, cria swap (se não houver) e liga o firewall (só 22, 80 e 443).
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Rode como root (ou com sudo)." >&2
  exit 1
fi

echo "==> Docker"
if ! command -v docker >/dev/null 2>&1; then
  echo "Docker não encontrado, instalando..."
  curl -fsSL https://get.docker.com | sh
fi
docker --version
docker compose version

echo "==> Swap (o build do Maven usa bastante memória)"
if swapon --show | grep -q .; then
  echo "Já existe swap, nada a fazer."
else
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  grep -q '^/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
  echo "Swap de 2 GB criada."
fi

echo "==> Firewall (ufw): libera só SSH, HTTP e HTTPS"
# A regra do SSH vem ANTES de ligar o firewall, para não perder o acesso ao servidor.
ufw allow 22/tcp >/dev/null
ufw allow 80/tcp >/dev/null
ufw allow 443/tcp >/dev/null
ufw allow 443/udp >/dev/null
ufw --force enable
ufw status

echo
echo "Servidor pronto. Próximo passo: bash gerar-env.sh SEU_DOMINIO"
