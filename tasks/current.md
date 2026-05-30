# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V226.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 15/15 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓

## 이전 세션에서 멈춘 곳
2026-05-30: 테스트 스위트 393개 구축 완료 + 전체 문서 최신화 완료.

완료 항목:
- test: E2ETest.java 단일 클래스 24케이스 — Auth(9)·Admin(8)·Document(7), application-e2e.yml 신설
- test: SignupServiceTest 16케이스, EmployeeManagementServiceTest 12케이스
- test: ThumbnailServiceTest 7케이스, DocumentFileGcServiceTest 10케이스
- test: DocumentExpiryServiceTest 6케이스, FolderPermissionServiceTest 8케이스
- test: AuditServiceTest 5케이스, FolderServiceTest 3케이스
- docs: HARNESS.md·tasks/current.md·CHANGELOG.md·testing.md·architecture.md·data-model.md·decisions.md 최신화
- 전체 빌드: 393개 전 통과, security-lint 15/15 PASS

**다음 작업: 미사용 코드 GC (사용자 지시)**
