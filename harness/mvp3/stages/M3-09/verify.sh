#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-09 — sales/members 정렬 + admin/employees 검색 =="; echo

E="$RES/templates/admin/employees.html"
[ -f "$E" ] && {
  check "admin employees q input"      grep -qE 'name="q"' "$E"
  check "admin employees position"     grep -qE 'name="position"' "$E"
  check "admin employees role"         grep -qE 'name="role"' "$E"
  check "admin employees teamId"       grep -qE 'name="teamId"' "$E"
  check "admin employees no '개인폴더' col" bash -c "! grep -E '개인폴더' '$E' >/dev/null"
}

M="$RES/templates/sales/members.html"
[ -f "$M" ] && check "sales/members sort link" grep -qE 'sort=position|sort=team|sort=role' "$M"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
