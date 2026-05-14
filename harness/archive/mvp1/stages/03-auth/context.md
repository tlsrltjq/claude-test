# Stage 03 — Context

## SSOT
`docs/PROJECT_SPEC.md` §5.1~§5.2, §20 보안 기준, §25 절대 원칙.

## 이전 단계 결과
- V1 마이그레이션 + 8개 도메인 Entity/Repository.
- `users.status` enum에는 최소 `PENDING_EMAIL_VERIFICATION`, `PENDING_ADMIN_APPROVAL`, `ACTIVE`, `REJECTED`, `DISABLED`가 들어가야 함 (없으면 V1 손보거나 V2.5에 enum 확장).

## 이번 단계 핵심 제약
- **JWT 절대 사용 금지.** Spring Security 세션 인증.
- CSRF on, Remember-me off.
- 세션 timeout 30분, 쿠키 `RESOURCEHUB_SESSION` httpOnly+sameSite strict.
- 인증 코드 6자리 숫자, TTL 10분.
- 로컬 EmailSender = `ConsoleEmailSender` (콘솔에 코드 출력).
- 회사 도메인은 `resourcehub.company-email-domain` 설정값으로.
- 기본 관리자는 `RESOURCEHUB_ADMIN_EMAIL/PASSWORD` 환경변수 또는 application.yml.
- README에 운영 시 secure=true 필요 명시.

## 코드가 들어갈 위치
- 새 마이그레이션: `src/main/resources/db/migration/V2__create_email_verification_tokens.sql`
- Service/Controller는 `user/`, `common/security/` 아래.
- `EmailSender` 인터페이스는 `common/email/` (없으면 만들어도 됨).
