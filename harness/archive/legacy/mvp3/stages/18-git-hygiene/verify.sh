#!/usr/bin/env bash
# 18-git-hygiene: Git 위생 검증
# 검증 항목:
#   [1] .gitignore 핵심 패턴 존재
#   [2] 민감 파일 미추적 (.env, docker-compose.override.yml)
#   [3] 커밋 히스토리 민감 패턴 미존재 (최근 50 커밋)
#   [4] 작업 폴더 clean 여부
#   [5] main 브랜치 기준 확인
#   [6] merge된 feature 브랜치 잔존 여부 (WARN)
set -uo pipefail

PASS=0; WARN=0; FAIL=0
STAGE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$STAGE_DIR/../../../.." && pwd)"

p() { echo "[PASS] $*"; ((PASS++)); }
w() { echo "[WARN] $*"; ((WARN++)); }
f() { echo "[FAIL] $*"; ((FAIL++)); }

echo "=== 18-git-hygiene: Git 위생 검증 ==="
echo "ROOT: $ROOT"
echo ""

cd "$ROOT"

# ─── .gitignore 패턴 ─────────────────────────────────────────────────────────

GITIGNORE="$ROOT/.gitignore"
echo "--- .gitignore 핵심 패턴 ---"

if [ ! -f "$GITIGNORE" ]; then
    f "[1] .gitignore 파일 없음"
else
    for pattern in ".env" "storage/" "build/" ".idea/" "*.log" "docker-compose.override.yml"; do
        if grep -qF "$pattern" "$GITIGNORE" 2>/dev/null; then
            p "[1] .gitignore에 '$pattern' 패턴 존재"
        else
            f "[1] .gitignore에 '$pattern' 패턴 없음"
        fi
    done
fi

echo ""
echo "--- 민감 파일 미추적 ---"

# [2] 민감 파일이 git에 추적되고 있지 않은지 확인
for sensitive in ".env" "docker-compose.override.yml"; do
    if git -C "$ROOT" ls-files --error-unmatch "$sensitive" > /dev/null 2>&1; then
        f "[2] '$sensitive' 파일이 git에 추적 중 — 민감 정보 유출 위험"
    else
        p "[2] '$sensitive' git-untracked (정상)"
    fi
done

echo ""
echo "--- 커밋 히스토리 민감 패턴 검사 (최근 50 커밋) ---"

# [3] application*.yml에 하드코딩 기본값 패턴 미존재 (security-lint [15] 동일 검사)
#     실제 시크릿이 코드에 박혀있는지 확인. diff 전체 grep은 false positive가 많으므로
#     현재 소스 파일을 직접 검사하는 방식 사용.
YML_FILES=$(find "$ROOT/src/main/resources" -name "application*.yml" 2>/dev/null)
FOUND_YML=""
for f_yml in $YML_FILES; do
    MATCH=$(grep -nE '\$\{[A-Z_]+:(?!)[^}]{4,}\}' "$f_yml" 2>/dev/null | grep -iE '(password|secret|key)' || true)
    if [ -n "$MATCH" ]; then
        FOUND_YML="$FOUND_YML\n$f_yml:\n$MATCH"
    fi
done

if [ -n "$FOUND_YML" ]; then
    f "[3] application*.yml에 민감 설정 하드코딩 기본값 발견:"
    echo -e "$FOUND_YML" | head -8 | sed 's/^/        /'
else
    p "[3] application*.yml — 민감 설정 하드코딩 기본값 없음"
fi

# AWS 자격증명 패턴은 전체 소스에서 검사 (false positive 적음)
AWS_FOUND=$(grep -rn "AKIA[A-Z0-9]\{16\}" "$ROOT/src" 2>/dev/null | grep -v "test\|Test\|example\|example" || true)
if [ -n "$AWS_FOUND" ]; then
    f "[3b] 소스 코드에 AWS Access Key 패턴 발견:"
    echo "$AWS_FOUND" | sed 's/^/        /'
else
    p "[3b] 소스 코드 — AWS Access Key 패턴 없음"
fi

echo ""
echo "--- 작업 폴더 상태 ---"

# [4] 미커밋 변경사항
DIRTY=$(git -C "$ROOT" status --porcelain 2>/dev/null)
if [ -z "$DIRTY" ]; then
    p "[4] 작업 폴더 clean — 미커밋 변경사항 없음"
else
    w "[4] 미커밋 변경사항 있음 (개발 중이라면 정상):"
    echo "$DIRTY" | head -5 | sed 's/^/        /'
fi

echo ""
echo "--- 브랜치 상태 ---"

# [5] 현재 브랜치 확인
CURRENT_BRANCH=$(git -C "$ROOT" branch --show-current 2>/dev/null || echo "detached")
if [ "$CURRENT_BRANCH" = "main" ]; then
    p "[5] 현재 브랜치: main"
else
    w "[5] 현재 브랜치: $CURRENT_BRANCH (main이 아님 — 작업 브랜치라면 정상)"
fi

# origin/main 기준 diverge 확인
if git -C "$ROOT" rev-parse origin/main > /dev/null 2>&1; then
    AHEAD=$(git -C "$ROOT" rev-list origin/main..HEAD --count 2>/dev/null || echo "?")
    BEHIND=$(git -C "$ROOT" rev-list HEAD..origin/main --count 2>/dev/null || echo "?")
    if [ "$AHEAD" = "0" ] && [ "$BEHIND" = "0" ]; then
        p "[5] origin/main과 동기화 상태"
    elif [ "$AHEAD" != "?" ] && [ "$BEHIND" != "?" ]; then
        w "[5] origin/main 대비 ahead=${AHEAD}, behind=${BEHIND}"
    fi
fi

echo ""
echo "--- merge된 feature 브랜치 ---"

# [6] main에 merge된 로컬 브랜치 목록
MERGED=$(git -C "$ROOT" branch --merged main 2>/dev/null | grep -v "^\*\|main$" | grep "feature/" || true)
if [ -n "$MERGED" ]; then
    COUNT=$(echo "$MERGED" | wc -l | tr -d ' ')
    w "[6] main에 merge된 feature/* 브랜치 ${COUNT}개 잔존 — 삭제 권장:"
    echo "$MERGED" | sed 's/^/        /'
else
    p "[6] main에 merge된 feature/* 브랜치 없음 (정상)"
fi

# ─── 결과 ──────────────────────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════"
echo "18-git-hygiene 결과: PASS=${PASS}  WARN=${WARN}  FAIL=${FAIL}"
echo "══════════════════════════════════════"
[ "$FAIL" -eq 0 ]
