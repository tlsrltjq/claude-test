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
2026-06-02: 보안 취약점 점검 및 수정 완료.

완료 항목:
- feat: LoginAttemptService — 10회 실패 시 카운터 리셋 후 /login/forgot?toomany 유도
- feat: LoginFailureHandler·LoginSuccessHandler — 실패/성공 시 카운터 연동
- feat: ForgotPasswordController — 5회 실패 시 토큰 무효화, 남은 횟수 안내
- feat: SignupController — 5회 실패 시 redirect:/signup?toomany (loadFormModel 누락 버그도 수정)
- feat: PasswordResetService.invalidateCurrentToken() — 5회 초과 시 DB 토큰 강제 만료
- feat: EmailAllowlistService.addBulkFromExcel — 확장자·매직바이트·1,000행 삼중 검증
- chore: SampleDataFixRunner @Profile("!prod") — 프로덕션 격리
- feat: CspNonceFilter·CspNonceHeaderWriter·CspNonceInterceptor·WebMvcConfig — CSP nonce 인프라
- chore: SecurityConfig — 정적 CSP → CspNonceHeaderWriter 동적 헤더
- chore: 템플릿 33개 인라인 script 41개 → th:attr="nonce=${cspNonce}"
- chore: login-forgot.html ?toomany 안내, signup.html ?toomany 안내
- chore: security-lint.sh 항목 18→21개 확장 ([19] nonce누락·[20] unsafe-inline·[21] SampleDataRunner)
- test: LoginAttemptServiceTest 8개, EmailAllowlistServiceTest 엑셀검증 3개
- test: ForgotPasswordControllerTest 3개, SignupControllerVerifyTest 3개
- docs: HARNESS.md·SECURITY_AND_PERMISSION.md·decisions.md(ADR-040~044)·testing.md·CHANGELOG.md 최신화

**다음 작업 없음 — 사용자 지시 대기**
