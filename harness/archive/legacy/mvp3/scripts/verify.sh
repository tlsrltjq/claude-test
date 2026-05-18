#!/usr/bin/env bash
# 특정 단계의 verify.sh를 실행한다. 결과에 따라 progress.json을 갱신한다.
#
# 사용:
#   bash harness/scripts/verify.sh M3-01
#   bash harness/scripts/verify.sh all                                # MVP3 전부
#   bash harness/scripts/verify.sh all --with-mvp1 --with-mvp2        # + 회귀
#   bash harness/scripts/verify.sh M3-04 --with-mvp2                  # 단일 + mvp2 회귀

set -u
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MVP1_VERIFY="$HARNESS_DIR/../mvp1/scripts/verify.sh"
MVP2_VERIFY="$HARNESS_DIR/../mvp2/scripts/verify.sh"
PROGRESS="$HARNESS_DIR/state/progress.json"

WITH_MVP1=0
WITH_MVP2=0
ARGS=()
for a in "$@"; do
  case "$a" in
    --with-mvp1) WITH_MVP1=1 ;;
    --with-mvp2) WITH_MVP2=1 ;;
    *) ARGS+=("$a") ;;
  esac
done
set -- "${ARGS[@]}"

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
    echo " MVP3 VERIFY $sid"
    echo "==========================================="
    if ! run_one "$sid"; then
      ANY_FAIL=1
    fi
  done
else
  run_one "$1"
  ANY_FAIL=$?
fi

if [ "$WITH_MVP2" -eq 1 ]; then
  if [ -f "$MVP2_VERIFY" ]; then
    echo
    echo "==========================================="
    echo " MVP2 회귀 검사 (mvp2/harness/scripts/verify.sh all)"
    echo "==========================================="
    if ! bash "$MVP2_VERIFY" all; then
      echo "[REGRESSION] MVP2 회귀 발생!" >&2
      ANY_FAIL=1
    fi
  else
    echo "[WARN] mvp2 verify.sh 없음 — 건너뜀" >&2
  fi
fi

if [ "$WITH_MVP1" -eq 1 ]; then
  if [ -x "$MVP1_VERIFY" ] || [ -f "$MVP1_VERIFY" ]; then
    echo
    echo "==========================================="
    echo " MVP1 회귀 검사 (mvp1/harness/scripts/verify.sh all)"
    echo "==========================================="
    if ! bash "$MVP1_VERIFY" all; then
      echo "[REGRESSION] MVP1 회귀 발생!" >&2
      ANY_FAIL=1
    fi
  else
    echo "[WARN] mvp1 verify.sh 없음 — 회귀 검사 건너뜀" >&2
  fi
fi

exit $ANY_FAIL
