#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-01 — 직급·권한 한글 + 상무 =="
echo

check "V200 migration"   bash -c "ls '$RES/db/migration/'V200__*managing_director* 2>/dev/null | grep -q ."
P="$SRC/user/entity/Position.java"
[ -f "$P" ] && check "Position has MANAGING_DIRECTOR" grep -q 'MANAGING_DIRECTOR' "$P"
[ -f "$P" ] && check "Position has 상무" grep -q '상무' "$P"
UR="$SRC/user/entity/UserRole.java"
[ -f "$UR" ] && check "UserRole has getDisplayName" grep -q 'getDisplayName' "$UR"
[ -f "$UR" ] && check "UserRole 관리자 mapping" grep -q '관리자' "$UR"
[ -f "$UR" ] && check "UserRole 영업 mapping"   grep -q '영업'   "$UR"
[ -f "$UR" ] && check "UserRole 사원 mapping"   grep -q '사원'   "$UR"

# Templates 한글 표시 — Thymeleaf th:text="${*.displayName}" 방식으로 렌더링
# (리터럴 한글 대신 displayName 표현식 사용 여부로 검증)
check "templates role.displayName 사용 (관리자/영업/사원)" \
  bash -c "grep -rE 'role\.displayName|role\.getDisplayName' '$RES/templates' | grep -q ."
check "templates position.displayName 사용 (상무 등 직급)" \
  bash -c "grep -rE 'position\.displayName|pos\.displayName|position\.getDisplayName' '$RES/templates' | grep -q ."
# UserRole enum에 '관리자' 리터럴 존재 (switch 매핑)
check "UserRole enum '관리자' 리터럴" bash -c "grep -q '관리자' '$UR'"
# Position enum에 '상무' 리터럴 존재
check "Position enum '상무' 리터럴"   bash -c "grep -q '상무'   '$P'"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
