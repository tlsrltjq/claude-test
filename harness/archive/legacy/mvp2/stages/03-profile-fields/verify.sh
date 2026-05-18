#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 03 — Profile Fields =="
echo

check "V102 migration"  bash -c "ls '$RES/db/migration/'V102__*profile*.sql 2>/dev/null | grep -q ."
if ls "$RES/db/migration/"V102__*.sql >/dev/null 2>&1; then
  V=$(ls "$RES/db/migration/"V102__*.sql | head -n1)
  check "V102: birth_date column"  grep -qi 'birth_date' "$V"
  check "V102: phone column"       grep -qi 'phone' "$V"
fi

check "Position enum exists" test -f "$SRC/user/entity/Position.java"
if [ -f "$SRC/user/entity/Position.java" ]; then
  for v in REPRESENTATIVE EXECUTIVE_DIRECTOR DIRECTOR GENERAL_MANAGER DEPUTY_GENERAL_MANAGER MANAGER ASSISTANT_MANAGER STAFF; do
    check "Position has $v" grep -q "\b$v\b" "$SRC/user/entity/Position.java"
  done
fi

USR="$SRC/user/entity/User.java"
if [ -f "$USR" ]; then
  check "User has birthDate"  grep -q 'birthDate' "$USR"
  check "User has phone"       grep -q 'phone' "$USR"
  check "User uses Position"   grep -q 'Position' "$USR"
fi

check "signup.html birth field"     bash -c "grep -qE 'type=\"date\"|name=\"birthDate\"|birthDateStr|birth' '$RES/templates/signup.html'"
check "signup.html domain suffix"   bash -c "grep -q 'eactive.co.kr\|company-email-domain\|emailDomain' '$RES/templates/signup.html'"
check "signup.html position select" bash -c "grep -qE 'name=\"position\"|<option' '$RES/templates/signup.html'"
check "signup-verify has no email input" bash -c "! grep -E '<input[^>]+name=\"email\"' '$RES/templates/signup-verify.html' >/dev/null"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
