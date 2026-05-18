#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-02 — 로그인 UX =="
echo

check "V201 password_reset_tokens migration" bash -c "ls '$RES/db/migration/'V201__*password_reset* 2>/dev/null | grep -q ."
check "PasswordResetToken entity" bash -c "grep -rl 'class[[:space:]]\\+PasswordResetToken' '$SRC' | grep -q ."
check "/login/forgot route"        bash -c "grep -rE '/login/forgot' '$SRC' | grep -q ."
check "/login/forgot/verify route" bash -c "grep -rE '/login/forgot/verify' '$SRC' | grep -q ."

LH="$RES/templates/login.html"
[ -f "$LH" ] && check "login.html rememberEmail input"    grep -qE 'rememberEmail' "$LH"
[ -f "$LH" ] && check "login.html RESOURCEHUB_LAST_EMAIL" grep -qE 'RESOURCEHUB_LAST_EMAIL' "$LH"

check "login-forgot.html"        test -f "$RES/templates/login-forgot.html"
check "login-forgot-verify.html" test -f "$RES/templates/login-forgot-verify.html"

SV="$RES/templates/signup-verify.html"
[ -f "$SV" ] && check "signup-verify timer hint" grep -qE 'setInterval|setTimeout|expireAt|countdown|타이머|남은' "$SV"

ACT="$SRC/audit/entity/AuditActionType.java"
[ -f "$ACT" ] && check "AuditActionType has RESET_PASSWORD" grep -q 'RESET_PASSWORD' "$ACT"

# TTL 5분 흔적
check "5min TTL trace" bash -c "grep -rE 'ofMinutes\\(5\\)|FIVE_MINUTES|300L?\\b' '$SRC' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
