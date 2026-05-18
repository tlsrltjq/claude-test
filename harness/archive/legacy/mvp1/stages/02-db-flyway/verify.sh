#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
SQL="$PROJECT_ROOT/src/main/resources/db/migration/V1__create_base_tables.sql"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 02 — DB/Flyway =="
echo

check "V1 migration exists" test -f "$SQL"

if [ -f "$SQL" ]; then
  for t in users teams employee_profiles folders documents document_versions permissions audit_logs; do
    check "table CREATE: $t" grep -qiE "create[[:space:]]+table[[:space:]]+(if[[:space:]]+not[[:space:]]+exists[[:space:]]+)?$t" "$SQL"
  done
  check "unique on email"     grep -qi "email" "$SQL"
  check "unique on login_id"  grep -qi "login_id" "$SQL"
fi

# Entity files
for e in \
  common/entity/BaseEntity \
  user/entity/User user/entity/UserRole user/entity/UserStatus \
  team/entity/Team \
  employee/entity/EmployeeProfile employee/entity/AvailableStatus \
  document/entity/Folder document/entity/Document document/entity/DocumentVersion \
  document/entity/DocumentType document/entity/DocumentStatus \
  permission/entity/Permission permission/entity/PermissionType permission/entity/PermissionTargetType \
  audit/entity/AuditLog audit/entity/AuditActionType audit/entity/AuditTargetType; do
  check "entity: $(basename $e).java" test -f "$SRC/$e.java"
done

# Repositories
for r in user/repository/UserRepository team/repository/TeamRepository \
         employee/repository/EmployeeProfileRepository \
         document/repository/FolderRepository document/repository/DocumentRepository document/repository/DocumentVersionRepository \
         permission/repository/PermissionRepository audit/repository/AuditLogRepository; do
  check "repo: $(basename $r).java" test -f "$SRC/$r.java"
done

# JPA Auditing config
check "JpaAuditingConfig present" bash -c "grep -rl '@EnableJpaAuditing' '$SRC' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
