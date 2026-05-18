#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-10 — sales/profiles 정비 =="; echo

check "V204 migration"   bash -c "ls '$RES/db/migration/'V204__* 2>/dev/null | grep -q ."
check "V205 migration"   bash -c "ls '$RES/db/migration/'V205__* 2>/dev/null | grep -q ."

EP="$SRC/employee/entity/EmployeeProfile.java"
[ -f "$EP" ] && check "EmployeeProfile careerTotalDays" grep -q 'careerTotalDays' "$EP"

check "ColumnViewPreference entity" bash -c "grep -rl 'class[[:space:]]\\+ColumnViewPreference' '$SRC' | grep -q ."
check "/sales/profiles/preset route" bash -c "grep -rE '/sales/profiles/preset' '$SRC' | grep -q ."

P="$RES/templates/sales/profiles.html"
[ -f "$P" ] && {
  check "profiles careerDisplay select" grep -qE 'careerDisplay|경력 표시|n년n월n일' "$P"
  check "profiles 등급 위젯"             grep -qE '등급|Grade|JUNIOR|INTERMEDIATE' "$P"
  check "profiles preset select"         grep -qE '프리셋|preset' "$P"
}

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
