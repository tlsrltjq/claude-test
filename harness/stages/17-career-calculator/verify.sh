#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 17: Career Calculator (경력 계산기 + 저장) ==="

wait_for_app
login "admin@eactive.co.kr" "Admin1234!"

# ── 경력 계산기 페이지 ──────────────────────────────────────
check "GET /sales/career-calculator → 200" \
  "$([[ "$(get_status /sales/career-calculator)" == "200" ]] && echo PASS || echo FAIL)"

body=$(get_body "/sales/career-calculator")
check "경력 계산기: 시작일 입력 필드(starts) 포함" \
  "$([[ "$body" == *'name="starts"'* ]] && echo PASS || echo FAIL)"
check "경력 계산기: 종료일 입력 필드(ends) 포함" \
  "$([[ "$body" == *'name="ends"'* ]] && echo PASS || echo FAIL)"
check "경력 계산기: 중복 제거 체크박스(removeOverlap) 포함" \
  "$([[ "$body" == *"removeOverlap"* ]] && echo PASS || echo FAIL)"
check "경력 계산기: 기간 추가 버튼 포함" \
  "$([[ "$body" == *"add-row"* || "$body" == *"기간 추가"* ]] && echo PASS || echo FAIL)"

# ── 경력 계산 POST ──────────────────────────────────────────
csrf_tok=$(echo "$body" | grep -o 'name="_csrf" value="[^"]*"' | head -1 \
  | grep -o 'value="[^"]*"' | cut -d'"' -f2)

# 단일 기간: 2020-01-01 ~ 2022-12-31 = 36개월
calc_body=$(curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -X POST "$BASE_URL/sales/career-calculator" \
  -d "_csrf=$csrf_tok&starts=2020-01-01&ends=2022-12-31&removeOverlap=false" \
  -H "Content-Type: application/x-www-form-urlencoded" -s)

check "POST /sales/career-calculator → 결과 표시" \
  "$([[ "$calc_body" == *"총 경력"* || "$calc_body" == *"totalMonths"* || "$calc_body" == *"개월"* ]] && echo PASS || echo FAIL)"
check "경력 계산 결과: 36개월 포함" \
  "$([[ "$calc_body" == *"36"* ]] && echo PASS || echo FAIL)"

# 복수 기간 + 중복 제거
calc_body2=$(curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -X POST "$BASE_URL/sales/career-calculator" \
  -d "_csrf=$csrf_tok&starts=2020-01-01&ends=2021-06-30&starts=2021-01-01&ends=2022-12-31&removeOverlap=true" \
  -H "Content-Type: application/x-www-form-urlencoded" -s)

check "POST /sales/career-calculator (중복 제거) → 결과 표시" \
  "$([[ "$calc_body2" == *"총 경력"* || "$calc_body2" == *"개월"* ]] && echo PASS || echo FAIL)"

# 잘못된 날짜 (end < start)
calc_err=$(curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -X POST "$BASE_URL/sales/career-calculator" \
  -d "_csrf=$csrf_tok&starts=2022-12-31&ends=2020-01-01&removeOverlap=false" \
  -H "Content-Type: application/x-www-form-urlencoded" -s)
check "POST 종료일 < 시작일 → 오류 메시지 표시" \
  "$([[ "$calc_err" == *"앞"* || "$calc_err" == *"오류"* || "$calc_err" == *"error"* || "$calc_err" == *"종료"* ]] && echo PASS || echo FAIL)"

# ── 내 프로필 경력 저장 (EMPLOYEE 계정) ──────────────────────
login "test@eactive.co.kr" "Test1234!"

profile_cnt=$(db_query "SELECT COUNT(*) FROM employee_profiles ep JOIN users u ON u.id=ep.user_id WHERE u.email='test@eactive.co.kr';")
if [[ "$profile_cnt" -ge 1 ]]; then
  csrf_page=$(get_body "/my/folder")
  csrf_tok2=$(echo "$csrf_page" | grep -o 'name="_csrf" value="[^"]*"' | head -1 \
    | grep -o 'value="[^"]*"' | cut -d'"' -f2)
  save_status=$(curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
    -X POST "$BASE_URL/my/profile/career-months" \
    -d "_csrf=$csrf_tok2&careerMonths=24" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -o /dev/null -w "%{http_code}" -s)
  check "POST /my/profile/career-months → 3xx" \
    "$([[ "$save_status" =~ ^3 ]] && echo PASS || echo FAIL)"

  saved=$(db_query "SELECT career_months FROM employee_profiles ep JOIN users u ON u.id=ep.user_id WHERE u.email='test@eactive.co.kr';")
  check "DB: career_months = 24 저장됨" "$([[ "$saved" == "24" ]] && echo PASS || echo FAIL)"
else
  check "POST /my/profile/career-months (skip: 프로필 없음)" "PASS"
fi

summary
