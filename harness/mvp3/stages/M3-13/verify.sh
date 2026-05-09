#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-13 — 계정 활성/비활성 =="; echo

check "/admin/employees/{id}/toggle-status 엔드포인트" \
  bash -c "grep -rE 'PostMapping.*toggle-status' '$SRC' | grep -q ."
check "EmployeeManagementService.toggleStatus()" \
  bash -c "grep -rE 'toggleStatus' '$SRC' | grep -q ."
ACT="$SRC/audit/entity/AuditActionType.java"
[ -f "$ACT" ] && check "AuditActionType DISABLE_USER"  grep -q 'DISABLE_USER'  "$ACT"
[ -f "$ACT" ] && check "AuditActionType ENABLE_USER"   grep -q 'ENABLE_USER'   "$ACT"
check "SessionRegistry 세션 즉시 만료" bash -c "grep -rE 'SessionRegistry|expireNow' '$SRC' | grep -q ."

E="$RES/templates/admin/employee-detail.html"
[ -f "$E" ] && check "employee-detail 토글 버튼" bash -c "grep -qE '비활성|활성화|toggle-status' '$E'"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
