#!/usr/bin/env bash
# 특정 단계의 verify.sh를 실행한다. 결과에 따라 progress.json을 갱신한다.
#
# 사용:
#   bash harness/scripts/verify.sh 01
#   bash harness/scripts/verify.sh 01-skeleton
#   bash harness/scripts/verify.sh all   # 1~10 전부 순서대로

set -u
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROGRESS="$HARNESS_DIR/state/progress.json"

run_one() {
  local arg="$1"
  local stage_dir=""
  local num
  num=$(printf "%02d" "$arg" 2>/dev/null || echo "")
  if [ -n "$num" ]; then
    stage_dir=$(ls -d "$HARNESS_DIR/stages/${num}-"* 2>/dev/null | head -n1)
  fi
  if [ -z "$stage_dir" ]; then
    stage_dir="$HARNESS_DIR/stages/$arg"
  fi

  if [ ! -d "$stage_dir" ]; then
    echo "stage not found: $arg" >&2
    return 2
  fi

  local sid
  sid="$(basename "$stage_dir")"

  if [ ! -f "$stage_dir/verify.sh" ]; then
    echo "verify.sh missing for $sid" >&2
    return 3
  fi

  bash "$stage_dir/verify.sh"
  local rc=$?

  if [ -f "$PROGRESS" ] && command -v python3 >/dev/null 2>&1; then
    python3 - "$PROGRESS" "$sid" "$rc" <<'PY'
import json, sys, datetime
path, sid, rc = sys.argv[1], sys.argv[2], int(sys.argv[3])
p = json.load(open(path))
for s in p.get('stages', []):
    if s['id'] == sid:
        s['verifyResult'] = 'pass' if rc == 0 else 'fail'
        s['verifiedAt'] = datetime.datetime.utcnow().isoformat() + 'Z'
        if rc == 0:
            s['status'] = 'verified'
            s['completedAt'] = s.get('completedAt') or s['verifiedAt']
        else:
            s['status'] = 'blocked'
        break
json.dump(p, open(path, 'w'), indent=2, ensure_ascii=False)
PY
  fi

  return $rc
}

if [ $# -lt 1 ]; then
  echo "usage: $0 <stage|all>" >&2
  exit 2
fi

if [ "$1" = "all" ]; then
  ANY_FAIL=0
  for d in "$HARNESS_DIR/stages"/*/; do
    sid="$(basename "$d")"
    echo
    echo "==========================================="
    echo " VERIFY $sid"
    echo "==========================================="
    if ! run_one "$sid"; then
      ANY_FAIL=1
    fi
  done
  exit $ANY_FAIL
else
  run_one "$1"
fi
