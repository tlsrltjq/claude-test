#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-06 — /myfolder 본인 삭제 =="; echo

check "self delete route"  bash -c "grep -rE '/my/folder/documents/\\{[a-zA-Z]+\\}(/delete)?|@DeleteMapping.*my/folder' '$SRC' | grep -q ."
check "DocumentDeleteService 재사용" bash -c "grep -rE 'DocumentDeleteService' '$SRC' | grep -q ."

# 삭제 버튼 흔적
check "templates 삭제 버튼" bash -c "grep -rE '삭제|delete' '$RES/templates/my' | grep -q ."

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
