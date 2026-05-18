#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-11 — 체크 후 엑셀 =="; echo

check "/sales/profiles/export route" bash -c "grep -rE '/sales/profiles/export' '$SRC' | grep -q ."
check "SalesProfileExporter"         bash -c "grep -rl 'class[[:space:]]\\+SalesProfileExporter' '$SRC' | grep -q ."
ACT="$SRC/audit/entity/AuditActionType.java"
[ -f "$ACT" ] && check "AuditActionType EXPORT_PROFILES" grep -q 'EXPORT_PROFILES' "$ACT"

P="$RES/templates/sales/profiles.html"
[ -f "$P" ] && {
  check "row checkbox name=selectedIds" grep -qE 'name="selectedIds"' "$P"
  check "선택 엑셀 button hint"           grep -qE '선택.*엑셀|export|내보내기' "$P"
}

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
