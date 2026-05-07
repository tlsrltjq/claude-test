# MVP3 M3-02 — Deliverables

## SQL
- `V201__create_password_reset_tokens.sql`

## Backend
- `user/entity/PasswordResetToken.java`
- `user/repository/PasswordResetTokenRepository.java`
- `user/service/PasswordResetService.java`
- `user/controller/PasswordResetController.java` — `/login/forgot`, `/login/forgot/verify`
- `user/controller/LoginCookieHandler.java` 또는 `AuthenticationSuccessHandler` — RESOURCEHUB_LAST_EMAIL cookie 처리
- `user/service/EmailVerificationService.java` 갱신 — TTL 5분, 신규 발급 시 이전 토큰 invalidate
- `audit/entity/AuditActionType.RESET_PASSWORD` 추가

## Templates
- `templates/login.html` 갱신 (체크박스 + JS)
- `templates/login-forgot.html`
- `templates/login-forgot-verify.html`
- `templates/signup-verify.html` 타이머 추가
- (선택) `static/js/verify-timer.js`
