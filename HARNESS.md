# eActive Resource Hub

## 한 줄 설명
이액티브 사내 직원·문서·인력 관리 포털. 회원가입·문서 업로드/검토·공용 폴더·영업 인력표·경력 계산기·재직증명서 자동 발급까지 한 곳에서 처리.

## 기술 스택
Java 21 / Spring Boot 3.5 / Gradle / PostgreSQL 18 + Flyway / Thymeleaf + Bootstrap 5 / Spring Security 세션. 운영은 Caddy(HTTPS) + Docker Compose. 상세는 `build.gradle`, `application.yml`, `docs/architecture.md`.

## 디렉토리 구조 (핵심만)
- `src/main/java/com/eactive/resourcehub/` — 도메인별(`user`, `document`, `team`, `permission`, `audit`, `template`, `employee`, `certificate`, `common`)
- `src/main/resources/templates/` — Thymeleaf (`admin/`, `sales/`, `my/`, `shared/`)
- `src/main/resources/db/migration/` — Flyway V1~V213 (다음은 V214부터)
- `certificate/` — 재직증명서 Python+Flask 서비스 (별도 Docker)
- `scripts/` — `security-lint.sh`, `deploy.sh`, `backup-*.sh`, `setup-cron.sh`
- `docs/`, `harness/archive/legacy/`, `사용법/`

## 현재 상태
운영 배포 준비 완료(MVP1·MVP2·MVP3·post-MVP3·19~21단계). 0단계(하네스 전면 개편) 완료. 소스 기준 문서 정합성 감사 완료(불일치 4건 수정, security-lint 15/15 PASS). 다음: 운영 도메인 배포(`scripts/deploy.sh`) 또는 신규 기능 — 새 작업 시작 시 `tasks/current.md` 덮어쓰기.

## 코딩 규칙
- JWT 금지(세션만), Remember-me 금지, CSRF 항상 활성화
- 파일은 컨트롤러 경유만(정적 노출 금지), 저장명 UUID, 원본명은 DB
- 권한 검사는 Service(`DocumentAccessService` / `FolderAccessService`), 컨트롤러에서 `role` 직접 비교·Repository 직접 주입 금지
- 스키마 변경은 Flyway만 (`ddl-auto: validate`), 감사 로그는 `REQUIRES_NEW`
- 화면 역할 표기는 한글(관리자/영업/사원), enum은 영문(`ADMIN/SALES/EMPLOYEE`)
- 커밋 접두어: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`
- 결정 근거는 `docs/decisions.md` (ADR-001~035)

## 절대 건드리면 안 되는 것
`src/main/resources/db/migration/V*.sql`(적용 번호), `SecurityConfig.java`, `.env`·`.env.example`, `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`, `harness/archive/legacy/**`

## 참고 문서
- `docs/architecture.md` — 패키지·라우트·DB·운영 인프라 상세
- `docs/decisions.md` — 기술 결정 ADR
- `tasks/current.md` — 현재 단계 작업 컨텍스트
- `CHANGELOG.md` — 변경 이력(한 줄 누적)
- `docs/archive/`, `harness/archive/legacy/` — 옛 자료
