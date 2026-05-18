# 보안·권한 정책

> `bash scripts/security-lint.sh` 로 자동 검증 가능 (15개 항목).

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

## 5. 삭제 정책 ✅ 구현 완료

- soft delete: `deleted_at`, `deleted_by` 컬럼 — V210 Flyway 마이그레이션 적용
- `Document` 엔티티: `delete(actorId)` 호출 → `deleted_at` / `deleted_by` 세트 (hard delete 없음)
- 물리 파일 지연 삭제: `DocumentFileGcService` `@Scheduled(cron="0 0 2 * * *")` — 매일 새벽 2시 실행
- 삭제 감사 로그(`DELETE_DOCUMENT`): `AuditLogService.logDeleteDocument()` 경유 기록
- GC 대시보드: `/admin/gc`

## 6. 로그 정책

**준수**
- 업로드·다운로드·삭제 이벤트: `AuditLog` 테이블 기록
- DB 비밀번호: 환경변수 기반 (`SPRING_DATASOURCE_PASSWORD`)
- 감사 로그에 비밀번호·주민번호 등 민감 정보 기록 금지

**완료**
- ~~`LocalFileStorage.java:28` — `log.debug("파일 저장: {}", target.toAbsolutePath())`~~ → `storedFileName`(UUID)만 로깅으로 수정 완료
- ~~`application.yml:53` — `RESOURCEHUB_ADMIN_PASSWORD:Admin1234!`~~ → 하드코딩 기본값 제거 완료 (`security-lint.sh [15]` PASS)
- ~~`PasswordResetService.java:52` — `log.info("... code={}", code)`~~ → 재설정 코드 로그 제거 완료 (`security-lint.sh [14]` PASS)

## 7. HTTP 보안 헤더 ✅ 구현 완료

> `SecurityConfig.securityFilterChain()` 의 `.headers()` 블록에 추가됨.

| 헤더 | 값 |
|------|----|
| `X-Frame-Options` | `DENY` |
| `X-Content-Type-Options` | `nosniff` |
| `Referrer-Policy` | `strict-origin-when-cross-origin` |
| `Content-Security-Policy` | `default-src 'self'; script-src 'self' cdn.jsdelivr.net 'unsafe-inline'; style-src 'self' cdn.jsdelivr.net 'unsafe-inline'; img-src 'self' data:; font-src 'self' cdn.jsdelivr.net` |

- HSTS는 프로덕션에서만 (`secure: true` 전환 시 함께 활성화)
- `scripts/security-lint.sh [13]` PASS 확인

## 8. 환경변수 기본값 정책

- `application.yml` 에서 비밀번호·시크릿 계열 환경변수에 하드코딩 기본값 **금지**
  - ❌ `password: ${RESOURCEHUB_ADMIN_PASSWORD:Admin1234!}`
  - ✅ `password: ${RESOURCEHUB_ADMIN_PASSWORD}` — 미설정 시 앱 기동 실패로 명시적 오류 발생
- `scripts/security-lint.sh [15]` 로 자동 검사

## 9. 비동기 처리 정책

- 썸네일 생성(`ThumbnailService.generateAndSave()`)은 업로드 응답을 지연시키지 않도록 `@Async` 필수
- `@EnableAsync` 선언 위치: `EactiveResourceHubApplication` 또는 별도 `AsyncConfig`
- `@Async` 메서드는 `void` 또는 `CompletableFuture` 반환 — 트랜잭션 전파 주의
