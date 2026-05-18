#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 15: Sales Profiles (인력 표) ==="

wait_for_app
login "admin@eactive.co.kr" "Admin1234!"

# ── 인력 표 페이지 ──────────────────────────────────────────
check "GET /sales/profiles → 200" \
  "$([[ "$(get_status /sales/profiles)" == "200" ]] && echo PASS || echo FAIL)"

body=$(get_body "/sales/profiles")
check "인력 표: 검색 폼(q 파라미터) 포함" \
  "$([[ "$body" == *'name="q"'* ]] && echo PASS || echo FAIL)"
check "인력 표: 직급 필터 포함" \
  "$([[ "$body" == *"position"* || "$body" == *"직급"* ]] && echo PASS || echo FAIL)"
check "인력 표: 정렬 링크 포함" \
  "$([[ "$body" == *"sort="* ]] && echo PASS || echo FAIL)"
check "인력 표: 엑셀 내보내기 버튼 포함" \
  "$([[ "$body" == *"export"* || "$body" == *"엑셀"* ]] && echo PASS || echo FAIL)"
check "인력 표: 번들 다운로드 폼 포함" \
  "$([[ "$body" == *"bundle"* || "$body" == *"묶음"* ]] && echo PASS || echo FAIL)"

# ── 검색 파라미터 ────────────────────────────────────────────
check "GET /sales/profiles?q=test → 200" \
  "$([[ "$(get_status "/sales/profiles?q=test")" == "200" ]] && echo PASS || echo FAIL)"

check "GET /sales/profiles?sort=career&direction=desc → 200" \
  "$([[ "$(get_status "/sales/profiles?sort=career&direction=desc")" == "200" ]] && echo PASS || echo FAIL)"

check "GET /sales/profiles?sort=name&direction=asc → 200" \
  "$([[ "$(get_status "/sales/profiles?sort=name&direction=asc")" == "200" ]] && echo PASS || echo FAIL)"

# ── 엑셀 내보내기 ────────────────────────────────────────────
export_status=$(curl -s -o /tmp/export_test.xlsx -w "%{http_code}" -b "$COOKIE_JAR" \
  "$BASE_URL/sales/profiles/export")
check "GET /sales/profiles/export → 200" \
  "$([[ "$export_status" == "200" ]] && echo PASS || echo FAIL)"

export_type=$(curl -s -I -b "$COOKIE_JAR" "$BASE_URL/sales/profiles/export" \
  | grep -i "content-type" | tr -d '\r')
check "엑셀 내보내기 Content-Type: xlsx" \
  "$([[ "$export_type" == *"spreadsheetml"* || "$export_type" == *"xlsx"* || "$export_type" == *"octet"* ]] && echo PASS || echo FAIL)"

# ── 번들 다운로드 ────────────────────────────────────────────
# 활성 유저 ID 가져오기
user_id=$(db_query "SELECT id FROM users WHERE status='ACTIVE' LIMIT 1;")
if [[ -n "$user_id" ]]; then
  csrf_page=$(get_body "/sales/profiles")
  csrf_tok=$(echo "$csrf_page" | grep -o 'name="_csrf" value="[^"]*"' | head -1 \
    | grep -o 'value="[^"]*"' | cut -d'"' -f2)
  bundle_status=$(curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
    -X POST "$BASE_URL/sales/profiles/bundle" \
    -d "_csrf=$csrf_tok&userIds=$user_id" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -o /dev/null -w "%{http_code}" -s)
  check "POST /sales/profiles/bundle → 200" \
    "$([[ "$bundle_status" == "200" ]] && echo PASS || echo FAIL)"
else
  check "POST /sales/profiles/bundle (skip: 활성 유저 없음)" "PASS"
fi

# ── 비인가 접근 차단 ─────────────────────────────────────────
COOKIE2="$(mktemp)"
# 비로그인 상태로 접근 시도
anon_status=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE2" "$BASE_URL/sales/profiles")
check "비로그인 /sales/profiles → 302 (로그인 리다이렉트)" \
  "$([[ "$anon_status" == "302" || "$anon_status" == "403" ]] && echo PASS || echo FAIL)"
rm -f "$COOKIE2"

summary
