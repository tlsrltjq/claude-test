#!/usr/bin/env bash
# 업로드 파일 백업 스크립트
# 사용법: bash scripts/backup-uploads.sh
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-./backups/uploads}"
UPLOADS_DIR="${RESOURCEHUB_UPLOAD_BASE_DIR:-./storage/uploads}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
OUT="$BACKUP_DIR/uploads_${TIMESTAMP}.tar.gz"

mkdir -p "$BACKUP_DIR"

tar -czf "$OUT" -C "$(dirname "$UPLOADS_DIR")" "$(basename "$UPLOADS_DIR")"

echo "업로드 백업 완료: $OUT"

# 30일 이상 된 백업 삭제
find "$BACKUP_DIR" -name "uploads_*.tar.gz" -mtime +30 -delete
