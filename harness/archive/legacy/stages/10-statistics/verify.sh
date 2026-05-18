#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 10: Download History & Statistics ==="

wait_for_app
login "admin@eactive.co.kr" "Admin1234!"

check "GET /admin/statistics → 200" \
  "$([[ "$(get_status /admin/statistics)" == "200" ]] && echo PASS || echo FAIL)"

body=$(get_body /admin/statistics)
check "통계 페이지에 총 다운로드 수 포함" \
  "$([[ "$body" == *"총 다운로드"* || "$body" == *"totalCount"* || "$body" == *"다운로드 통계"* ]] && echo PASS || echo FAIL)"
check "통계 페이지에 TOP 문서 테이블 포함" \
  "$([[ "$body" == *"TOP"* || "$body" == *"인기"* || "$body" == *"top-documents"* ]] && echo PASS || echo FAIL)"

# 내 활동 내역
login "test@eactive.co.kr" "Test1234!"
check "GET /my/activity → 200" \
  "$([[ "$(get_status /my/activity)" == "200" ]] && echo PASS || echo FAIL)"

body=$(get_body /my/activity)
check "활동 페이지에 다운로드 내역 포함" \
  "$([[ "$body" == *"다운로드"* || "$body" == *"DOWNLOAD"* || "$body" == *"activity"* ]] && echo PASS || echo FAIL)"

# DB: audit_logs 테이블에 DOWNLOAD 또는 VIEW 레코드
audit=$(db_query "SELECT COUNT(*) FROM audit_logs WHERE action_type IN ('DOWNLOAD','VIEW');")
check "DB: DOWNLOAD/VIEW 감사 로그 존재" "$([[ "$audit" -ge 0 ]] && echo PASS || echo FAIL)"

summary
