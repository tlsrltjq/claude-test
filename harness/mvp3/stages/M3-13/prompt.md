# MVP3 M3-13 — Prompt

작업: 계정 활성/비활성 토글.

요구사항:

1. 컨트롤러
   - `POST /admin/employees/{userId}/disable` — ADMIN만, status=DISABLED
   - `POST /admin/employees/{userId}/activate` — ADMIN만, status=ACTIVE
   - 자기 자신은 비활성 X (보안 — 한 ADMIN이라도 살아있어야 함, 또는 단순히 본인 계정 비활성 거부)
   - audit_logs `CHANGE_USER_STATUS` 행

2. 즉시 세션 무효화
   - Spring Security `SessionRegistry` 또는 `findAllSessions(principal)` → expireNow()
   - 사용자가 어느 화면을 보고 있어도 다음 요청에서 /login 으로 redirect (세션 만료)
   - SecurityConfig 에 `sessionAuthenticationStrategy` + `SessionRegistry` Bean 등록

3. 로그인 거부
   - mvp2 03 의 LoginAuthenticationProvider 또는 UserDetailsService 가 status 검증하고 있을 것
   - DISABLED 면 거부 (이미 그렇게 되어있을 가능성 높음 — 회귀 안 깨지게)

4. permissions 보존
   - 비활성 시 permissions 행 삭제 X
   - 활성 복귀 시 그대로 사용

5. UI
   - `/admin/employees/{userId}` 직원 상세에 "비활성화" 버튼 (ACTIVE 일 때) / "활성화" 버튼 (DISABLED 일 때)
   - 클릭 시 confirm — "정말 비활성화 하시겠습니까? 즉시 모든 세션이 만료됩니다."
   - 비활성 사용자는 직원 목록에서 회색/배지 "비활성"

6. NOT-DOING
   - 자동 비활성화 정책 (퇴사일 등) — 다음 라운드

검증:
- 활성 사용자 비활성 → status=DISABLED, audit_logs 행
- 그 사용자가 접속 중이었다면 다음 요청에서 /login 으로 튕김
- 그 사용자가 다시 로그인 시도 → 거부
- 활성 복귀 → 로그인 가능, permissions 그대로
- 본인 자신 비활성 시도 → 거부
- 직원 목록에서 비활성 배지 표시
