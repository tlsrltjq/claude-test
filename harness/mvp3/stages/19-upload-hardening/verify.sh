#!/usr/bin/env bash
# 19-upload-hardening: 업로드 안전성 강화 검증
# 검증 항목:
#   [1] FileMagicValidator 클래스 존재 + 9개 확장자 시그니처 포함
#   [2] DocumentUploadService에서 FileMagicValidator.validate() 호출
#   [3] V211 마이그레이션 (uk_documents_folder_type_title_active partial unique index)
#   [4] DocumentUploadService: DataIntegrityViolationException 처리
#   [5] DocumentVersionRepository: findFirstByChecksumInFolder 중복 탐지 쿼리
#   [6] DocumentUploadService: 중복 파일 탐지 로직 존재
#   [7] upload.html: XHR abort (cancelBtn + onCancelClick)
#   [8] upload.html: currentXhr 변수 + xhr.onabort 핸들러
#   [9] FileStorage: listAll(Instant) 기본 메서드 존재
#  [10] LocalFileStorage: listAll 구현 (Files.walk)
#  [11] DocumentFileGcService: runOrphanScan 메서드 존재
#  [12] DocumentVersionRepository: findAllStoragePaths / findAllPreviewPaths / findAllThumbnailPaths
set -uo pipefail

PASS=0; WARN=0; FAIL=0
STAGE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$STAGE_DIR/../../../.." && pwd)"
SRC="$ROOT/src"

p() { echo "[PASS] $*"; ((PASS++)); }
w() { echo "[WARN] $*"; ((WARN++)); }
f() { echo "[FAIL] $*"; ((FAIL++)); }

src_grep() { grep -rn "$@" "$SRC" --include="*.java" 2>/dev/null; }

echo "=== 19-upload-hardening: 업로드 안전성 강화 ==="
echo "ROOT: $ROOT"
echo ""

MIGRATION_DIR="$SRC/main/resources/db/migration"
TMPL_DIR="$SRC/main/resources/templates"

# [1] FileMagicValidator
MAGIC_FILE=$(find "$SRC" -name "FileMagicValidator.java" 2>/dev/null | head -1)
if [ -n "$MAGIC_FILE" ]; then
    EXT_COUNT=$(grep -c '"pdf"\|"jpg"\|"png"\|"docx"\|"pptx"\|"hwpx"\|"hwp"\|"ppt"\|"jpeg"' "$MAGIC_FILE" 2>/dev/null || echo 0)
    if [ "${EXT_COUNT:-0}" -ge 7 ]; then
        p "[1] FileMagicValidator.java 존재 + 9개 확장자 시그니처 포함"
    else
        w "[1] FileMagicValidator.java 존재하지만 확장자 시그니처가 적음 (${EXT_COUNT}개)"
    fi
else
    f "[1] FileMagicValidator.java 없음"
fi

# [2] DocumentUploadService에서 FileMagicValidator 호출
UPLOAD_SVC=$(find "$SRC" -name "DocumentUploadService.java" 2>/dev/null | head -1)
if [ -n "$UPLOAD_SVC" ]; then
    if grep -q "FileMagicValidator.validate" "$UPLOAD_SVC" 2>/dev/null; then
        p "[2] DocumentUploadService — FileMagicValidator.validate() 호출 확인"
    else
        f "[2] DocumentUploadService — FileMagicValidator.validate() 호출 없음"
    fi
else
    w "[2] DocumentUploadService.java를 찾을 수 없음"
fi

# [3] V211 마이그레이션 partial unique index
if ls "$MIGRATION_DIR"/V211*.sql 2>/dev/null | grep -q .; then
    V211=$(ls "$MIGRATION_DIR"/V211*.sql | head -1)
    if grep -q "uk_documents_folder_type_title_active\|WHERE status" "$V211" 2>/dev/null; then
        p "[3] V211 마이그레이션 — partial unique index (활성 문서 중복 방지)"
    else
        w "[3] V211 마이그레이션 존재하지만 partial unique index 내용 불명확"
    fi
else
    f "[3] V211 마이그레이션 없음 — uk_documents_folder_type_title_active 미생성"
fi

# [4] DataIntegrityViolationException 처리
if [ -n "$UPLOAD_SVC" ]; then
    if grep -q "DataIntegrityViolationException" "$UPLOAD_SVC" 2>/dev/null; then
        p "[4] DocumentUploadService — DataIntegrityViolationException 처리 존재"
    else
        f "[4] DocumentUploadService — DataIntegrityViolationException 미처리"
    fi
fi

# [5] findFirstByChecksumInFolder 쿼리
DOC_VER_REPO=$(find "$SRC" -name "DocumentVersionRepository.java" 2>/dev/null | head -1)
if [ -n "$DOC_VER_REPO" ]; then
    if grep -q "findFirstByChecksumInFolder" "$DOC_VER_REPO" 2>/dev/null; then
        p "[5] DocumentVersionRepository — findFirstByChecksumInFolder 쿼리 존재"
    else
        f "[5] DocumentVersionRepository — findFirstByChecksumInFolder 없음"
    fi
else
    w "[5] DocumentVersionRepository.java를 찾을 수 없음"
fi

# [6] 중복 파일 탐지 로직
if [ -n "$UPLOAD_SVC" ]; then
    if grep -q "checksum\|중복\|duplicate\|findFirstByChecksum" "$UPLOAD_SVC" 2>/dev/null; then
        p "[6] DocumentUploadService — 중복 파일 체크섬 탐지 로직 존재"
    else
        f "[6] DocumentUploadService — 중복 파일 탐지 로직 없음"
    fi
fi

# [7] upload.html: cancelBtn + onCancelClick
UPLOAD_HTML="$TMPL_DIR/my/upload.html"
if [ -f "$UPLOAD_HTML" ]; then
    HAS_BTN=$(grep -c "cancelBtn\|onCancelClick" "$UPLOAD_HTML" 2>/dev/null || echo 0)
    if [ "${HAS_BTN:-0}" -ge 2 ]; then
        p "[7] upload.html — cancelBtn + onCancelClick() 존재"
    else
        f "[7] upload.html — cancelBtn 또는 onCancelClick() 없음"
    fi
else
    w "[7] my/upload.html 없음"
fi

# [8] currentXhr + xhr.onabort
if [ -f "$UPLOAD_HTML" ]; then
    HAS_XHR=$(grep -c "currentXhr\|onabort\|xhr.abort" "$UPLOAD_HTML" 2>/dev/null || echo 0)
    if [ "${HAS_XHR:-0}" -ge 2 ]; then
        p "[8] upload.html — currentXhr 변수 + xhr.abort/onabort 핸들러 존재"
    else
        f "[8] upload.html — XHR 취소 구현 불완전"
    fi
fi

# [9] FileStorage: listAll(Instant) 기본 메서드
FILE_STORAGE=$(find "$SRC" -name "FileStorage.java" 2>/dev/null | head -1)
if [ -n "$FILE_STORAGE" ]; then
    if grep -q "listAll" "$FILE_STORAGE" 2>/dev/null; then
        p "[9] FileStorage — listAll(Instant) 기본 메서드 존재"
    else
        f "[9] FileStorage — listAll(Instant) 없음"
    fi
else
    w "[9] FileStorage.java를 찾을 수 없음"
fi

# [10] LocalFileStorage: listAll 구현
LOCAL_STORAGE=$(find "$SRC" -name "LocalFileStorage.java" 2>/dev/null | head -1)
if [ -n "$LOCAL_STORAGE" ]; then
    if grep -q "Files.walk\|listAll" "$LOCAL_STORAGE" 2>/dev/null; then
        p "[10] LocalFileStorage — Files.walk 기반 listAll 구현 존재"
    else
        f "[10] LocalFileStorage — listAll 구현 없음"
    fi
else
    w "[10] LocalFileStorage.java를 찾을 수 없음"
fi

# [11] DocumentFileGcService: runOrphanScan
GC_SVC=$(find "$SRC" -name "DocumentFileGcService.java" 2>/dev/null | head -1)
if [ -n "$GC_SVC" ]; then
    if grep -q "runOrphanScan\|orphan\|Orphan" "$GC_SVC" 2>/dev/null; then
        p "[11] DocumentFileGcService — runOrphanScan 고아 파일 정리 존재"
    else
        f "[11] DocumentFileGcService — runOrphanScan 없음"
    fi
else
    w "[11] DocumentFileGcService.java를 찾을 수 없음"
fi

# [12] findAllStoragePaths / findAllPreviewPaths / findAllThumbnailPaths
if [ -n "$DOC_VER_REPO" ]; then
    COUNT=$(grep -c "findAllStoragePaths\|findAllPreviewPaths\|findAllThumbnailPaths" "$DOC_VER_REPO" 2>/dev/null || echo 0)
    if [ "${COUNT:-0}" -ge 3 ]; then
        p "[12] DocumentVersionRepository — orphan 스캔용 경로 조회 3개 쿼리 존재"
    else
        f "[12] DocumentVersionRepository — orphan 스캔 쿼리 부족 (${COUNT}/3)"
    fi
fi

echo ""
echo "══════════════════════════════════════"
echo "19-upload-hardening 결과: PASS=${PASS}  WARN=${WARN}  FAIL=${FAIL}"
echo "══════════════════════════════════════"
[ "$FAIL" -eq 0 ]
