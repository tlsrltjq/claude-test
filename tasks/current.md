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
2026-05-30: 테스트·문서·GC·사이드바 세션 완료.

완료 항목:
- test: E2ETest.java 단일 클래스 24케이스, 단위 테스트 8클래스 추가 — 393개 전 통과
- docs: 전체 문서(HARNESS·testing·architecture·data-model·decisions) V227 기준 최신화
- refactor: dead code GC — CertificateService.getTemplates(), SalesProfileExporter byte[] 오버로드 2개, ProfileRow 미사용 메서드 2개 제거
- feat: 사이드바 관리자 메뉴 추가 — 팀 프로젝트 설정(/admin/teams/project-settings), 만료 문서(/admin/documents/expiry)
- 전체 빌드: 393개 전 통과, security-lint 15/15 PASS

**다음 작업 없음 — 사용자 지시 대기**
