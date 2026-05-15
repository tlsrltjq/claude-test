#!/usr/bin/env bash
# eActive Resource Hub — 백업 크론잡 등록 스크립트
# 사용법: bash scripts/setup-cron.sh
# 효과: 매일 03:00 DB 백업, 03:30 업로드 백업 등록
set -euo pipefail

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CRON_DB="0 3 * * * bash ${REPO_DIR}/scripts/backup-db.sh >> ${REPO_DIR}/logs/backup.log 2>&1"
CRON_UPLOADS="30 3 * * * bash ${REPO_DIR}/scripts/backup-uploads.sh >> ${REPO_DIR}/logs/backup.log 2>&1"

mkdir -p "${REPO_DIR}/logs"

# 기존 크론탭을 임시 파일로 내보낸 뒤 중복 없이 추가
TMPFILE=$(mktemp)
crontab -l 2>/dev/null > "$TMPFILE" || true

add_if_missing() {
    local entry="$1"
    if grep -qF "$entry" "$TMPFILE"; then
        echo "이미 등록됨: $entry"
    else
        echo "$entry" >> "$TMPFILE"
        echo "추가됨: $entry"
    fi
}

add_if_missing "$CRON_DB"
add_if_missing "$CRON_UPLOADS"

crontab "$TMPFILE"
rm -f "$TMPFILE"

echo ""
echo "현재 크론탭:"
crontab -l
