#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
TMPL="$RES/templates/sales/profiles.html"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 10 — Bundle (선택 행 엑셀 내보내기) =="
echo

# 행 선택 UI
check "profiles.html: 전체 선택 체크박스 (#selectAll)"  grep -q 'id="selectAll"'            "$TMPL"
check "profiles.html: 행 체크박스 (.row-chk)"           grep -q 'row-chk'                  "$TMPL"
check "profiles.html: 선택 엑셀 버튼"                   grep -q '선택 엑셀'                 "$TMPL"
check "profiles.html: submitSelectedExport JS 함수"     grep -q 'submitSelectedExport'     "$TMPL"
check "profiles.html: updateSelectedCount JS 함수"      grep -q 'updateSelectedCount'      "$TMPL"

# POST 폼 (숨김)
check "profiles.html: exportSelectedForm 숨김 폼"       grep -q 'exportSelectedForm'       "$TMPL"
check "profiles.html: selectedIds input"                grep -q 'selectedIds'              "$TMPL"

# 컨트롤러 — POST export selectedIds 필터링
CTRL="$SRC/user/controller/SalesProfileController.java"
check "SalesProfileController: POST export + selectedIds" bash -c "
  grep -q 'selectedIds' '$CTRL' && grep -q 'PostMapping.*export' '$CTRL'
"
check "SalesProfileController: idSet 교집합 필터"  grep -q 'idSet\|filter.*idSet\|contains.*getId' "$CTRL"

# 감사 로그
ACT="$SRC/audit/entity/AuditActionType.java"
check "AuditActionType: EXPORT_PROFILES"  grep -q 'EXPORT_PROFILES' "$ACT"

echo; echo "  passed: $PASS  failed: $FAIL"
[ "$FAIL" -eq 0 ]
