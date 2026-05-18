#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-05 — DocumentType + 태그 제거 + ppt/pptx =="; echo

check "V202 migration"  bash -c "ls '$RES/db/migration/'V202__* 2>/dev/null | grep -q ."
DT="$SRC/document/entity/DocumentType.java"
[ -f "$DT" ] && {
  check "DocumentType has PROFILE_PHOTO" grep -q 'PROFILE_PHOTO' "$DT"
  check "EMPLOYMENT_CERTIFICATE deprecated" bash -c "grep -B1 'EMPLOYMENT_CERTIFICATE' '$DT' | grep -q '@Deprecated'"
  check "LICENSE displayName 정보처리기사"  grep -q '정보처리기사' "$DT"
}

YML="$RES/application.yml"
[ -f "$YML" ] && {
  check "yml ppt"  grep -qE '\bppt\b' "$YML"
  check "yml pptx" grep -qE '\bpptx\b' "$YML"
}

# 태그 미노출 (templates 안에서 한국어 '태그' 또는 영어 'tag' 입력/표시)
check "templates 어디에도 '태그' input/표 노출 없음" \
  bash -c "! grep -rE '<input[^>]+name=\"tag|name=\"tags|>태그</|배지.*tag' '$RES/templates' >/dev/null"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
