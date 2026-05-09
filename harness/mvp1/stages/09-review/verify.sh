#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 09 — Review =="
echo

check "V4 migration"          bash -c "ls '$RES/db/migration/'V4__*review* 2>/dev/null | grep -q ."
check "DocumentReviewStatus"  test -f "$SRC/document/entity/DocumentReviewStatus.java"
check "review list route"     bash -c "grep -rE '/admin/documents/review' '$SRC' | grep -q ."
check "approve route"         bash -c "grep -rE '/approve' '$SRC' | grep -q ."
check "reject route"          bash -c "grep -rE '/reject' '$SRC' | grep -q ."

DV="$SRC/document/entity/DocumentVersion.java"
if [ -f "$DV" ]; then
  check "DocumentVersion: reviewStatus" grep -q 'reviewStatus' "$DV"
  check "DocumentVersion: rejectReason" grep -q 'rejectReason' "$DV"
fi

DRS="$SRC/document/entity/DocumentReviewStatus.java"
if [ -f "$DRS" ]; then
  for v in PENDING_REVIEW APPROVED REJECTED ARCHIVED; do
    check "DocumentReviewStatus has $v" grep -q "$v" "$DRS"
  done
fi

ACT="$SRC/audit/entity/AuditActionType.java"
if [ -f "$ACT" ]; then
  check "AuditActionType has SUBMIT_REVIEW"     grep -q 'SUBMIT_REVIEW' "$ACT"
  check "AuditActionType has APPROVE_DOCUMENT"  grep -q 'APPROVE_DOCUMENT' "$ACT"
  check "AuditActionType has REJECT_DOCUMENT"   grep -q 'REJECT_DOCUMENT' "$ACT"
fi

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
