#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 06 — Resume Template =="
echo

check "V104 migration"            bash -c "ls '$RES/db/migration/'V104__*resume_template*.sql 2>/dev/null | grep -q ."
check "ResumeTemplate entity"     bash -c "grep -rl 'class[[:space:]]\\+ResumeTemplate\\b' '$SRC' | grep -q ."
check "ResumeTemplateService"     bash -c "grep -rl 'class[[:space:]]\\+ResumeTemplateService' '$SRC' | grep -q ."
check "/admin/resume-template route"      bash -c "grep -rE '/admin/resume-template' '$SRC' | grep -q ."
check "resume-template download route"    bash -c "grep -rE 'resume-template/download|resume-template\".*/download' '$SRC' | grep -q ."
check "templates/admin/resume-template.html" test -f "$RES/templates/admin/resume-template.html"
check "my/folder template has download link" bash -c "grep -qE 'resume-template' '$RES/templates/my/folder.html'"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
