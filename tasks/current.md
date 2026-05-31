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
2026-06-01: 전체 테스트·린트 점검 및 보안 강화 완료.

완료 항목:
- 전체 테스트 393개 통과 (37개 클래스, failures/errors/skipped 모두 0)
- 보안 린트 15→18항목 확장
  - #16 safeReferer 중복 구현 감지 → RedirectUtils로 통합
  - #17 LocalFileStorage 경로 탈출 방어(resolveAndValidate) 추가
  - #18 광범위 예외(Exception/RuntimeException) catch 내 e.getMessage() 모델 노출 감지 (Python 헬퍼)
- 문의 이메일 하드코딩 제거 → RESOURCEHUB_CONTACT_EMAIL 환경변수로 치환, 미설정 시 UI 비노출
- 전체 빌드: BUILD SUCCESSFUL, security-lint 18/18 PASS

**다음 작업 없음 — 사용자 지시 대기**
