#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 07: Permissions (팀장 권한·폴더 공유) ==="

wait_for_app
login "admin@eactive.co.kr" "Admin1234!"

check "GET /admin/employees → 200" "$([[ "$(get_status /admin/employees)" == "200" ]] && echo PASS || echo FAIL)"

# 직원 ID 하나 조회
uid=$(db_query "SELECT id FROM users WHERE role='EMPLOYEE' AND status='ACTIVE' LIMIT 1;")
if [[ -n "$uid" ]]; then
  check "GET /admin/users/$uid/role → 200" \
    "$([[ "$(get_status /admin/users/$uid/role)" == "200" ]] && echo PASS || echo FAIL)"
  check "GET /admin/users/$uid/permissions → 200" \
    "$([[ "$(get_status /admin/users/$uid/permissions)" == "200" ]] && echo PASS || echo FAIL)"
fi

# DB: permissions 테이블 구조
col=$(db_query "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='permissions' AND column_name='permission_type';")
check "DB: permissions.permission_type 컬럼" "$([[ "$col" -ge 1 ]] && echo PASS || echo FAIL)"

# /shared/folders (인증 후 접근 가능)
login "test@eactive.co.kr" "Test1234!"
check "GET /shared/folders → 200" "$([[ "$(get_status /shared/folders)" == "200" ]] && echo PASS || echo FAIL)"

summary
