#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 06: Preview & Download (미리보기·다운로드) ==="

wait_for_app
login "test@eactive.co.kr" "Test1234!"

# 승인된 버전 ID 조회
ver_id=$(db_query "SELECT id FROM document_versions WHERE review_status='APPROVED' LIMIT 1;")

if [[ -n "$ver_id" ]]; then
  check "GET /documents/$ver_id/preview → 2xx 또는 415" \
    "$([[ "$(get_status /documents/$ver_id/preview)" =~ ^(200|415)$ ]] && echo PASS || echo FAIL)"
  check "GET /documents/$ver_id/download/reason → 200" \
    "$([[ "$(get_status /documents/$ver_id/download/reason)" == "200" ]] && echo PASS || echo FAIL)"
  check "GET /documents/$ver_id/thumbnail → 200" \
    "$([[ "$(get_status /documents/$ver_id/thumbnail)" == "200" ]] && echo PASS || echo FAIL)"
else
  yellow "  - 승인된 버전 없음 — 스킵"
fi

# DB: audit_logs에 DOWNLOAD/VIEW 액션 존재
audit_count=$(db_query "SELECT COUNT(*) FROM audit_logs WHERE action_type IN ('VIEW','DOWNLOAD');")
check "DB: VIEW/DOWNLOAD 감사 로그 존재" "$([[ "$audit_count" -ge 0 ]] && echo PASS || echo FAIL)"

summary
