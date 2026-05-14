# Stage 03 — Acceptance

> PDF §22 3단계 검증 방법

## 자동 검증 (verify.sh)
- [ ] `V2__create_email_verification_tokens.sql` 존재
- [ ] `SecurityConfig.java` 존재 + `csrf` 설정 + `formLogin` 사용 + JWT 라이브러리 import 없음
- [ ] `EmailSender.java` 인터페이스 + `ConsoleEmailSender` 구현 존재
- [ ] `SignupController.java`, `AuthController.java` (또는 동급 클래스) 존재
- [ ] `templates/login.html`, `signup.html`, `signup-verify.html`, `signup-pending.html`, `dashboard.html` 존재
- [ ] `application.yml`에 `RESOURCEHUB_SESSION` 쿠키명, `30m` timeout, `strict` sameSite, `company-email-domain` 키 존재

## 수동 검증
- [ ] `docker compose up -d postgres` 후 애플리케이션 실행
- [ ] `/signup` 접속 → 회사 이메일로 가입 (예: `test@eactive.co.kr`)
- [ ] 일반 메일(`@gmail.com`)로 가입 시도 → 거부
- [ ] 서버 로그에서 6자리 인증 코드 확인
- [ ] `/signup/verify`에서 코드 입력 → 성공 시 `/signup/pending`
- [ ] DB에서 `users.status = PENDING_ADMIN_APPROVAL` 확인
- [ ] `admin@eactive.co.kr`(기본 관리자)로 로그인 → `/dashboard` 도착
- [ ] PENDING/REJECTED 상태 사용자로 로그인 시도 → 거부
- [ ] 로그인 실패 메시지가 너무 구체적이지 않은지 ("로그인할 수 없습니다…")
- [ ] `/logout` → 세션 무효화 + `RESOURCEHUB_SESSION` 쿠키 삭제
- [ ] 인증 안 된 상태로 `/dashboard` 접근 → `/login`으로 리다이렉트
- [ ] CSRF 토큰 없는 POST 요청 거부됨

## NOT-DOING 확인
- [ ] 팀별/개별 폴더 접근 권한 화면 없음
- [ ] 파일 업로드 화면 없음
- [ ] 관리자 승인 처리 화면 없음 (4단계로 미룸)
