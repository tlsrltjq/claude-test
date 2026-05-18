#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
RES="$PROJECT_ROOT/src/main/resources"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-04 — /dashboard 보강 =="; echo

D="$RES/templates/dashboard.html"
[ -f "$D" ] && {
  for kw in 등급 생년월일 팀 연락처 직급; do
    check "dashboard has '$kw'" grep -q "$kw" "$D"
  done
}
check "DashboardSelfView or equivalent DTO" bash -c "grep -rE 'DashboardSelfView|DashboardView|class[[:space:]]+DashboardController' '$SRC' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
