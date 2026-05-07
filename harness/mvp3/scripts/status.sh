#!/usr/bin/env bash
# 전체 단계 진행 현황을 출력한다.
# 의존: jq 없으면 Python 또는 grep 폴백 사용.

set -u
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROGRESS="$HARNESS_DIR/state/progress.json"

if [ ! -f "$PROGRESS" ]; then
  echo "progress.json not found at $PROGRESS"
  exit 1
fi

echo "== eActive Resource Hub — Harness Status =="
echo

if command -v jq >/dev/null 2>&1; then
  echo "  current stage: $(jq -r '.currentStage' "$PROGRESS")"
  echo
  jq -r '.stages[] | "  [\(.status | ascii_upcase | .[:11] | (. + "           ") | .[:11])] \(.id) — \(.title)"' "$PROGRESS"
elif command -v python3 >/dev/null 2>&1; then
  python3 - "$PROGRESS" <<'PY'
import json, sys
p = json.load(open(sys.argv[1]))
print(f"  current stage: {p.get('currentStage','?')}")
print()
for s in p.get('stages', []):
    status = s.get('status','pending').upper().ljust(11)
    print(f"  [{status}] {s['id']} — {s.get('title','')}")
PY
else
  # Crude fallback
  grep -E '"id"|"status"|"title"' "$PROGRESS"
fi
echo
