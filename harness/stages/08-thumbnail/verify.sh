#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 08: Thumbnail & Card View ==="

wait_for_app

# DB: thumbnail 컬럼 존재
col=$(db_query "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='document_versions' AND column_name='thumbnail_storage_path';")
check "DB: thumbnail_storage_path 컬럼" "$([[ "$col" -ge 1 ]] && echo PASS || echo FAIL)"

login "test@eactive.co.kr" "Test1234!"
body=$(get_body /my/folder)
check "폴더 페이지에 doc-card 클래스 포함" \
  "$([[ "$body" == *"doc-card"* ]] && echo PASS || echo FAIL)"
check "폴더 페이지에 filterType 필터 포함" \
  "$([[ "$body" == *"filterType"* ]] && echo PASS || echo FAIL)"

# 썸네일 엔드포인트 (아무 버전이나)
ver_id=$(db_query "SELECT id FROM document_versions LIMIT 1;")
if [[ -n "$ver_id" ]]; then
  check "GET /documents/$ver_id/thumbnail → 200" \
    "$([[ "$(get_status /documents/$ver_id/thumbnail)" == "200" ]] && echo PASS || echo FAIL)"
fi

summary
