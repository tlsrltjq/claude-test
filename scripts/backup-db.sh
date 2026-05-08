#!/usr/bin/env bash
# PostgreSQL DB 백업 스크립트
# 사용법: bash scripts/backup-db.sh
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-./backups/db}"
CONTAINER="${DB_CONTAINER:-resourcehub-postgres}"
DB_NAME="${POSTGRES_DB:-resourcehub}"
DB_USER="${POSTGRES_USER:-resourcehub}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUT="$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

docker exec "$CONTAINER" \
  pg_dump -U "$DB_USER" -d "$DB_NAME" --no-password \
  | gzip > "$OUT"

echo "DB 백업 완료: $OUT"

# 30일 이상 된 백업 삭제
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +30 -delete
