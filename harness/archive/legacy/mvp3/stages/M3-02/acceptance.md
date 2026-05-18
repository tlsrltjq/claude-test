# MVP3 M3-02 — Acceptance

## 자동 검증
- [ ] V201 마이그레이션 존재
- [ ] `PasswordResetToken` entity 존재
- [ ] `/login/forgot` 매핑
- [ ] `/login/forgot/verify` 매핑
- [ ] `templates/login.html` 에 rememberEmail input
- [ ] `templates/login.html` 에 RESOURCEHUB_LAST_EMAIL 관련 JS 또는 cookie name 흔적
- [ ] `templates/login-forgot.html`, `login-forgot-verify.html` 존재
- [ ] `templates/signup-verify.html` 에 타이머 JS 흔적 (setTimeout/setInterval/expireAt 등)
- [ ] AuditActionType.RESET_PASSWORD 존재
- [ ] EmailVerificationService 또는 토큰 발급 코드에 5분/300초/Duration.ofMinutes(5) 흔적

## 수동 검증
- [ ] /login 체크박스 체크 + 로그인 성공 → cookie 발급
- [ ] 다음 방문 시 이메일 미리 채워짐, 비번 입력만 하면 됨
- [ ] 체크 해제 + 로그인 → cookie 삭제 확인
- [ ] /login/forgot 이메일 입력 → 콘솔에 인증 코드 출력
- [ ] 코드 + 새 비번 입력 → 정책 위반 거부 / 통과 시 비번 갱신 + /login?reset 이동
- [ ] 새 비번으로 로그인 가능
- [ ] /signup 후 /signup/verify 5분 타이머 동작
- [ ] 4분 지나면 1분 미만 빨간색
- [ ] 만료 시 재발송 버튼 활성, 클릭 시 새 토큰 발급, 이전 토큰은 만료 처리됨 (DB 확인)
- [ ] audit_logs RESET_PASSWORD 기록

## NOT-DOING
- [ ] /signup 폼 순서 변경 없음 (M3-03)
- [ ] /dashboard 변경 없음 (M3-04)

## 회귀
```bash
bash mvp3/harness/scripts/verify.sh M3-02 --with-mvp1 --with-mvp2
```
