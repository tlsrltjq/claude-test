#!/usr/bin/env bash
# M3-16: 데이터 무결성 + 런타임 안전성 검증
# 이 스테이지에서 방지하는 버그:
#   [1] Collectors.toMap 머지 함수 누락 → Duplicate key 런타임 예외
#   [2] 중복 PERSONAL 폴더 → findByOwnerIdAndType "unique result" 예외
#   [3] application-prod.yml Secure=true + HTTP 환경 → 로그인 무한 리다이렉트
#   [4] invalidSessionUrl("/login") 재등장 → 로그인 무한 리다이렉트
set -uo pipefail

PASS=0; WARN=0; FAIL=0
STAGE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$STAGE_DIR/../../../.." && pwd)"
SRC="$ROOT/src"

p() { echo "[PASS] $*"; ((PASS++)); }
w() { echo "[WARN] $*"; ((WARN++)); }
f() { echo "[FAIL] $*"; ((FAIL++)); }

src_grep() {
    # src 접근 불가 시 경고만 내고 false 반환
    grep -rn "$@" "$SRC" --include="*.java" 2>/dev/null
}

src_accessible() {
    [ -r "$SRC/main" ] 2>/dev/null
}

echo "=== M3-16 데이터 무결성 + 런타임 안전성 ==="
echo "ROOT: $ROOT"
echo ""

# ─── 코드 정적 분석 ────────────────────────────────────────────────────────────

if src_accessible; then

    # [1] Collectors.toMap 머지 함수 누락
    #     2인수: toMap(key, value)  → 중복 키 시 IllegalStateException
    #     3인수: toMap(key, value, merge) → 안전
    TOMAP_UNSAFE=$(src_grep -P '\.collect\(\s*Collectors\.toMap\([^)]+,[^)]+\)\s*\)' 2>/dev/null || true)
    # 더 단순한 패턴으로 재시도
    if [ -z "$TOMAP_UNSAFE" ]; then
        # 3인수가 아닌 toMap 호출 찾기: toMap(A, B) 형태 (쉼표 2개 미만)
        TOMAP_ALL=$(src_grep "Collectors\.toMap(" 2>/dev/null || true)
        TOMAP_SAFE=$(src_grep "Collectors\.toMap([^)]*,[^)]*,[^)]*)" 2>/dev/null || true)
        TOMAP_UNSAFE=""
        while IFS= read -r line; do
            if [ -n "$line" ] && ! echo "$line" | grep -q "toMap([^)]*,[^)]*,[^)]*)"; then
                TOMAP_UNSAFE+="$line"$'\n'
            fi
        done <<< "$TOMAP_ALL"
    fi

    if [ -n "$TOMAP_UNSAFE" ]; then
        f "[1] Collectors.toMap() 2인수 호출 발견 — 머지 함수 없으면 중복 키 시 런타임 예외:"
        echo "$TOMAP_UNSAFE" | sed 's/^/        /'
    else
        p "[1] Collectors.toMap() — 머지 함수(3인수) 형태 확인"
    fi

    # [2] invalidSessionUrl("/login") 재등장 검사
    if src_grep "invalidSessionUrl" -q 2>/dev/null; then
        f "[2] SecurityConfig에 invalidSessionUrl() 존재 — 로그인 페이지 무한 리다이렉트 발생 위험"
        src_grep "invalidSessionUrl" | sed 's/^/        /'
    else
        p "[2] invalidSessionUrl() 없음 — 정상"
    fi

    # [3] application-prod.yml Secure=true + docker-compose HTTP 오버라이드 확인
    PROD_YML="$ROOT/src/main/resources/application-prod.yml"
    COMPOSE="$ROOT/docker-compose.yml"
    if [ -f "$PROD_YML" ] && grep -q "secure: true" "$PROD_YML" 2>/dev/null; then
        if [ -f "$COMPOSE" ] && grep -q "SERVER_SERVLET_SESSION_COOKIE_SECURE.*false" "$COMPOSE" 2>/dev/null; then
            p "[3] prod Secure=true, docker-compose.yml에서 HTTP용 false 오버라이드 확인"
        else
            f "[3] application-prod.yml Secure=true 이지만 docker-compose.yml에 오버라이드 없음"
            echo "        → HTTP 환경에서 세션 쿠키가 전송되지 않아 무한 리다이렉트 발생"
            echo "        → docker-compose.yml app.environment에 추가:"
            echo "             SERVER_SERVLET_SESSION_COOKIE_SECURE: \"false\""
        fi
    else
        p "[3] application-prod.yml Secure 설정 — 검사 해당없음"
    fi

    # [4] FolderRepository Optional findBy — UNIQUE 제약 확인
    OPTIONAL_FINDBY=$(src_grep "Optional<Folder> findBy" 2>/dev/null || true)
    if [ -n "$OPTIONAL_FINDBY" ]; then
        MIGRATION_DIR="$SRC/main/resources/db/migration"
        UNIQUE_OWNER_TYPE=$(grep -rn "unique\|UNIQUE" "$MIGRATION_DIR" --include="*.sql" 2>/dev/null \
            | grep -i "owner\|type" || true)
        if [ -n "$UNIQUE_OWNER_TYPE" ]; then
            p "[4] FolderRepository Optional findBy — DB UNIQUE 제약 마이그레이션 확인됨"
        else
            w "[4] FolderRepository.findByOwnerIdAndType Optional 사용 중이지만 마이그레이션에서 (owner_id, type) UNIQUE 제약 미확인"
            echo "        → 중복 폴더 생성 시 'Query did not return a unique result' 예외 발생"
        fi
    else
        p "[4] Optional findByOwnerIdAndType — 해당 패턴 없음"
    fi

else
    w "src 디렉터리 접근 불가 — 정적 분석 건너뜀 ($SRC)"
    ((WARN+=4))
fi

# ─── 런타임 DB 검사 ────────────────────────────────────────────────────────────

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-resourcehub}"
DB_USER="${DB_USER:-resourcehub}"
export PGPASSWORD="${POSTGRES_PASSWORD:-resourcehub}"

run_psql() {
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "$1" 2>/dev/null
}

if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1" > /dev/null 2>&1; then
    echo ""
    echo "--- DB 런타임 검사 ---"

    # [5] folders (owner_user_id, type) 중복 검사 — 핵심 버그 재발 탐지
    DUP=$(run_psql \
        "SELECT owner_user_id, type, COUNT(*) cnt FROM folders GROUP BY owner_user_id, type HAVING COUNT(*)>1;")
    if [ -n "$DUP" ]; then
        f "[5] folders 중복 (owner_user_id, type) — findByOwnerIdAndType 500 오류 유발:"
        echo "$DUP" | column -t | sed 's/^/        /'
        echo "        → 중복 폴더 중 빈 것을 DELETE 후 재검증하세요"
    else
        p "[5] folders (owner_user_id, type) 중복 없음"
    fi

    # [6] ACTIVE 사용자 전원 PERSONAL 폴더 보유 확인
    MISSING=$(run_psql \
        "SELECT u.id, u.name FROM users u
         LEFT JOIN folders f ON f.owner_user_id=u.id AND f.type='PERSONAL'
         WHERE u.status='ACTIVE' AND f.id IS NULL
         LIMIT 10;")
    if [ -n "$MISSING" ]; then
        w "[6] PERSONAL 폴더 없는 ACTIVE 사용자 (최대 10명):"
        echo "$MISSING" | sed 's/^/        /'
    else
        p "[6] 모든 ACTIVE 사용자 PERSONAL 폴더 보유"
    fi

    # [7] SHARED_PUBLIC 폴더 존재 확인
    PUB=$(run_psql "SELECT COUNT(*) FROM folders WHERE type='SHARED_PUBLIC';")
    if [ "${PUB:-0}" -ge 1 ]; then
        p "[7] SHARED_PUBLIC 폴더 ${PUB}개 존재"
    else
        w "[7] SHARED_PUBLIC 폴더 없음"
    fi

    # [8] ACTIVE 문서 current_version_id NULL 비율
    TOTAL=$(run_psql "SELECT COUNT(*) FROM documents WHERE status='ACTIVE';")
    NULL_V=$(run_psql "SELECT COUNT(*) FROM documents WHERE status='ACTIVE' AND current_version_id IS NULL;")
    TOTAL=${TOTAL:-0}; NULL_V=${NULL_V:-0}
    if [ "$TOTAL" -gt 0 ] && [ "$NULL_V" -gt 0 ]; then
        PCT=$(( NULL_V * 100 / TOTAL ))
        if [ "$PCT" -gt 10 ]; then
            w "[8] ACTIVE 문서 current_version_id NULL ${PCT}% (${NULL_V}/${TOTAL}) — 업로드 오류 의심"
        else
            p "[8] current_version_id NULL 비율 ${PCT}% — 허용 범위"
        fi
    else
        p "[8] current_version_id NULL 없음 (총 ${TOTAL}건)"
    fi

    # [9] 문서 버전 storage_path 포맷 일관성 (yyyy/MM/uuid.ext)
    BAD_PATH=$(run_psql \
        "SELECT COUNT(*) FROM document_versions
         WHERE stored_file_name IS NOT NULL
           AND storage_path NOT SIMILAR TO '[0-9]{4}/[0-9]{2}/%.%';")
    if [ "${BAD_PATH:-0}" -gt 0 ]; then
        w "[9] storage_path 포맷 불일치 ${BAD_PATH}건 (기대: yyyy/MM/uuid.ext)"
    else
        p "[9] document_versions storage_path 포맷 일관성 OK"
    fi

else
    w "DB 연결 불가 — 런타임 DB 검사 건너뜀 (host=${DB_HOST}:${DB_PORT})"
    ((WARN+=5))
fi

# ─── 결과 ──────────────────────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════"
echo "M3-16 결과: PASS=${PASS}  WARN=${WARN}  FAIL=${FAIL}"
echo "══════════════════════════════════════"
[ "$FAIL" -eq 0 ]
