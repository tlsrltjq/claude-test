#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 08 — Excel Export =="
echo

# 의존성
check "build.gradle: poi-ooxml" grep -q 'poi-ooxml' "$PROJECT_ROOT/build.gradle"

# 엔드포인트
CTRL="$SRC/user/controller/SalesProfileController.java"
check "GET  /sales/profiles/export"       grep -q 'GetMapping.*export'        "$CTRL"
check "POST /sales/profiles/export"       grep -q 'PostMapping.*export'       "$CTRL"
check "POST export: selectedIds 파라미터"  grep -q 'selectedIds'               "$CTRL"
check "POST export: columnsJson 파라미터"  grep -q 'columnsJson'               "$CTRL"
check "POST export: careerDisplay 반영"    grep -q 'careerDisplay'             "$CTRL"

# 서비스
SVC="$SRC/user/service/ProfileExcelExportService.java"
check "ProfileExcelExportService 존재"    [ -f "$SVC" ]
check "SXSSFWorkbook 스트리밍 사용"        grep -q 'SXSSFWorkbook'             "$SVC"
check "careerDisplay 분기 (careerText)"   grep -q 'careerText'                "$SVC"
check "docCell: 없음 fallback"            grep -q '"없음"'                    "$SVC"

# 감사 로그
ACT="$SRC/audit/entity/AuditActionType.java"
check "AuditActionType: EXPORT_PROFILES"  grep -q 'EXPORT_PROFILES'          "$ACT"

# 파일명 한글
check "한글 파일명 (인력프로필_)" grep -q '인력프로필_' "$CTRL"

echo; echo "  passed: $PASS  failed: $FAIL"
[ "$FAIL" -eq 0 ]
