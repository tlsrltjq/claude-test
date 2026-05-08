#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
WORKSPACE_ROOT="${WORKSPACE_ROOT:-$PROJECT_ROOT}"
RES="$PROJECT_ROOT/src/main/resources"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 10 — Deploy/Security =="
echo

check "application-local.yml"            test -f "$RES/application-local.yml"
check "application-prod.yml"             test -f "$RES/application-prod.yml"
check "docker-compose.prod.yml"          test -f "$PROJECT_ROOT/docker-compose.prod.yml"
check "scripts/backup-db.sh"             test -f "$PROJECT_ROOT/scripts/backup-db.sh"
check "scripts/backup-uploads.sh"        test -f "$PROJECT_ROOT/scripts/backup-uploads.sh"
check "OPERATION_SECURITY_CHECKLIST.md"  bash -c "test -f '$PROJECT_ROOT/docs/OPERATION_SECURITY_CHECKLIST.md' || test -f '$WORKSPACE_ROOT/docs/OPERATION_SECURITY_CHECKLIST.md'"

if [ -f "$PROJECT_ROOT/.gitignore" ]; then
  check ".gitignore has backups/" grep -q '^backups/' "$PROJECT_ROOT/.gitignore"
fi

if [ -f "$PROJECT_ROOT/.env.example" ]; then
  for k in POSTGRES_PASSWORD RESOURCEHUB_ADMIN_EMAIL RESOURCEHUB_ADMIN_PASSWORD \
           RESOURCEHUB_UPLOAD_BASE_DIR RESOURCEHUB_COMPANY_EMAIL_DOMAIN \
           SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD; do
    check ".env.example has $k" grep -q "^$k=" "$PROJECT_ROOT/.env.example"
  done
fi

if [ -f "$RES/application-prod.yml" ]; then
  check "prod yml: secure=true on cookie"  grep -qiE 'secure: *true' "$RES/application-prod.yml"
  check "prod yml: timeout 30m"            grep -qE "timeout: *30m|timeout: *PT30M|timeout: *1800" "$RES/application-prod.yml"
fi

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
