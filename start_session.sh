#!/usr/bin/env bash
# 세션 시작 컨텍스트 출력 — AI 에이전트 채팅창에 붙여넣을 프롬프트도 같이 안내.
set -u
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=================================================="
echo " eActive Resource Hub — 세션 시작"
echo "=================================================="
echo

echo "── HARNESS.md ──"
cat "$ROOT/HARNESS.md"
echo
echo "── tasks/current.md ──"
cat "$ROOT/tasks/current.md"
echo

echo "=================================================="
echo " Claude Code / Codex 시작 프롬프트 (복사해서 사용)"
echo "=================================================="
cat <<'EOF'
HARNESS.md 읽고 tasks/current.md 확인해. 현재 단계와 목표를 한 줄로 요약한 뒤 작업 시작해.
완료 기준 도달하면 멈추고 결과 보고. 불확실한 부분은 가정하지 말고 질문해.
EOF
echo
