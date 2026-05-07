#!/usr/bin/env bash
# Stage NN — 자동 검증 스크립트
#
# 사용: bash verify.sh
# 종료코드:
#   0 — 모든 자동 검증 통과
#   1 — 자동 검증 실패 (어디서 실패했는지 출력)
#
# 자동으로 잡을 수 있는 것만 잡는다. 사람만 판단할 수 있는 항목은 acceptance.md.

set -u
PASS=0
FAIL=0

# 프로젝트 루트(=하네스 부모의 부모)
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MVP_ROOT="$(cd "$HARNESS_DIR/.." && pwd)"
WORKSPACE_ROOT="$(cd "$MVP_ROOT/.." && pwd)"
PROJECT_ROOT="$WORKSPACE_ROOT/eactive-resource-hub"

check() {
  local desc="$1"; shift
  if "$@" >/dev/null 2>&1; then
    echo "  [PASS] $desc"
    PASS=$((PASS+1))
  else
    echo "  [FAIL] $desc"
    FAIL=$((FAIL+1))
  fi
}

echo "== Stage NN verify =="
echo "  workspace: $WORKSPACE_ROOT"
echo "  project:   $PROJECT_ROOT"
echo

# (단계별로 채울 자리)
# check "build.gradle exists" test -f "$PROJECT_ROOT/build.gradle"

echo
echo "  passed: $PASS"
echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
