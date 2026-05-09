# MVP3 M3-13 — Context

DECISIONS D-11.

이전: UserStatus enum 에 DISABLED 가 이미 존재(mvp1 기준). 로그인 시 status 검증도 있을 가능성.

핵심:
- SessionRegistry Bean 등록이 필요할 수 있음
- 본인 비활성 거부
- permissions 보존

위치:
- `user/controller/AdminUserStatusController` (or AdminEmployeeController에 메서드 추가)
- `user/service/AdminUserStatusService` — 세션 무효화 로직
- `common/security/SecurityConfig` — SessionRegistry Bean
- `templates/admin/employee-detail.html` — 토글 버튼
- `audit/entity/AuditActionType.CHANGE_USER_STATUS`
