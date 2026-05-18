# Stage 03 — Prompt

> PDF §22 3단계 프롬프트 원본

---

3단계 작업을 진행해줘.

현재 프로젝트는 Java 21, Spring Boot 3.5.x, Gradle 기반의 eActive Resource Hub야.
패키지명은 `com.eactive.resourcehub`야.

1단계에서 프로젝트 기본 골격을 만들었고,
2단계에서 `users`, `teams`, `employee_profiles`, `folders`, `documents`, `document_versions`, `permissions`, `audit_logs` 테이블과 JPA Entity, Repository를 만들었어.

이번 단계의 목표는 회사 이메일 기반 회원가입, 이메일 인증, 관리자 승인 대기, 세션 기반 로그인/로그아웃 기능을 구현하는 것이야.

중요:
- JWT는 사용하지 마.
- 로그인 인증은 Spring Security의 세션 기반 인증을 사용해.
- Remember-me 기능은 사용하지 마.
- CSRF는 활성화해.
- 세션 유지 시간은 기본 30분으로 설정해.
- 이메일 인증에는 별도의 6자리 인증 코드를 사용해.

요구사항은 다음과 같아.

1. 회사 이메일로만 회원가입 가능하게 해줘.
   - 회사 이메일 도메인은 `application.yml`의 `resourcehub.company-email-domain` 값으로 관리해줘.
   - 기본값은 `eactive.co.kr`로 해줘.
   - 코드에 `@eactive.co.kr` 값을 직접 박지 말고 설정값을 사용해줘.

2. 회원가입 화면을 만들어줘.
   - URL: `/signup`
   - 입력값: 이름, 이메일, 비밀번호, 비밀번호 확인
   - 이메일은 회사 도메인만 허용해줘.
   - 비밀번호와 비밀번호 확인이 일치해야 해.
   - 비밀번호는 BCrypt로 암호화해서 저장해줘.
   - `login_id`는 이메일과 동일하게 사용해줘.

3. 회원가입 직후 사용자 상태는 `PENDING_EMAIL_VERIFICATION`으로 저장해줘.
   - 기본 권한은 `EMPLOYEE`로 저장해줘.
   - `email_verified`는 false로 저장해줘.

4. 이메일 인증 기능을 만들어줘.
   - `email_verification_tokens` 테이블을 Flyway 마이그레이션으로 추가해줘.
   - 파일명은 `src/main/resources/db/migration/V2__create_email_verification_tokens.sql`로 해줘.
   - 컬럼: `id`, `user_id`, `email`, `token`, `expired_at`, `verified_at`, `created_at`
   - 인증 코드는 6자리 숫자.
   - 인증 코드 유효시간은 10분.
   - 로컬 개발 단계에서는 실제 메일을 보내지 말고 `ConsoleEmailSender`를 만들어 서버 로그에 인증 코드를 출력하게 해줘.
   - 나중에 SMTP로 바꿀 수 있도록 `EmailSender` 인터페이스를 만들어줘.

5. 인증 코드 입력 화면 (`/signup/verify`)에서 검증, 성공 시 `email_verified=true` + 상태 `PENDING_ADMIN_APPROVAL`로, 실패 시 적절한 메시지. 인증 후 `/signup/pending`으로 이동.

6. 관리자 승인 대기 화면(`/signup/pending`)에 안내 문구.

7. Spring Security 설정.
   - 세션 기반 로그인.
   - JWT 만들지 마.
   - Remember-me 사용하지 마.
   - CSRF 활성화.
   - `/login`, `/signup`, `/signup/verify`, `/signup/pending`, `/health`, `/css/**`, `/js/**`, `/images/**` 허용.
   - 그 외는 인증 필요.

8. 세션 정책.
   - timeout 30분, 쿠키명 `RESOURCEHUB_SESSION`, httpOnly true, sameSite strict, secure는 로컬 false (운영 true).

9. 로그인 화면 `/login`. ACTIVE 상태만 허용. 실패 시 공통 메시지("로그인할 수 없습니다. 이메일, 비밀번호 또는 계정 상태를 확인해주세요.").

10. 로그아웃 `/logout`. 세션 무효화 + 쿠키 삭제 + `/login?logout`으로 이동.

11. 기본 관리자 계정 초기화.
    - 시작 시 관리자 없으면 생성.
    - 이메일/비밀번호는 `application.yml`/환경변수.
    - 기본 이메일 `admin@eactive.co.kr`.
    - role=ADMIN, status=ACTIVE, email_verified=true.

12. 로그인 성공 후 `/dashboard`로 이동. 사용자 이름/이메일/권한/상태 표시.

13. 인증 관련 로그(가입/인증/로그인/로그아웃) — 가능하면 `audit_logs`에, 어려우면 애플리케이션 로그.

14. README.md에 3단계 실행/검증 방법 추가.

15. 아직 만들지 마: 팀별 접근 권한, 파일 업로드, 문서 미리보기/다운로드, 관리자 전체 화면, 관리자 승인 처리 화면, 팀장 권한 부여, 개별 폴더 접근 권한.
