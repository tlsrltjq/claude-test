#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 04 — Team/Folder =="
echo

check "AdminUserApprovalService"   bash -c "grep -rl 'class[[:space:]]\\+AdminUserApprovalService' '$SRC' | grep -q ."
check "TeamService"                bash -c "grep -rl 'class[[:space:]]\\+TeamService' '$SRC' | grep -q ."
check "EmployeeManagementService"  bash -c "grep -rl 'class[[:space:]]\\+EmployeeManagementService' '$SRC' | grep -q ."
check "FolderService"              bash -c "grep -rl 'class[[:space:]]\\+FolderService' '$SRC' | grep -q ."

check "/admin route"               bash -c "grep -rE '\"/admin\"|@RequestMapping\\(\"/admin' '$SRC' | grep -q ."
check "/admin/users/pending route" bash -c "grep -rE '/admin/users/pending' '$SRC' | grep -q ."
check "/admin/teams route"         bash -c "grep -rE '/admin/teams' '$SRC' | grep -q ."
check "/admin/employees route"     bash -c "grep -rE '/admin/employees' '$SRC' | grep -q ."

check "admin/dashboard template"   bash -c "find '$RES/templates' -name 'dashboard.html' -path '*admin*' | grep -q ."
check "admin/users-pending template" bash -c "find '$RES/templates' -name 'users-pending.html' | grep -q ."
check "admin/teams template"       bash -c "find '$RES/templates' -name 'teams.html' | grep -q ."
check "admin/employees template"   bash -c "find '$RES/templates' -name 'employees.html' | grep -q ."

# Initial team seed (any of these patterns)
check "initial team seed present" bash -c "grep -rE '개발팀|영업팀|기술지원팀|경영지원팀' '$SRC' '$RES' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
