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

# AdminUserApprovalService는 M3-01에서 승인 흐름 제거로 삭제됨 — 검사 제외
check "TeamService"                bash -c "grep -rl 'class[[:space:]]\\+TeamService' '$SRC' | grep -q ."
check "EmployeeManagementService"  bash -c "grep -rl 'class[[:space:]]\\+EmployeeManagementService' '$SRC' | grep -q ."
check "FolderService"              bash -c "grep -rl 'class[[:space:]]\\+FolderService' '$SRC' | grep -q ."

check "/admin route"               bash -c "grep -rE '\"/admin\"|@RequestMapping\\(\"/admin' '$SRC' | grep -q ."
# /admin/users/pending 및 admin/users-pending template: M3-01에서 승인 흐름 제거로 삭제됨
check "/admin/teams route"         bash -c "grep -rE '/admin/teams' '$SRC' | grep -q ."
check "/admin/employees route"     bash -c "grep -rE '/admin/employees' '$SRC' | grep -q ."

check "admin/dashboard template"   bash -c "find '$RES/templates' -name 'dashboard.html' -path '*admin*' | grep -q ."
check "admin/teams template"       bash -c "find '$RES/templates' -name 'teams.html' | grep -q ."
check "admin/employees template"   bash -c "find '$RES/templates' -name 'employees.html' | grep -q ."

# 기본 팀 자동 시딩 제거됨 — 팀은 관리자가 직접 생성·관리 (M3 결정)

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
