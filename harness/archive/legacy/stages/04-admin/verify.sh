#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 04: Admin (승인·팀·직원 관리) ==="

wait_for_app
login "admin@eactive.co.kr" "Admin1234!"

check "GET /admin → 200" "$([[ "$(get_status /admin)" == "200" ]] && echo PASS || echo FAIL)"
check "GET /admin/users/pending → 200" "$([[ "$(get_status /admin/users/pending)" == "200" ]] && echo PASS || echo FAIL)"
check "GET /admin/teams → 200" "$([[ "$(get_status /admin/teams)" == "200" ]] && echo PASS || echo FAIL)"
check "GET /admin/employees → 200" "$([[ "$(get_status /admin/employees)" == "200" ]] && echo PASS || echo FAIL)"

# 비관리자는 /admin 접근 불가
COOKIE_JAR2="$(mktemp)"
curl -sc "$COOKIE_JAR2" "$BASE_URL/login" -o /tmp/_lp2.html -s
csrf2=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/_lp2.html | grep -o 'value="[^"]*"' | cut -d'"' -f2)
curl -sb "$COOKIE_JAR2" -c "$COOKIE_JAR2" -X POST "$BASE_URL/login" \
  -d "username=test%40eactive.co.kr&password=Test1234%21&_csrf=$csrf2" \
  -H "Content-Type: application/x-www-form-urlencoded" -o /dev/null -s
status=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR2" "$BASE_URL/admin")
check "일반 유저 /admin 접근 → 403" "$([[ "$status" == "403" ]] && echo PASS || echo FAIL)"
rm -f "$COOKIE_JAR2"

# DB: admin 유저 존재
admin_count=$(db_query "SELECT COUNT(*) FROM users WHERE role='ADMIN' AND status='ACTIVE';")
check "DB: ADMIN 유저 존재" "$([[ "$admin_count" -ge 1 ]] && echo PASS || echo FAIL)"

summary
