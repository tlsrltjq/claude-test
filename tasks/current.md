# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V227.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 18/18 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓

## 이전 세션에서 멈춘 곳
2026-06-01: 대규모 테스트·품질 개선 세션 완료.

완료 항목:
- feat: 오피스 파일(docx·hwpx·pptx·ppt·xlsx·xls) LibreOffice PDF 변환 미리보기
  - OfficePreviewService — soffice 비동기 변환, 60초 타임아웃, graceful skip
  - Dockerfile Alpine → Debian(jammy) 전환, LibreOffice + 나눔폰트
  - DocumentVersion.isPreviewSupported() 추가
  - hwp·zip 등 미지원 파일 미리보기 버튼 숨김 (5개 템플릿)
  - app.libreoffice.enabled 환경변수 (false 시 skip)
- test: Playwright E2E 테스트 추가 (playwright:1.47.0)
  - 16개 케이스 — 로그인, 네비게이션, 관리자, 접근제어, 로그아웃
  - 실행: ./gradlew playwrightInstall && ./gradlew playwrightTest
- test: Repository DB 통합 테스트 4개 신규
  - AuditLogRepositoryIntegrationTest (LazyInit 회귀 방지 포함)
  - UserRepositoryIntegrationTest (복합 필터, 제약조건)
  - ProjectRepositoryIntegrationTest (@Modifying 상태 전환)
  - ProjectAssignmentRepositoryIntegrationTest (겹침 감지, 일괄 취소)
- test: 단위 테스트 보강 (392→492개, 41개 클래스)
  - DocumentPreviewResolverTest 정비, RedirectUtilsTest, LocalFileStorageTest
  - DocumentVersionTest(isPreviewSupported), OfficePreviewServiceTest
- fix: getDeployStats() findByStatus 전체 로드 → countByStatusAndRoleNot COUNT 쿼리
- 전체 빌드: BUILD SUCCESSFUL, security-lint 18/18 PASS

**다음 작업 없음 — 사용자 지시 대기**
