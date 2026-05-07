#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 08 — Thumbnail =="
echo

check "V3 migration exists" bash -c "ls '$RES/db/migration/'V3__*thumbnail* 2>/dev/null | grep -q ."
check "ThumbnailService"    bash -c "grep -rl 'class[[:space:]]\\+ThumbnailService' '$SRC' | grep -q ."
check "thumbnail GET route" bash -c "grep -rE '/documents/.+/thumbnail' '$SRC' | grep -q ."
check "thumbnail regenerate route" bash -c "grep -rE 'thumbnail/regenerate' '$SRC' | grep -q ."

DV="$SRC/document/entity/DocumentVersion.java"
if [ -f "$DV" ]; then
  check "DocumentVersion: thumbnailFileName"     grep -q 'thumbnailFileName' "$DV"
  check "DocumentVersion: thumbnailStoragePath"  grep -q 'thumbnailStoragePath' "$DV"
  check "DocumentVersion: thumbnailContentType"  grep -q 'thumbnailContentType' "$DV"
  check "DocumentVersion: thumbnailGeneratedAt"  grep -q 'thumbnailGeneratedAt' "$DV"
fi

ACT="$SRC/audit/entity/AuditActionType.java"
if [ -f "$ACT" ]; then
  check "AuditActionType has REGENERATE_THUMBNAIL" grep -q 'REGENERATE_THUMBNAIL' "$ACT"
fi

if [ -f "$PROJECT_ROOT/build.gradle" ]; then
  check "build.gradle has PDFBox or thumbnail lib" grep -qiE 'pdfbox|thumbnailator' "$PROJECT_ROOT/build.gradle"
fi

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
