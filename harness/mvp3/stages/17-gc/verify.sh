#!/usr/bin/env bash
# M3-17: 파일 GC (Garbage Collection) 구현 검증
# 검증 항목:
#   [1] V210 마이그레이션 존재 (files_purged_at 컬럼)
#   [2] Document.java에 filesPurgedAt 필드 + markFilesPurged() 메서드
#   [3] DocumentRepository.findPurgeCandidates 쿼리 존재
#   [4] DocumentVersionRepository.findByDocumentIdIn 쿼리 존재
#   [5] DocumentFileGcService 클래스 존재
#   [6] @Scheduled 새벽 2시 cron 설정
#   [7] AdminController에 /admin/gc POST 엔드포인트 존재
#   [8] gc.html 템플릿 존재
#   [9] application.yml에 GC 보존 기간 설정 존재
#  [10] DB: files_purged_at 컬럼 실제 존재
#  [11] DB: GC 부분 인덱스 존재
set -uo pipefail

PASS=0; WARN=0; FAIL=0
STAGE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$STAGE_DIR/../../../.." && pwd)"
SRC="$ROOT/src"

p() { echo "[PASS] $*"; ((PASS++)); }
w() { echo "[WARN] $*"; ((WARN++)); }
f() { echo "[FAIL] $*"; ((FAIL++)); }

src_grep() {
    grep -rn "$@" "$SRC" --include="*.java" 2>/dev/null
}

src_accessible() {
    [ -r "$SRC/main" ] 2>/dev/null
}

echo "=== M3-17 파일 GC (Garbage Collection) ==="
echo "ROOT: $ROOT"
echo ""

# ─── 정적 분석 ────────────────────────────────────────────────────────────────

MIGRATION_DIR="$SRC/main/resources/db/migration"
TMPL_DIR="$SRC/main/resources/templates/admin"

# [1] V210 마이그레이션
if ls "$MIGRATION_DIR"/V210*.sql 2>/dev/null | grep -q .; then
    V210=$(ls "$MIGRATION_DIR"/V210*.sql | head -1)
    if grep -q "files_purged_at" "$V210" 2>/dev/null; then
        p "[1] V210 마이그레이션 존재 + files_purged_at 컬럼 포함"
    else
        f "[1] V210 마이그레이션 존재하지만 files_purged_at 없음"
    fi
else
    f "[1] V210 마이그레이션 파일 없음 — files_purged_at 컬럼 미추가"
fi

if src_accessible; then

    # [2] Document.java: filesPurgedAt + markFilesPurged
    DOC_JAVA=$(find "$SRC" -name "Document.java" -path "*/document/entity/*" | head -1)
    if [ -n "$DOC_JAVA" ]; then
        HAS_FIELD=$(grep -c "filesPurgedAt\|files_purged_at" "$DOC_JAVA" 2>/dev/null || echo 0)
        HAS_METHOD=$(grep -c "markFilesPurged" "$DOC_JAVA" 2>/dev/null || echo 0)
        if [ "$HAS_FIELD" -gt 0 ] && [ "$HAS_METHOD" -gt 0 ]; then
            p "[2] Document.java — filesPurgedAt 필드 + markFilesPurged() 메서드 확인"
        else
            [ "$HAS_FIELD" -eq 0 ] && f "[2] Document.java — filesPurgedAt 필드 없음"
            [ "$HAS_METHOD" -eq 0 ] && f "[2] Document.java — markFilesPurged() 없음"
        fi
    else
        w "[2] Document.java (entity) 파일을 찾을 수 없음"
    fi

    # [3] DocumentRepository.findPurgeCandidates
    if src_grep "findPurgeCandidates" -l 2>/dev/null | grep -q "DocumentRepository"; then
        p "[3] DocumentRepository.findPurgeCandidates 쿼리 존재"
    else
        f "[3] DocumentRepository.findPurgeCandidates 없음"
    fi

    # [4] DocumentVersionRepository.findByDocumentIdIn
    if src_grep "findByDocumentIdIn" -l 2>/dev/null | grep -q "DocumentVersionRepository"; then
        p "[4] DocumentVersionRepository.findByDocumentIdIn 쿼리 존재"
    else
        f "[4] DocumentVersionRepository.findByDocumentIdIn 없음"
    fi

    # [5] DocumentFileGcService 클래스
    GC_SVC=$(find "$SRC" -name "DocumentFileGcService.java" 2>/dev/null | head -1)
    if [ -n "$GC_SVC" ]; then
        p "[5] DocumentFileGcService.java 존재"
    else
        f "[5] DocumentFileGcService.java 없음"
    fi

    # [6] @Scheduled 새벽 2시 cron
    if [ -n "$GC_SVC" ] && grep -q '@Scheduled.*0 0 2' "$GC_SVC" 2>/dev/null; then
        p "[6] @Scheduled(cron = \"0 0 2 * * *\") 새벽 2시 자동 실행 확인"
    else
        f "[6] DocumentFileGcService에 새벽 2시 cron 설정 없음"
    fi

    # [7] AdminController /admin/gc POST 엔드포인트
    ADMIN_CTRL=$(find "$SRC" -name "AdminController.java" 2>/dev/null | head -1)
    if [ -n "$ADMIN_CTRL" ]; then
        if grep -q 'PostMapping.*gc' "$ADMIN_CTRL" 2>/dev/null; then
            p "[7] AdminController — POST /admin/gc/run 엔드포인트 존재"
        else
            f "[7] AdminController — POST /admin/gc/run 엔드포인트 없음"
        fi
        if grep -q "DocumentFileGcService" "$ADMIN_CTRL" 2>/dev/null; then
            p "[7b] AdminController — DocumentFileGcService 주입 확인"
        else
            f "[7b] AdminController — DocumentFileGcService 미주입"
        fi
    else
        w "[7] AdminController.java를 찾을 수 없음"
    fi

    # [8] gc.html 템플릿
    if [ -f "$TMPL_DIR/gc.html" ]; then
        p "[8] templates/admin/gc.html 존재"
    else
        f "[8] templates/admin/gc.html 없음"
    fi

else
    w "src 디렉터리 접근 불가 — 정적 분석 건너뜀 ($SRC)"
    ((WARN+=7))
fi

# [9] application.yml GC 설정
APP_YML="$SRC/main/resources/application.yml"
if [ -f "$APP_YML" ] && grep -q "retention-days" "$APP_YML" 2>/dev/null; then
    p "[9] application.yml — GC 보존 기간(retention-days) 설정 존재"
else
    f "[9] application.yml — GC 보존 기간(retention-days) 설정 없음"
fi

# ─── DB 런타임 검사 ────────────────────────────────────────────────────────────

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

    # [10] files_purged_at 컬럼 존재
    COL=$(run_psql \
        "SELECT COUNT(*) FROM information_schema.columns
         WHERE table_name='documents' AND column_name='files_purged_at';")
    if [ "${COL:-0}" -ge 1 ]; then
        p "[10] documents.files_purged_at 컬럼 존재"
    else
        f "[10] documents.files_purged_at 컬럼 없음 — V210 마이그레이션 미실행"
    fi

    # [11] GC 부분 인덱스 존재
    IDX=$(run_psql \
        "SELECT COUNT(*) FROM pg_indexes
         WHERE tablename='documents' AND indexname='idx_documents_gc_candidates';")
    if [ "${IDX:-0}" -ge 1 ]; then
        p "[11] idx_documents_gc_candidates 부분 인덱스 존재"
    else
        w "[11] idx_documents_gc_candidates 인덱스 없음 — GC 쿼리 성능 저하 가능"
    fi

    # [12] GC 미처리 건수 조회 (정보성)
    PENDING=$(run_psql \
        "SELECT COUNT(*) FROM documents
         WHERE status='DELETED' AND files_purged_at IS NULL
           AND deleted_at < NOW() - INTERVAL '7 days';" 2>/dev/null || echo "N/A")
    if [ "$PENDING" = "N/A" ]; then
        w "[12] GC 대기 건수 조회 실패"
    elif [ "${PENDING:-0}" -gt 0 ]; then
        w "[12] GC 처리 대기 중인 문서 ${PENDING}건 — /admin/gc에서 수동 실행 가능"
    else
        p "[12] GC 처리 대기 건수 0건 (정상)"
    fi

else
    w "DB 연결 불가 — 런타임 DB 검사 건너뜀 (host=${DB_HOST}:${DB_PORT})"
    ((WARN+=3))
fi

# ─── 결과 ──────────────────────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════"
echo "M3-17 결과: PASS=${PASS}  WARN=${WARN}  FAIL=${FAIL}"
echo "══════════════════════════════════════"
[ "$FAIL" -eq 0 ]
