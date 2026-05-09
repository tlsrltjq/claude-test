#!/usr/bin/env bash
# 단계에 메모를 추가한다 (progress.json + harness/logs/<stage>.log).
#
# 사용:
#   bash harness/scripts/log.sh 01 "AI에게 1차 시도 — preview 빠짐"
#   bash harness/scripts/log.sh 01-skeleton "재검증 통과"

set -u
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROGRESS="$HARNESS_DIR/state/progress.json"

if [ $# -lt 2 ]; then
  echo "usage: $0 <stage> <note...>" >&2
  exit 2
fi

ARG="$1"; shift
NOTE="$*"

NUM=$(printf "%02d" "$ARG" 2>/dev/null || echo "")
STAGE_DIR=""
if [ -n "$NUM" ]; then
  STAGE_DIR=$(ls -d "$HARNESS_DIR/stages/${NUM}-"* 2>/dev/null | head -n1)
fi
if [ -z "$STAGE_DIR" ]; then
  STAGE_DIR="$HARNESS_DIR/stages/$ARG"
fi

if [ ! -d "$STAGE_DIR" ]; then
  echo "stage not found: $ARG" >&2
  exit 1
fi

SID="$(basename "$STAGE_DIR")"
LOGFILE="$HARNESS_DIR/logs/${SID}.log"
mkdir -p "$HARNESS_DIR/logs"

TS="$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
echo "[$TS] $NOTE" >> "$LOGFILE"

if [ -f "$PROGRESS" ] && command -v python3 >/dev/null 2>&1; then
  python3 - "$PROGRESS" "$SID" "$TS" "$NOTE" <<'PY'
import json, sys
path, sid, ts, note = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
p = json.load(open(path))
for s in p.get('stages', []):
    if s['id'] == sid:
        s.setdefault('notes', []).append({'at': ts, 'note': note})
        break
json.dump(p, open(path, 'w'), indent=2, ensure_ascii=False)
PY
fi

echo "logged to $LOGFILE"
