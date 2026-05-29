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
2026-05-29: 테스트 스위트 302개 구축 완료.

완료 항목:
- fix: 문서 검색 500 에러 — PostgreSQL null LocalDateTime JDBC 타입 추론 실패, sentinel(1970/9999) 패턴으로 수정
- test: SearchServiceTest 16개 (단위) — ADMIN/EMPLOYEE 라우팅·folderKind 필터·날짜 sentinel·kw 변환·중복 제거
- test: DocumentUploadServiceTest 7개 (단위) — 확장자·magic bytes·신규 업로드·대용량 검토 플래그·중복 체크섬
- test: RouteSecurityTest 12개 (WebMvc 슬라이스) — 미인증 302·EMPLOYEE/SALES/ADMIN 권한 매트릭스
- test: DocumentRepositoryIntegrationTest 11개 (Testcontainers PostgreSQL) — sentinel 날짜·키워드·타입 필터·날짜 범위
- build.gradle: Testcontainers 1.21.0 의존성 3개 추가
- application-test.yml: Flyway 비활성화, ddl-auto: create, stub mail/S3
- @Import(JpaAuditingConfig.class) — @DataJpaTest 슬라이스에서 @CreatedDate 동작 보장

**다음 작업 없음 — 사용자 지시 대기**
