#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
TMPL="$RES/templates/sales/profiles.html"
DB="$RES/db/migration"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 11 — 컬럼 프리셋 + 경력 표시 토글 + 등급 위젯 =="
echo

# Flyway 마이그레이션
check "V204 마이그레이션 존재 (career_total_days 백필)"  bash -c "ls '$DB'/V204__* 2>/dev/null | grep -q ."
check "V205 마이그레이션 존재 (column_view_preferences)" bash -c "ls '$DB'/V205__* 2>/dev/null | grep -q ."
check "V205: column_view_preferences 테이블 생성"        bash -c "grep -q 'column_view_preferences' '$DB'/V205__*.sql"

# 엔티티 / 레포지터리 / 서비스
check "ColumnViewPreference 엔티티 존재"     [ -f "$SRC/user/entity/ColumnViewPreference.java" ]
check "ColumnViewPreferenceRepository 존재" [ -f "$SRC/user/repository/ColumnViewPreferenceRepository.java" ]
check "ColumnViewPreferenceService 존재"    [ -f "$SRC/user/service/ColumnViewPreferenceService.java" ]
check "ColumnViewPreferenceService: save (upsert)" grep -q 'save\|upsert' "$SRC/user/service/ColumnViewPreferenceService.java"
check "ColumnViewPreferenceService: delete (소유권 검사)" grep -q 'delete\|403\|Forbidden\|actorId' "$SRC/user/service/ColumnViewPreferenceService.java"

# 컨트롤러 엔드포인트
CTRL="$SRC/user/controller/SalesProfileController.java"
check "POST /sales/profiles/preset 존재"          grep -q 'preset'     "$CTRL"
check "POST /sales/profiles/preset/{id}/delete"   grep -q 'preset.*delete\|delete.*preset' "$CTRL"

# SalesProfileQuery: careerDisplay 필드
check "SalesProfileQuery: careerDisplay 필드"  grep -q 'careerDisplay' "$SRC/user/dto/SalesProfileQuery.java"

# SalesProfileQueryService: getGradeCountsFromRows
check "getGradeCountsFromRows (필터된 행 기준)"  grep -q 'getGradeCountsFromRows' "$SRC/user/service/SalesProfileQueryService.java"
check "미설정 버킷 포함"                         grep -q '미설정'                  "$SRC/user/service/SalesProfileQueryService.java"

# 템플릿
check "profiles.html: careerDisplay select"    grep -q 'careerDisplay'  "$TMPL"
check "profiles.html: 프리셋 드롭다운"          grep -q 'applyPreset'    "$TMPL"
check "profiles.html: 프리셋 저장 패널"         grep -q 'presetName'     "$TMPL"
check "profiles.html: 등급 위젯 (gradeCounts)"  grep -q 'gradeCounts'    "$TMPL"

echo; echo "  passed: $PASS  failed: $FAIL"
[ "$FAIL" -eq 0 ]
