#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 05 — Search/Filter/Sort =="
echo

T="$RES/templates/sales/profiles.html"
if [ -f "$T" ]; then
  check "search input q"        grep -qE 'name="q"' "$T"
  check "position select"       grep -qE 'name="position"' "$T"
  check "developerGrade filter" grep -qE 'name="developerGrade"' "$T"
  check "sort param usage"      grep -qE 'sort=|name="sort"' "$T"
  check "column toggle hint"    grep -qE 'cols=|column-toggle|컬럼' "$T"
fi

check "SalesProfileQuery DTO or Specification" \
  bash -c "grep -rl 'SalesProfileQuery\\|SalesProfileSpecification' '$SRC' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
