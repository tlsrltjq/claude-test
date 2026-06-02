# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V229.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 21/21 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓
- 531개 테스트 전 통과 ✓

## 이전 세션에서 멈춘 곳
2026-06-02: 하네스 정비 완료.

완료 항목:
- chore: HARNESS.md ADR 번호 동기화 (ADR-039→ADR-044, Flyway V218→V230)
- chore: data-model.md Flyway 이력 요약 섹션 제거 (377→332줄, git log로 대체)
- chore: docs/CALENDAR_REDESIGN.md → docs/archive/ 이동

**다음 작업 없음 — 사용자 지시 대기**
