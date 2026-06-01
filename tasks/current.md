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
2026-06-01: 개발자 등급 DB 정비 완료.

완료 항목:
- chore: employee_profiles.developer_grade DB 정비
  · JUNIOR(27) → 초급
  · INTERMEDIATE(29) → 중급
  · SENIOR(14) → 고급(career_months < 96) / 특급(career_months ≥ 96) 분리
  · 경영본부 NULL(13) → user_id % 5 기준 특급/고급/중급 랜덤 배분
  · 최종: 특급 12명 / 고급 18명 / 중급 37명 / 초급 34명 / NULL 1명(팀 없는 계정)
- 코드 변경 없음, DB 직접 UPDATE

**다음 작업 없음 — 사용자 지시 대기**
