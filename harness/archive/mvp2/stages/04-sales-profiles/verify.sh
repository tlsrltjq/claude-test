#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 04 — Sales Profiles =="
echo

check "V103 migration"          bash -c "ls '$RES/db/migration/'V103__*developer_grade*.sql 2>/dev/null | grep -q ."
EP="$SRC/employee/entity/EmployeeProfile.java"
[ -f "$EP" ] && {
  check "EmployeeProfile: developerGrade" grep -q 'developerGrade' "$EP"
  check "EmployeeProfile: careerMonths"   grep -q 'careerMonths' "$EP"
}
DT="$SRC/document/entity/DocumentType.java"
[ -f "$DT" ] && check "DocumentType has HEALTH_INSURANCE_PROOF" grep -q 'HEALTH_INSURANCE_PROOF' "$DT"

check "/sales/profiles route"               bash -c "grep -rE '/sales/profiles' '$SRC' | grep -q ."
check "/sales/employees/{id}/documents route" bash -c "grep -rE '/sales/employees/\\{[a-zA-Z]+\\}/documents' '$SRC' | grep -q ."
check "SalesProfileQueryService"            bash -c "grep -rl 'class[[:space:]]\\+SalesProfileQueryService' '$SRC' | grep -q ."
check "templates/sales/profiles.html"       test -f "$RES/templates/sales/profiles.html"
check "templates/sales/employee-documents.html" test -f "$RES/templates/sales/employee-documents.html"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
