#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
YMLS="$RES/application.yml"
DB="$RES/db/migration"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }
check_fail() { local d="$1"; shift; if ! "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d (정책 위반 발견됨)"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 14 — 권한·보안 정책 검사 =="
echo

# ─────────────────────────────────────────────
echo "[ 1. 파일 저장 정책 ]"
# storedFileName = UUID 기반
check "UUID 기반 storedFileName 생성"       grep -q 'randomUUID' "$SRC/document/service/DocumentUploadService.java"
check "originalFileName DB 저장"            grep -q 'originalFileName' "$SRC/document/entity/DocumentVersion.java"
check "storedFileName DB 저장"              grep -q 'storedFileName' "$SRC/document/entity/DocumentVersion.java"
check "storagePath DB 저장"                 grep -q 'storagePath' "$SRC/document/entity/DocumentVersion.java"
# 저장 경로: 설정 기반 (환경변수 주입)
check "base-dir 설정 기반 (환경변수)"       grep -q 'RESOURCEHUB_UPLOAD_BASE_DIR' "$YMLS"
# 사용자가 저장 경로를 직접 지정하지 못함 (업로드 컨트롤러에 path 파라미터 없음)
check_fail "업로드 컨트롤러에 path 직접 지정 파라미터 없음" \
  bash -c "grep -q '@RequestParam.*path\|@RequestParam.*dir\|@RequestParam.*folder.*path' '$SRC/document/controller/DocumentController.java'"

echo

# ─────────────────────────────────────────────
echo "[ 2. 업로드 보안 ]"
# 허용 확장자 whitelist 설정 존재
check "application.yml: allowed-extensions 설정"  grep -q 'allowed-extensions' "$YMLS"
# 서비스 레이어에서 파일 검증 (컨트롤러가 아닌 서비스에서)
check "DocumentUploadService: validateFile 메서드" grep -q 'validateFile' "$SRC/document/service/DocumentUploadService.java"
check "DocumentUploadService: 허용 확장자 목록 로드" grep -q 'allowedExtensions\|allowed-extensions' "$SRC/document/service/DocumentUploadService.java"
# 위험 확장자 (exe, sh, bat, cmd, php, jsp) whitelist에 포함되지 않음
check_fail "exe whitelist 포함 여부"  bash -c "grep -E 'allowed-extensions.*exe' '$YMLS'"
check_fail "sh/bat whitelist 포함 여부" bash -c "grep -E 'allowed-extensions.*(sh|bat|cmd)' '$YMLS'"
check_fail "php/jsp whitelist 포함 여부" bash -c "grep -E 'allowed-extensions.*(php|jsp)' '$YMLS'"

echo

# ─────────────────────────────────────────────
echo "[ 3. 권한 정책 ]"
# 서비스 레이어 접근 검사 빈 존재
check "DocumentAccessService 존재"    [ -f "$SRC/document/service/DocumentAccessService.java" ]
check "FolderAccessService 존재"      [ -f "$SRC/document/service/FolderAccessService.java" ]
# 다운로드·미리보기·썸네일 엔드포인트 모두 accessService 주입
check "DocumentController: accessService 의존성"  grep -q 'DocumentAccessService\|accessService' "$SRC/document/controller/DocumentController.java"
# accessService.getVersionWithAccessCheck (or similar) 호출이 파일 I/O 전에 수행됨
check "download 전 접근 검사 호출"    grep -q 'getVersionWithAccessCheck\|checkAccess\|canRead' "$SRC/document/controller/DocumentController.java"
# SALES role: 서버 권한 검사 (FolderAccessService에 SALES 분기 존재)
check "FolderAccessService: SALES 분기"    grep -q 'SALES' "$SRC/document/service/FolderAccessService.java"
check "DocumentAccessService: SALES 분기"  grep -q 'SALES' "$SRC/document/service/DocumentAccessService.java"

echo

# ─────────────────────────────────────────────
echo "[ 4. 다운로드 정책 ]"
# Spring API 경유 (InputStreamResource 또는 StreamingResponseBody)
check "download: InputStreamResource 사용"    grep -q 'InputStreamResource' "$SRC/document/controller/DocumentController.java"
check "download: Content-Disposition 헤더 설정" grep -q 'Content-Disposition\|contentDisposition\|attachment' "$SRC/document/controller/DocumentController.java"
# 정적 파일 URL 직접 공개 금지: resources/static 하위에 업로드 디렉터리 없음
check_fail "static/ 하위 uploads 디렉터리 없음" bash -c "[ -d '$RES/static/uploads' ] || [ -d '$RES/static/storage' ]"
# storagePath를 응답 JSON/HTML에 직접 노출하지 않음
check_fail "API 응답에 storagePath 직접 반환"   grep -q 'storagePath' "$SRC/document/dto/DocumentVersionResponse.java" 2>/dev/null

echo

# ─────────────────────────────────────────────
echo "[ 5. 미리보기·썸네일 정책 ]"
# preview/thumbnail도 동일한 접근 검사 적용
check "preview: accessService 호출"     bash -c "grep -A10 'preview\|@GetMapping.*preview' '$SRC/document/controller/DocumentController.java' | grep -q 'accessService\|getVersionWithAccessCheck'"
check "thumbnail: accessService 호출"   bash -c "grep -A10 'thumbnail' '$SRC/document/controller/DocumentController.java' | grep -q 'accessService\|getVersionWithAccessCheck'"
check "thumbnail: InputStreamResource"  bash -c "grep -A10 'thumbnail' '$SRC/document/controller/DocumentController.java' | grep -q 'InputStreamResource'"
# 썸네일이 정적 URL로 직접 노출되지 않음 (컨트롤러 통과)
check_fail "thumbnailStoragePath DTO 직접 노출" grep -q 'thumbnailStoragePath' "$SRC/document/dto/DocumentVersionResponse.java" 2>/dev/null

echo

# ─────────────────────────────────────────────
echo "[ 6. 삭제 정책 ]"
# soft delete 구현 여부 (deleted_at, deleted_by 컬럼)
# 현재 구현: hard delete — 아래 두 항목은 FAIL 예상
check "Document 엔티티: deletedAt 필드"   grep -q 'deletedAt\|deleted_at' "$SRC/document/entity/Document.java"
check "Document 엔티티: deletedBy 필드"   grep -q 'deletedBy\|deleted_by' "$SRC/document/entity/Document.java"
check_fail "DocumentDeleteService: 즉시 물리 삭제 (hard delete)" \
  bash -c "grep -q 'documentRepository\.delete\b' '$SRC/document/service/DocumentDeleteService.java'"
# 삭제 감사 로그 기록
check "AuditActionType: DELETE_DOCUMENT"  grep -q 'DELETE_DOCUMENT' "$SRC/audit/entity/AuditActionType.java"
check "DocumentDeleteService: 감사 로그 호출" grep -q 'auditService\|audit' "$SRC/document/service/DocumentDeleteService.java"

echo

# ─────────────────────────────────────────────
echo "[ 7. 로그 정책 ]"
# 절대 경로 로그 금지
check_fail "LocalFileStorage: toAbsolutePath() 로그 노출" \
  bash -c "grep -q 'toAbsolutePath()' '$SRC/common/file/LocalFileStorage.java'"
# 설정 기반 관리자 초기 비밀번호 (하드코딩 기본값 금지)
# ${RESOURCEHUB_ADMIN_PASSWORD:Admin1234!} 형태는 fallback 하드코딩으로 FAIL
check_fail "application.yml: 관리자 비밀번호 하드코딩 기본값" \
  bash -c "grep -qE 'RESOURCEHUB_ADMIN_PASSWORD:[A-Za-z0-9!@#\$%^&*]+' '$YMLS'"
# 비밀번호를 감사 로그에 기록하지 않음
check_fail "감사 로그에 password 필드 기록" \
  bash -c "grep -q 'password' '$SRC/audit/service/AuditService.java' 2>/dev/null && grep -q 'log\.' '$SRC/audit/service/AuditService.java'"
# 업로드·다운로드 감사 이벤트 타입 존재
check "AuditActionType: UPLOAD_DOCUMENT 또는 UPLOAD" \
  bash -c "grep -qE 'UPLOAD_DOCUMENT|UPLOAD_FILE|UPLOAD' '$SRC/audit/entity/AuditActionType.java'"
check "AuditActionType: DOWNLOAD_DOCUMENT 또는 DOWNLOAD" \
  bash -c "grep -qE 'DOWNLOAD_DOCUMENT|DOWNLOAD_FILE|DOWNLOAD' '$SRC/audit/entity/AuditActionType.java'"
# DB 비밀번호도 환경변수 기반
check "DB 비밀번호: 환경변수 기반"  grep -q 'SPRING_DATASOURCE_PASSWORD' "$YMLS"

echo; echo "  passed: $PASS  failed: $FAIL"
[ "$FAIL" -eq 0 ]
