#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 02 — Download Policy =="
echo

# Reason endpoint removed
check "no /documents/.../download/reason route" bash -c "! grep -rE 'download/reason' '$SRC' >/dev/null"
check "no document-download-reason.html"        bash -c "! find '$RES/templates' -name 'document-download-reason.html' | grep -q ."

# GET download endpoint exists
check "GET /documents/{id}/download route"      bash -c "grep -rE '/documents/\\{[a-zA-Z]+\\}/download' '$SRC' | grep -q ."

# Delete endpoint
check "admin delete document route" bash -c "grep -rE '/admin/documents/\\{[a-zA-Z]+\\}(/delete)?' '$SRC' | grep -q ."

# Audit
ACT="$SRC/audit/entity/AuditActionType.java"
[ -f "$ACT" ] && check "AuditActionType has DELETE_DOCUMENT" grep -q 'DELETE_DOCUMENT' "$ACT"

# Service
check "DocumentDeleteService exists" bash -c "grep -rl 'class[[:space:]]\\+DocumentDeleteService' '$SRC' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
