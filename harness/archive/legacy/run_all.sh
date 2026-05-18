#!/usr/bin/env bash
# 전체 스테이지 순서대로 실행
# 사용: ./harness/run_all.sh [BASE_URL]
#   BASE_URL 기본값: http://localhost:8080

set -euo pipefail
export BASE_URL="${1:-http://localhost:8080}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STAGES_DIR="$SCRIPT_DIR/stages"

green()  { printf "\033[32m%s\033[0m\n" "$*"; }
red()    { printf "\033[31m%s\033[0m\n" "$*"; }
yellow() { printf "\033[33m%s\033[0m\n" "$*"; }

TOTAL_PASS=0
TOTAL_FAIL=0
FAILED_STAGES=()

run_stage() {
  local script="$1"
  local stage_name
  stage_name=$(basename "$(dirname "$script")")

  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

  if bash "$script"; then
    green "[$stage_name] 통과"
    ((TOTAL_PASS++))
  else
    red "[$stage_name] 실패"
    ((TOTAL_FAIL++))
    FAILED_STAGES+=("$stage_name")
  fi
}

echo "========================================"
echo "  eActive Resource Hub — 전체 하네스 실행"
echo "  대상: $BASE_URL"
echo "========================================"

for verify in "$STAGES_DIR"/*/verify.sh; do
  [[ -f "$verify" ]] || continue
  run_stage "$verify"
done

echo ""
echo "========================================"
echo "  최종 결과"
echo "========================================"
if [[ $TOTAL_FAIL -eq 0 ]]; then
  green "  전체 스테이지 ${TOTAL_PASS}/${TOTAL_PASS} 통과 ✔"
  exit 0
else
  red "  ${TOTAL_PASS}/$((TOTAL_PASS + TOTAL_FAIL)) 통과  (실패 스테이지: ${FAILED_STAGES[*]})"
  exit 1
fi
