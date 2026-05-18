#!/usr/bin/env bash
# 세션 종료 마무리 — 에이전트에게 붙여넣을 종료 프롬프트와 최근 변경 이력 출력.
set -u
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=================================================="
echo " eActive Resource Hub — 세션 종료 마무리"
echo "=================================================="
echo
echo "── 에이전트에 붙여넣을 종료 프롬프트 ──"
cat <<'EOF'
세션 종료 마무리 작업해줘:
1. HARNESS.md "현재 상태" 섹션 갱신 (완료/진행 중/다음)
2. CHANGELOG.md 한 줄 추가 (형식: YYYY-MM-DD | 단계 | feat/fix/chore/docs: 내용)
3. tasks/current.md "이전 세션에서 멈춘 곳" 갱신
EOF
echo
echo "── 최근 CHANGELOG (마지막 5줄) ──"
tail -n 5 "$ROOT/CHANGELOG.md"
echo
echo "── 작업 후 권장 검증 ──"
echo "  bash scripts/security-lint.sh    # 15/15 PASS 유지"
echo "  ./gradlew build                   # BUILD SUCCESSFUL 유지 (코드 변경 시)"
echo
