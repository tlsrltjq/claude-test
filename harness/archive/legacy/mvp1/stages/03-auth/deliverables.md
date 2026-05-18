# Stage 03 — Deliverables

## SQL
- `src/main/resources/db/migration/V2__create_email_verification_tokens.sql`

## Configuration
- `common/security/SecurityConfig.java` — Spring Security 세션 설정 (CSRF on, JWT/Remember-me off, 정적+공개 경로 허용, 쿠키 설정)
- `application.yml`
  - `server.servlet.session.timeout: 30m`
  - `server.servlet.session.cookie.name: RESOURCEHUB_SESSION`
  - `server.servlet.session.cookie.http-only: true`
  - `server.servlet.session.cookie.same-site: strict`
  - `resourcehub.company-email-domain: eactive.co.kr`

## Email
- `common/email/EmailSender.java` (interface)
- `common/email/ConsoleEmailSender.java` (로컬 구현)

## Controllers / Services
- `user/controller/SignupController.java` — `/signup`, `/signup/verify`, `/signup/pending`
- `user/controller/AuthController.java` (또는 LoginController) — `/login`, `/logout`, `/dashboard`
- `user/service/SignupService.java`, `EmailVerificationService.java`
- `user/service/AdminBootstrapper.java` 또는 `ApplicationRunner` — 기본 관리자 자동 생성

## Views (Thymeleaf)
- `templates/signup.html`, `templates/signup-verify.html`, `templates/signup-pending.html`
- `templates/login.html`
- `templates/dashboard.html`

## Repositories (추가)
- `user/repository/EmailVerificationTokenRepository.java`
