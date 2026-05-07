#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-13 — 계정 활성/비활성 =="; echo

check "/admin/employees/{id}/disable"  bash -c "grep -rE '/admin/employees/\\{[a-zA-Z]+\\}/disable' '$SRC' | grep -q ."
check "/admin/employees/{id}/activate" bash -c "grep -rE '/admin/employees/\\{[a-zA-Z]+\\}/activate' '$SRC' | grep -q ."
check "AdminUserStatusService"          bash -c "grep -rl 'class[[:space:]]\\+AdminUserStatusService' '$SRC' | grep -q ."
ACT="$SRC/audit/entity/AuditActionType.java"
[ -f "$ACT" ] && check "AuditActionType CHANGE_USER_STATUS" grep -q 'CHANGE_USER_STATUS' "$ACT"
check "SessionRegistry expireNow trace" bash -c "grep -rE 'SessionRegistry|expireNow' '$SRC' | grep -q ."

E="$RES/templates/admin/employee-detail.html"
[ -f "$E" ] && check "employee-detail toggle btn" bash -c "grep -qE '비활성|활성화|disable|activate' '$E'"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
