#!/usr/bin/env bash
# 특정 단계의 표준 프롬프트(SSOT 헤더 + context + prompt + acceptance)를
# 그대로 표준출력에 뱉는다. AI 도구에 붙여넣어 작업 시작.
#
# 사용:
#   bash harness/scripts/start.sh 01
#   bash harness/scripts/start.sh 03   # zero-padding 자동 보정
#
# 옵션:
#   --status  실행 시 progress.json의 단계 상태를 in_progress로 갱신

set -u
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
WORKSPACE_ROOT="$(cd "$PROJECT_ROOT/.." && pwd)"
MVP_ROOT="$WORKSPACE_ROOT/mvp3"
PROGRESS="$HARNESS_DIR/state/progress.json"

if [ $# -lt 1 ]; then
  echo "usage: $0 <stage-number-or-id> [--status]" >&2
  exit 2
fi

ARG="$1"
SHOULD_UPDATE_STATUS=0
shift || true
for opt in "$@"; do
  case "$opt" in
    --status) SHOULD_UPDATE_STATUS=1 ;;
  esac
done

# Resolve stage dir: accept "1", "01", "01-skeleton", or full id
STAGE_DIR=""
NUM=$(printf "%02d" "$ARG" 2>/dev/null || echo "")
if [ -n "$NUM" ] && [ -d "$HARNESS_DIR/stages" ]; then
  STAGE_DIR=$(ls -d "$HARNESS_DIR/stages/${NUM}-"* 2>/dev/null | head -n1)
fi
if [ -z "$STAGE_DIR" ]; then
  STAGE_DIR="$HARNESS_DIR/stages/$ARG"
fi

if [ ! -d "$STAGE_DIR" ]; then
  echo "stage not found: $ARG" >&2
  echo "available stages:" >&2
  ls "$HARNESS_DIR/stages" >&2
  exit 1
fi

STAGE_ID="$(basename "$STAGE_DIR")"

cat <<EOF
=== eActive Resource Hub — MVP3 Stage $STAGE_ID ===

[프로젝트 SSOT — MVP3]
- 본 SSOT: $MVP_ROOT/docs/PROJECT_SPEC_MVP3.md
- 결정된 항목: $MVP_ROOT/docs/DECISIONS.md
- 단계 분할: $MVP_ROOT/docs/STAGE_PLAN.md
- 마이그레이션 가이드: $MVP_ROOT/docs/MIGRATION_FROM_MVP2.md
- 배경: $WORKSPACE_ROOT/mvp1/docs/PROJECT_SPEC.md, $WORKSPACE_ROOT/mvp2/docs/PROJECT_SPEC_MVP2.md
- 핵심 제약 (mvp1·mvp2와 동일):
  - Java 21 + Spring Boot 3.5.x + Gradle + PostgreSQL
  - 인증: Spring Security 세션 기반 (JWT 절대 금지)
  - Remember-me 금지, CSRF 활성화
  - 세션 30분, 쿠키 RESOURCEHUB_SESSION (httpOnly+sameSite=strict)
  - 파일은 디스크 + UUID 파일명, DB는 메타데이터만
  - 파일 폴더 정적 노출 금지 — 모든 접근은 컨트롤러
- 권한: ADMIN / SALES / EMPLOYEE — 화면 표시는 한글 (관리자/영업/사원)
- 패키지: com.eactive.resourcehub
- 코드는 $WORKSPACE_ROOT/eactive-resource-hub/ 아래에 추가/수정
- Flyway 마이그레이션은 V200 이상 번호로 추가

[이전 단계 컨텍스트]
EOF

if [ -f "$STAGE_DIR/context.md" ]; then
  cat "$STAGE_DIR/context.md"
else
  echo "(context.md missing for $STAGE_ID)"
fi

cat <<'EOF'

[이번 단계 작업 — 그대로 수행]
EOF

if [ -f "$STAGE_DIR/prompt.md" ]; then
  cat "$STAGE_DIR/prompt.md"
else
  echo "(prompt.md missing for $STAGE_ID)"
fi

cat <<'EOF'

[수락 기준 — 끝났으면 자체 점검]
EOF

if [ -f "$STAGE_DIR/acceptance.md" ]; then
  cat "$STAGE_DIR/acceptance.md"
else
  echo "(acceptance.md missing for $STAGE_ID)"
fi

cat <<EOF

---
이번 단계에 명시되지 않은 기능은 만들지 말고, $STAGE_ID 안에서만 작업하라.
완료 후 사람은 \`bash harness/scripts/verify.sh $STAGE_ID\` 로 자동 검증을 돌린다.
EOF

# Update progress.json status -> in_progress
if [ "$SHOULD_UPDATE_STATUS" -eq 1 ] && [ -f "$PROGRESS" ] && command -v python3 >/dev/null 2>&1; then
  python3 - "$PROGRESS" "$STAGE_ID" <<'PY'
import json, sys, datetime
path, sid = sys.argv[1], sys.argv[2]
p = json.load(open(path))
p['currentStage'] = sid
for s in p.get('stages', []):
    if s['id'] == sid:
        if s.get('status') in (None, 'pending', 'blocked'):
            s['status'] = 'in_progress'
            s['startedAt'] = datetime.datetime.utcnow().isoformat() + 'Z'
        break
json.dump(p, open(path, 'w'), indent=2, ensure_ascii=False)
PY
  echo
  echo "(progress.json updated: $STAGE_ID -> in_progress)" >&2
fi
