#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 05: Upload (파일 업로드·버전 관리) ==="

wait_for_app
login "test@eactive.co.kr" "Test1234!"

check "GET /my/folder → 200" "$([[ "$(get_status /my/folder)" == "200" ]] && echo PASS || echo FAIL)"
check "GET /my/folder/documents/upload → 200" \
  "$([[ "$(get_status /my/folder/documents/upload)" == "200" ]] && echo PASS || echo FAIL)"

# DB: document_versions 테이블에 레코드 있는지
ver_count=$(db_query "SELECT COUNT(*) FROM document_versions;")
check "DB: document_versions 레코드 존재" "$([[ "$ver_count" -ge 1 ]] && echo PASS || echo FAIL)"

# 버전 번호 순서
max_ver=$(db_query "SELECT MAX(version_no) FROM document_versions;")
check "DB: version_no >= 1" "$([[ "$max_ver" -ge 1 ]] && echo PASS || echo FAIL)"

summary
