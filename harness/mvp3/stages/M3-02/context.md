# MVP3 M3-02 — Context

## SSOT
DECISIONS D-07, D-08, D-12.

## 이전 결과
- mvp2 03 회원가입 인증 흐름 동작 (10분 TTL)
- 비밀번호 정책 (영/숫/특수 3종 + 8자) 적용 중

## 핵심 제약
- Remember-me 금지 — ID 저장은 단순 cookie
- 비밀번호 평문 메일 X — 인증코드 + 새 비번 입력
- 5분 타이머는 클라이언트 JS, 서버 만료는 expired_at 컬럼

## 코드 위치
- `V201__create_password_reset_tokens.sql`
- `user/entity/PasswordResetToken.java`, repository
- `user/service/PasswordResetService.java`
- `user/controller/PasswordResetController.java` — `/login/forgot/**`
- `templates/login.html` (체크박스 + JS), `login-forgot.html`, `login-forgot-verify.html`
- `templates/signup-verify.html` (타이머 JS)
- `static/js/login-cookie.js`, `static/js/verify-timer.js` (선택)
