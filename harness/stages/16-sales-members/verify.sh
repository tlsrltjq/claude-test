#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 16: Sales Members (영업부 인력 목록 + 문서) ==="

wait_for_app
login "admin@eactive.co.kr" "Admin1234!"

# ── 영업부 인력 목록 ─────────────────────────────────────────
check "GET /sales/members → 200" \
  "$([[ "$(get_status /sales/members)" == "200" ]] && echo PASS || echo FAIL)"

body=$(get_body "/sales/members")
check "인력 목록: 이름 컬럼 포함" \
  "$([[ "$body" == *"이름"* ]] && echo PASS || echo FAIL)"
check "인력 목록: 팀 컬럼 포함" \
  "$([[ "$body" == *"팀"* ]] && echo PASS || echo FAIL)"
check "인력 목록: 문서 보기 링크 포함 (documents)" \
  "$([[ "$body" == *"documents"* || "$body" == *"문서"* ]] && echo PASS || echo FAIL)"

# ── 영업부 멤버 문서 목록 (SalesController) ──────────────────
user_id=$(db_query "SELECT id FROM users WHERE status='ACTIVE' LIMIT 1;")
if [[ -n "$user_id" ]]; then
  check "GET /sales/members/$user_id/documents → 200 또는 404" \
    "$( s=$(get_status "/sales/members/$user_id/documents"); [[ "$s" == "200" || "$s" == "404" ]] && echo PASS || echo FAIL)"
fi

# ── 영업부 직원 문서 목록 (SalesEmployeeDocumentController) ──
if [[ -n "$user_id" ]]; then
  check "GET /sales/employees/$user_id/documents → 200 또는 404" \
    "$( s=$(get_status "/sales/employees/$user_id/documents"); [[ "$s" == "200" || "$s" == "404" ]] && echo PASS || echo FAIL)"

  emp_body=$(get_body "/sales/employees/$user_id/documents")
  check "직원 문서 페이지: 업로드/삭제 버튼 없음 (읽기 전용)" \
    "$([[ "$emp_body" != *"/upload"* && "$emp_body" != *"/delete"* ]] && echo PASS || echo FAIL)"
fi

# ── 네비게이션 링크 확인 ────────────────────────────────────
check "인력 목록 페이지: 인력 표 링크(/sales/profiles) 포함" \
  "$([[ "$body" == *"/sales/profiles"* ]] && echo PASS || echo FAIL)"
check "인력 목록 페이지: 경력 계산기 링크 포함" \
  "$([[ "$body" == *"career-calculator"* || "$body" == *"경력"* ]] && echo PASS || echo FAIL)"

summary
