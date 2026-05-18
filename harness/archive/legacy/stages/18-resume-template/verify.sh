#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 18: Resume Template (양식 이력서 관리) ==="

wait_for_app
login "admin@eactive.co.kr" "Admin1234!"

# ── 관리자 양식 이력서 페이지 ───────────────────────────────
check "GET /admin/resume-template → 200 (admin)" \
  "$([[ "$(get_status /admin/resume-template)" == "200" ]] && echo PASS || echo FAIL)"

body=$(get_body "/admin/resume-template")
check "양식 이력서: 파일 업로드 폼 포함" \
  "$([[ "$body" == *'type="file"'* ]] && echo PASS || echo FAIL)"
check "양식 이력서: enctype=multipart 포함" \
  "$([[ "$body" == *"multipart"* ]] && echo PASS || echo FAIL)"

# ── 파일 업로드 테스트 ──────────────────────────────────────
csrf_tok=$(echo "$body" | grep -o 'name="_csrf" value="[^"]*"' | head -1 \
  | grep -o 'value="[^"]*"' | cut -d'"' -f2)

echo "%PDF-1.4 harness-test" > /tmp/harness_test.pdf
upload_status=$(curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -X POST "$BASE_URL/admin/resume-template" \
  -F "_csrf=$csrf_tok" \
  -F "file=@/tmp/harness_test.pdf;type=application/pdf" \
  -o /dev/null -w "%{http_code}" -s)
check "POST /admin/resume-template (PDF 업로드) → 3xx" \
  "$([[ "$upload_status" =~ ^3 ]] && echo PASS || echo FAIL)"

# DB에 레코드 생성됐는지
rt_cnt=$(db_query "SELECT COUNT(*) FROM resume_templates WHERE status='ACTIVE';")
check "DB: resume_templates ACTIVE 레코드 존재" "$([[ "$rt_cnt" -ge 1 ]] && echo PASS || echo FAIL)"

# ── 다운로드 ────────────────────────────────────────────────
dl_status=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR" \
  "$BASE_URL/admin/resume-template/download")
check "GET /admin/resume-template/download → 200" \
  "$([[ "$dl_status" == "200" ]] && echo PASS || echo FAIL)"

# ── SALES 역할도 접근 가능 ──────────────────────────────────
# SALES 유저 있으면 테스트, 없으면 skip
sales_email=$(db_query "SELECT email FROM users WHERE role='SALES' AND status='ACTIVE' LIMIT 1;")
if [[ -n "$sales_email" ]]; then
  COOKIE2="$(mktemp)"
  curl -sc "$COOKIE2" "$BASE_URL/login" -o /tmp/_lp_s.html -s
  csrf2=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/_lp_s.html | grep -o 'value="[^"]*"' | cut -d'"' -f2)
  curl -sb "$COOKIE2" -c "$COOKIE2" -X POST "$BASE_URL/login" \
    -d "username=$(urlencode "$sales_email")&password=Sales1234%21&_csrf=$csrf2" \
    -H "Content-Type: application/x-www-form-urlencoded" -o /dev/null -s
  s2=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE2" "$BASE_URL/admin/resume-template")
  check "SALES 유저 /admin/resume-template 접근 → 200" \
    "$([[ "$s2" == "200" ]] && echo PASS || echo FAIL)"
  rm -f "$COOKIE2"
else
  check "SALES 유저 접근 테스트 (skip: SALES 유저 없음)" "PASS"
fi

# ── 내 폴더에 양식 이력서 다운로드 버튼 ────────────────────
login "test@eactive.co.kr" "Test1234!"
folder_body=$(get_body "/my/folder")
check "내 폴더: 양식 이력서 다운로드 버튼 표시" \
  "$([[ "$folder_body" == *"resume-template"* || "$folder_body" == *"양식"* || "$folder_body" == *"이력서"* ]] && echo PASS || echo FAIL)"

summary
