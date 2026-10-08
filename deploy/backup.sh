#!/usr/bin/env bash
# Backup diário do PostgreSQL. Agendar no cron, ex. todo dia às 03:00:
#   0 3 * * * /caminho/para/deploy/backup.sh >> /var/log/mano-backup.log 2>&1
#
# Restaurar (num banco vazio):
#   gunzip -c backups/mano-AAAA-MM-DD_HHMM.sql.gz | docker compose exec -T db psql -U manomelancias_user manomelancias
set -euo pipefail

cd "$(dirname "$0")"
[ -f .env ] && set -a && . ./.env && set +a

DEST="${BACKUP_DIR:-./backups}"
RETENCAO_DIAS="${BACKUP_RETENCAO_DIAS:-14}"
ARQUIVO="$DEST/mano-$(date +%F_%H%M).sql.gz"

mkdir -p "$DEST"
docker compose exec -T db pg_dump -U manomelancias_user --no-owner manomelancias | gzip > "$ARQUIVO"

# Um dump vazio/truncado não pode passar por backup válido
if [ "$(gzip -dc "$ARQUIVO" | wc -c)" -lt 1000 ]; then
  echo "ERRO: backup suspeito (muito pequeno): $ARQUIVO" >&2
  rm -f "$ARQUIVO"
  exit 1
fi

find "$DEST" -name 'mano-*.sql.gz' -mtime +"$RETENCAO_DIAS" -delete

# Cópia fora do servidor (se rclone estiver configurado)
if [ -n "${BACKUP_RCLONE_REMOTE:-}" ]; then
  rclone copy "$ARQUIVO" "$BACKUP_RCLONE_REMOTE"
fi

echo "Backup OK: $ARQUIVO"
