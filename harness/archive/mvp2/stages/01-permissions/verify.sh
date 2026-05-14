#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 01 — Permissions =="
echo

# Migration
check "V100 migration exists" bash -c "ls '$RES/db/migration/'V100__*team_leader_to_sales*.sql 2>/dev/null | grep -q ."
if ls "$RES/db/migration/"V100__*.sql >/dev/null 2>&1; then
  V100=$(ls "$RES/db/migration/"V100__*.sql | head -n1)
  check "V100: UPDATE TEAM_LEADER → SALES" grep -qiE "update[[:space:]]+users[[:space:]]+set[[:space:]]+role[[:space:]]*=[[:space:]]*'SALES'" "$V100"
fi

# UserRole enum
UR="$SRC/user/entity/UserRole.java"
if [ -f "$UR" ]; then
  check "UserRole has SALES"        grep -qE "\bSALES\b" "$UR"
  check "UserRole TEAM_LEADER deprecated" bash -c "grep -B2 'TEAM_LEADER' '$UR' | grep -q '@Deprecated' || grep -A0 'TEAM_LEADER' '$UR' | grep -q 'Deprecated'"
fi

# SecurityConfig
SC="$SRC/common/security/SecurityConfig.java"
if [ -f "$SC" ]; then
  check "SecurityConfig has /sales/**" grep -q '/sales/\*\*' "$SC"
  check "SecurityConfig role SALES"     grep -q 'SALES' "$SC"
fi

# Sales controller / template
check "/sales/members route present"     bash -c "grep -rE '/sales/members' '$SRC' | grep -q ."
check "templates/sales/ exists"          test -d "$RES/templates/sales"

# Access services
DAS=$(grep -rl 'class[[:space:]]\+DocumentAccessService' "$SRC" 2>/dev/null | head -n1)
[ -n "${DAS:-}" ] && check "DocumentAccessService references SALES" grep -q 'SALES' "$DAS"
FAS=$(grep -rl 'class[[:space:]]\+FolderAccessService' "$SRC" 2>/dev/null | head -n1)
[ -n "${FAS:-}" ] && check "FolderAccessService references SALES"   grep -q 'SALES' "$FAS"

# Legacy redirect
check "Legacy /team redirect controller" bash -c "grep -rE 'redirect:/sales|RedirectView.*sales|/team/.*->.*sales' '$SRC' | grep -q ."

# Role admin UI: TEAM_LEADER 옵션 제거
ROLE_TEMPLATE=$(grep -rl 'TEAM_LEADER' "$RES/templates" 2>/dev/null | head -n1 || true)
if [ -n "${ROLE_TEMPLATE:-}" ]; then
  echo "  [WARN] template still references TEAM_LEADER: $ROLE_TEMPLATE"
fi

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
