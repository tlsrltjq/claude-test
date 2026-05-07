#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 05 — Upload =="
echo

check "FileStorage interface"  bash -c "grep -rE 'interface[[:space:]]+FileStorage' '$SRC/common/file' >/dev/null"
check "LocalFileStorage class" bash -c "grep -rE 'class[[:space:]]+LocalFileStorage' '$SRC/common/file' >/dev/null"
check "/my/folder route"       bash -c "grep -rE '/my/folder' '$SRC' >/dev/null"
check "/admin/employees/{id}/documents route" bash -c "grep -rE '/admin/employees/\\{userId\\}/documents|/admin/employees/.*/documents' '$SRC' >/dev/null"

if [ -f "$RES/application.yml" ]; then
  check "yml: max-file-size 20MB"       grep -qE "max-file-size: *20" "$RES/application.yml"
  check "yml: max-request-size 40MB"    grep -qE "max-request-size: *40" "$RES/application.yml"
  check "yml: allowed-extensions list"  grep -qE "allowed-extensions" "$RES/application.yml"
fi

# DocumentType enum values
DT="$SRC/document/entity/DocumentType.java"
if [ -f "$DT" ]; then
  for v in RESUME CAREER_DESCRIPTION GRADUATION_CERTIFICATE LICENSE EMPLOYMENT_CERTIFICATE ETC; do
    check "DocumentType has $v" grep -q "$v" "$DT"
  done
fi

# Templates
check "my/folder template"         bash -c "find '$RES/templates' -name 'folder.html' | grep -q ."
check "my/upload template"         bash -c "find '$RES/templates' -name 'upload.html' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
