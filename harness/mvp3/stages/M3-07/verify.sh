#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-07 — 공용 폴더 =="; echo

V=$(ls "$RES/db/migration/"V203__* 2>/dev/null | head -n1 || true)
[ -n "$V" ] && check "V203 migration"           true
[ -n "$V" ] && check "V203 has folders.type"    grep -qi 'folders.*type\|type.*folders\|PERSONAL\|add_folder_type' "$V"
# SHARED_PUBLIC — V203 or any later migration (V207)
check "V203+ has SHARED_PUBLIC" bash -c "grep -rl 'SHARED_PUBLIC' '$RES/db/migration' | grep -q ."

check "FolderType enum"        bash -c "grep -rl 'enum[[:space:]]\\+FolderType' '$SRC' | grep -q ."
check "/shared/folders/public" bash -c "grep -rE '/shared/folders/public' '$SRC' | grep -q ."
check "PublicFolderController" bash -c "grep -rl 'class[[:space:]]\\+PublicFolderController\|class[[:space:]]\\+SharedFolderController' '$SRC' | grep -q ."
check "templates/shared/public-folder.html" test -f "$RES/templates/shared/public-folder.html"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
