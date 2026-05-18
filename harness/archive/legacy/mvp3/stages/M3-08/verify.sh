#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-08 — /search =="; echo

check "/search route"        bash -c "grep -rE '\"/search\"|/search\\\$' '$SRC' | grep -q ."
check "SearchService"        bash -c "grep -rl 'class[[:space:]]\\+SearchService' '$SRC' | grep -q ."
T="$RES/templates/search.html"
[ -f "$T" ] && {
  for k in q type uploader; do
    check "search.html has name=\"$k\"" grep -qE "name=\"$k\"" "$T"
  done
}

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
