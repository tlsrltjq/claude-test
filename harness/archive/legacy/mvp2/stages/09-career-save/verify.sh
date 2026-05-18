#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 09 — Career Save =="
echo

# 엔드포인트
CTRL="$SRC/user/controller/CareerCalculatorController.java"
check "POST /sales/career-calculator/save 존재"  grep -q 'career-calculator/save' "$CTRL"
check "targetUserId 파라미터 수신"               grep -q 'targetUserId'           "$CTRL"
check "careerMonths 파라미터 수신"               grep -q 'careerMonths'           "$CTRL"
check "careerTotalDays 파라미터 수신"            grep -q 'careerTotalDays'        "$CTRL"
check "developerGrade 파라미터 수신"             grep -q 'developerGrade'         "$CTRL"

# 서비스
SVC="$SRC/employee/service/CareerSaveService.java"
check "CareerSaveService 존재"    [ -f "$SVC" ]
check "career_total_days 저장"    grep -q 'careerTotalDays\|career_total_days' "$SVC"
check "developer_grade 저장"      grep -q 'developerGrade\|developer_grade'   "$SVC"

# 엔티티
EP="$SRC/employee/entity/EmployeeProfile.java"
check "EmployeeProfile: careerTotalDays 필드"  grep -q 'careerTotalDays' "$EP"
check "EmployeeProfile: developerGrade 필드"   grep -q 'developerGrade'  "$EP"

# 감사 로그
ACT="$SRC/audit/entity/AuditActionType.java"
check "AuditActionType: UPDATE_CAREER_PROFILE" grep -q 'UPDATE_CAREER_PROFILE' "$ACT"

# ProfileRow: career 표시용 헬퍼
PR="$SRC/user/service/ProfileRow.java"
check "ProfileRow: getCareerYmd()"              grep -q 'getCareerYmd'           "$PR"
check "ProfileRow: getCareerMonthsFromDays()"   grep -q 'getCareerMonthsFromDays' "$PR"
check "ProfileRow: getCareerTotalDays()"        grep -q 'getCareerTotalDays'     "$PR"

echo; echo "  passed: $PASS  failed: $FAIL"
[ "$FAIL" -eq 0 ]
