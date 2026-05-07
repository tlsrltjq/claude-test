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

# Templates 한글 표시 흔적
check "templates 어딘가에 '관리자'" bash -c "grep -rE '관리자' '$RES/templates' | grep -q ."
check "templates 어딘가에 '영업'"   bash -c "grep -rE '영업'   '$RES/templates' | grep -q ."
check "templates 어딘가에 '사원'"   bash -c "grep -rE '사원'   '$RES/templates' | grep -q ."
check "templates 어딘가에 '상무'"   bash -c "grep -rE '상무'   '$RES/templates' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
