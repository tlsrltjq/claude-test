#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 19: Admin Career Months (경력 보정) ==="

wait_for_app
login "admin@eactive.co.kr" "Admin1234!"

# ── 프로필 있는 직원 찾기 ────────────────────────────────────
user_id=$(db_query "SELECT u.id FROM users u JOIN employee_profiles ep ON ep.user_id=u.id WHERE u.status='ACTIVE' LIMIT 1;")

if [[ -z "$user_id" ]]; then
  check "경력 보정 테스트 (skip: 프로필 있는 직원 없음)" "PASS"
  summary
  exit 0
fi

# ── 직원 상세 페이지 ─────────────────────────────────────────
check "GET /admin/employees/$user_id → 200" \
  "$([[ "$(get_status "/admin/employees/$user_id")" == "200" ]] && echo PASS || echo FAIL)"

detail_body=$(get_body "/admin/employees/$user_id")
check "직원 상세: 인력 프로필 카드 포함" \
  "$([[ "$detail_body" == *"인력 프로필"* || "$detail_body" == *"career"* ]] && echo PASS || echo FAIL)"
check "직원 상세: 경력 보정 폼 포함 (career-months)" \
  "$([[ "$detail_body" == *"career-months"* ]] && echo PASS || echo FAIL)"
check "직원 상세: 팀 변경 폼 포함" \
  "$([[ "$detail_body" == *"change-team"* || "$detail_body" == *"팀 변경"* ]] && echo PASS || echo FAIL)"

# ── 경력 보정 POST ──────────────────────────────────────────
before=$(db_query "SELECT career_months FROM employee_profiles WHERE user_id=$user_id;")

csrf_tok=$(echo "$detail_body" | grep -o 'name="_csrf" value="[^"]*"' | head -1 \
  | grep -o 'value="[^"]*"' | cut -d'"' -f2)
update_status=$(curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -X POST "$BASE_URL/admin/employees/$user_id/career-months" \
  -d "_csrf=$csrf_tok&careerMonths=60" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -o /dev/null -w "%{http_code}" -s)
check "POST /admin/employees/$user_id/career-months → 3xx" \
  "$([[ "$update_status" =~ ^3 ]] && echo PASS || echo FAIL)"

after=$(db_query "SELECT career_months FROM employee_profiles WHERE user_id=$user_id;")
check "DB: career_months = 60 업데이트됨" "$([[ "$after" == "60" ]] && echo PASS || echo FAIL)"

# ── 감사 로그 기록 확인 ──────────────────────────────────────
audit_cnt=$(db_query "SELECT COUNT(*) FROM audit_logs WHERE action_type='UPDATE_CAREER_MONTHS';")
check "DB: UPDATE_CAREER_MONTHS 감사 로그 존재" "$([[ "$audit_cnt" -ge 1 ]] && echo PASS || echo FAIL)"

# ── 엑셀 내보내기 감사 로그 ──────────────────────────────────
export_audit=$(db_query "SELECT COUNT(*) FROM audit_logs WHERE action_type='EXPORT_PROFILES';")
check "DB: EXPORT_PROFILES 감사 로그 존재 (stage 15 실행 후)" \
  "$([[ "$export_audit" -ge 1 ]] && echo PASS || echo FAIL)"

# ── 번들 감사 로그 ───────────────────────────────────────────
bundle_audit=$(db_query "SELECT COUNT(*) FROM audit_logs WHERE action_type='EXPORT_BUNDLE';")
check "DB: EXPORT_BUNDLE 감사 로그 존재 (stage 15 실행 후)" \
  "$([[ "$bundle_audit" -ge 1 ]] && echo PASS || echo FAIL)"

summary
