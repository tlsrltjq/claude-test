#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 03: Auth (회원가입·이메일인증·로그인) ==="

wait_for_app

# /health
check "GET /health → 200" "$([[ "$(get_status /health)" == "200" ]] && echo PASS || echo FAIL)"

# 비인증 접근 차단
check "GET /dashboard → 302(로그인 리다이렉트)" "$([[ "$(get_status /dashboard)" == "302" ]] && echo PASS || echo FAIL)"

# 로그인 페이지
check "GET /login → 200" "$([[ "$(get_status /login)" == "200" ]] && echo PASS || echo FAIL)"

# 잘못된 자격으로 로그인 실패
login "wrong@eactive.co.kr" "wrongpass"
check "잘못된 로그인 → /login?error 리다이렉트" \
  "$([[ "$(get_status /dashboard)" == "302" ]] && echo PASS || echo FAIL)"

# 올바른 로그인
login "admin@eactive.co.kr" "Admin1234!"
check "관리자 로그인 성공" "$([[ "$(get_status /dashboard)" == "200" ]] && echo PASS || echo FAIL)"

summary
