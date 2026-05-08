#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
TEST="$PROJECT_ROOT/src/test/java/com/eactive/resourcehub"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 07 — Career Calculator =="
echo

check "/sales/career-calculator route" bash -c "grep -rE '/sales/career-calculator' '$SRC' | grep -q ."
check "CareerCalculator class"          bash -c "grep -rl 'class[[:space:]]\\+CareerCalculator\\b' '$SRC' | grep -q ."
check "templates/sales/career-calculator.html" test -f "$RES/templates/sales/career-calculator.html"

# DB 변경 없음 확인 — career-calculator 자체는 DB 추가 없음 (V106은 EmployeeProfile 단계)
check "no V105 migration this stage" bash -c "! ls '$RES/db/migration/'V105__*.sql 2>/dev/null | grep -q ."

# 단위 테스트 존재 (선택)
[ -d "$TEST" ] && {
  if find "$TEST" -name 'CareerCalculator*Test*.java' | grep -q .; then
    echo "  [PASS] CareerCalculator unit test exists"; PASS=$((PASS+1))
  else
    echo "  [WARN] CareerCalculator unit test 권장"
  fi
}

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
