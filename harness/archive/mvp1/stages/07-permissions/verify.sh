#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 07 — Permissions =="
echo

check "FolderAccessService"           bash -c "grep -rl 'class[[:space:]]\\+FolderAccessService' '$SRC' | grep -q ."
check "/admin/users/{id}/role route"  bash -c "grep -rE '/admin/users/.+/role' '$SRC' | grep -q ."
check "/admin/users/{id}/permissions" bash -c "grep -rE '/admin/users/.+/permissions' '$SRC' | grep -q ."
# /team/members는 TEAM_LEADER Deprecated로 미구현 — /sales/members로 대체됨
check "/sales/members route"          bash -c "grep -rE '/sales/members' '$SRC' | grep -q ."
check "/shared/folders route"         bash -c "grep -rE '/shared/folders' '$SRC' | grep -q ."

ACT="$SRC/audit/entity/AuditActionType.java"
if [ -f "$ACT" ]; then
  check "AuditActionType has CHANGE_ROLE"        grep -q 'CHANGE_ROLE' "$ACT"
  check "AuditActionType has GRANT_PERMISSION"   grep -q 'GRANT_PERMISSION' "$ACT"
  check "AuditActionType has REVOKE_PERMISSION"  grep -q 'REVOKE_PERMISSION' "$ACT"
fi

PT="$SRC/permission/entity/PermissionType.java"
if [ -f "$PT" ]; then
  check "PermissionType has FOLDER_ACCESS" grep -q 'FOLDER_ACCESS' "$PT"
fi

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
