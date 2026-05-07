# MVP3 M3-02 — Prompt

MVP3 2단계.

작업 묶음 3개:
1. **/login ID 저장 체크박스 + cookie**
2. **/login/forgot 비밀번호 찾기** (인증코드 + 새 비번 입력)
3. **/signup/verify 5분 인증 타이머** + 토큰 정책

중요:
- Remember-me 사용 금지(MVP1 원칙) — ID 저장은 단순 cookie로 이메일만 저장.
- 비밀번호 평문 메일 발송 X — 인증코드 → 새 비번 입력 화면.
- 비밀번호 정책(영/숫/특수 3종 + 8자) 그대로 적용 (mvp2 03).

요구사항:

## 1. /login ID 저장 (D-08)

- 화면에 체크박스 "이메일 기억하기".
- 체크 + 로그인 성공 시:
  - cookie `RESOURCEHUB_LAST_EMAIL` 발급 (Max-Age 30일, sameSite=strict, httpOnly=false, secure=false 로컬)
  - 값은 이메일 그대로
- 체크 안 하고 로그인 시: cookie가 있으면 삭제 (Max-Age=0)
- 페이지 진입 시 작은 JS:
  ```js
  const em = document.cookie.split('; ').find(r => r.startsWith('RESOURCEHUB_LAST_EMAIL='));
  if (em) {
    document.querySelector('input[name="username"]').value = decodeURIComponent(em.split('=')[1]);
    document.querySelector('input[name="rememberEmail"]').checked = true;
    document.querySelector('input[name="password"]').focus();
  }
  ```
- Spring 측에서는 폼 파라미터 `rememberEmail` (true/false) 받아 cookie 처리.
- `LoginCookieFilter` 또는 `AuthenticationSuccessHandler` 어디에 두든 OK.

## 2. /login/forgot 비밀번호 찾기 (D-07)

- Flyway `V201__create_password_reset_tokens.sql`
  ```sql
  CREATE TABLE password_reset_tokens (
      id BIGSERIAL PRIMARY KEY,
      user_id BIGINT NOT NULL REFERENCES users(id),
      email VARCHAR(255) NOT NULL,
      token VARCHAR(10) NOT NULL,        -- 6자리 코드
      expired_at TIMESTAMPTZ NOT NULL,
      verified_at TIMESTAMPTZ,
      consumed_at TIMESTAMPTZ,            -- 새 비번 설정 후 사용처리
      created_at TIMESTAMPTZ NOT NULL DEFAULT now()
  );
  CREATE INDEX idx_prt_user ON password_reset_tokens(user_id);
  CREATE INDEX idx_prt_email ON password_reset_tokens(email);
  ```
- 화면 흐름:
  1. `GET /login/forgot` — 이메일 입력 폼 (이메일 앞부분 + @eactive.co.kr suffix)
  2. `POST /login/forgot` — 사용자 존재 확인 → 6자리 코드 발급 → 이메일 발송 (ConsoleEmailSender 로컬) → 5분 유효
  3. `GET /login/forgot/verify?email=...` — 6자리 코드 + 새 비번 + 새 비번 확인 입력 폼
  4. `POST /login/forgot/verify` — 코드 검증 → 비번 정책 검증 → User.password 갱신 → consumed_at 기록 → `/login?reset` 으로 redirect
- 사용자가 존재하지 않아도 동일한 응답 ("코드를 발송했습니다") — 이메일 enumeration 방지
- audit_logs `RESET_PASSWORD` 기록

## 3. /signup/verify 5분 타이머 (D-12)

- 기존 `email_verification_tokens` TTL 변경: 10분 → 5분 (코드 상수 + 새 토큰 발급 시 자연 적용)
- 신규 발급 시 같은 user_id의 기존 verified_at IS NULL 토큰들을 expired_at = now() 처리(무효화)
- 화면에 5분 카운트다운 JS:
  ```js
  const expireAt = ...; // 서버에서 model로 내려준 ISO timestamp
  function tick() {
    const remain = (new Date(expireAt) - new Date()) / 1000;
    if (remain < 0) { ... 만료 처리, 재발송 버튼 활성 ... ; return; }
    const m = Math.floor(remain/60), s = Math.floor(remain%60);
    el.textContent = `${m}:${s.toString().padStart(2,'0')}`;
    if (remain < 60) el.classList.add('text-danger'); // 빨간색
    else el.classList.remove('text-danger');
    setTimeout(tick, 250);
  }
  tick();
  ```
- 만료 후 자동 재발송 X — "재발송" 버튼만 활성. 클릭 시 동일 라우트(`/signup/resend`)로 POST.
- 동시 발급된 이전 토큰은 무효화 (재발송 시 새 토큰만 valid).

## 4. NOT-DOING
- /signup 폼 순서 (M3-03)
- /dashboard 보강 (M3-04)
- DocumentType 변경
- 태그 제거

## 검증
- ID 저장 체크 + 로그인 → 다음 방문 시 이메일 미리 채워짐
- 체크 해제 + 로그인 → cookie 삭제됨
- 비번 찾기 흐름 정상 (이메일 입력 → 코드 검증 → 새 비번 → 로그인 가능)
- 비번 정책 위반 거부
- 인증 화면 5분 카운트다운 동작 / 만료 시 재발송 버튼 활성 / 1분 미만 빨간색
- 새 토큰 발급 시 이전 토큰 즉시 만료
- audit_logs `RESET_PASSWORD` 기록
