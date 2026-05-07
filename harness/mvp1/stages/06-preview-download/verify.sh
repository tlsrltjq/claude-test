#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 06 — Preview/Download =="
echo

check "DocumentAccessService"          bash -c "grep -rl 'class[[:space:]]\\+DocumentAccessService' '$SRC' | grep -q ."
check "AuditLogService"                bash -c "grep -rl 'class[[:space:]]\\+AuditLogService' '$SRC' | grep -q ."
check "preview endpoint"               bash -c "grep -rE '/documents/\\{documentVersionId\\}/preview|/documents/.*/preview' '$SRC' | grep -q ."
check "download endpoint"              bash -c "grep -rE '/documents/\\{documentVersionId\\}/download|/documents/.*/download' '$SRC' | grep -q ."
check "download reason endpoint"       bash -c "grep -rE 'download/reason' '$SRC' | grep -q ."

ACT="$SRC/audit/entity/AuditActionType.java"
if [ -f "$ACT" ]; then
  check "AuditActionType has VIEW"     grep -qE '\bVIEW\b' "$ACT"
  check "AuditActionType has DOWNLOAD" grep -qE '\bDOWNLOAD\b' "$ACT"
fi

# 정적 리소스 직접 노출 회피 (Spring 기본 정적 매핑이 /uploads/**로 잡혀있지 않은지 가벼운 휴리스틱)
check "no static handler for /uploads/" bash -c "! grep -rE 'addResourceHandler\\(\\s*\"/uploads' '$SRC' >/dev/null"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
