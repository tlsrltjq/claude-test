# 보안·권한 정책

> harness/mvp2/stages/14-security-policy/verify.sh 로 자동 검증 가능.

## 1. 파일 저장

- 저장 파일명: `UUID.ext` (사용자 입력 파일명 그대로 저장 금지)
- `originalFileName` / `storedFileName` / `storagePath` 는 DB(`document_versions`)에만 보관
- 저장 경로: 환경변수 `RESOURCEHUB_UPLOAD_BASE_DIR` 기반 — 하드코딩 금지
- 사용자가 저장 경로를 직접 지정할 수 없음 (업로드 API에 path 파라미터 없음)

## 2. 업로드 보안

- 허용 확장자: `application.yml` `file.allowed-extensions` 에 명시 (현재: pdf, jpg, jpeg, png, docx, hwp, hwpx, ppt, pptx)
- 검증 위치: `DocumentUploadService.validateFile()` — 컨트롤러 아님
- 위험 확장자 (exe, sh, bat, cmd, php, jsp) whitelist 포함 금지

## 3. 권한 정책

- 모든 파일 접근(다운로드·미리보기·썸네일·삭제)은 `DocumentAccessService.getVersionWithAccessCheck()` 경유
- 프론트엔드 메뉴 숨김만으로 처리 금지 — 서비스 레이어에서 역할 검사 수행
- `FolderAccessService` / `DocumentAccessService` 에 SALES 전사 read-only 분기 포함

## 4. 다운로드 / 미리보기

- `InputStreamResource` 스트리밍 — 정적 URL 직접 공개 금지
- `Content-Disposition` 헤더 필수
- `storagePath` / `thumbnailStoragePath` 를 응답 JSON에 노출 금지
- `resources/static/` 하위에 업로드 디렉터리 없음

## 5. 삭제 정책 (TODO — 현재 미구현)

> **현재 hard delete 사용 중 — 아래가 목표 정책**

- soft delete: `deleted_at`, `deleted_by` 컬럼 추가 (Flyway 마이그레이션 필요)
- `Document` 엔티티: `@SQLDelete` + `@Where(clause="deleted_at IS NULL")`
- 물리 파일은 배치 또는 별도 cleanup 작업으로 지연 삭제
- 삭제 감사 로그(`DELETE_DOCUMENT`) 는 이미 구현됨

## 6. 로그 정책 (TODO — 일부 미준수)

**준수**
- 업로드·다운로드·삭제 이벤트: `AuditLog` 테이블 기록
- DB 비밀번호: 환경변수 기반 (`SPRING_DATASOURCE_PASSWORD`)
- 감사 로그에 비밀번호·주민번호 등 민감 정보 기록 금지

**미준수 (수정 필요)**
- `LocalFileStorage.java:28` — `log.debug("파일 저장: {}", target.toAbsolutePath())` → 절대 경로 제거
- `application.yml:53` — `RESOURCEHUB_ADMIN_PASSWORD:Admin1234!` → 하드코딩 기본값 제거
