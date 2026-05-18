#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-12 — career-calculator 검색 =="; echo

C="$RES/templates/sales/career-calculator.html"
if [ -f "$C" ]; then
  check "search button or form action" bash -c "grep -qE '<button[^>]*type=\"submit\"|action=\"/sales/career' '$C'"
  check "search input"                  bash -c "grep -qE '<input[^>]+(type=\"search\"|name=\"q\"|placeholder=\"이름)' '$C'"
fi

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
