# MVP3 M3-01 — Deliverables

## SQL
- `V200__add_managing_director_to_position.sql`

## Enum
- `user/entity/Position.java` — MANAGING_DIRECTOR("상무") 추가, 전무 다음 / 이사 앞
- `user/entity/UserRole.java` — `getDisplayName()` 한글 매핑

## Templates (영문 → 한글 displayName 사용)
- `templates/dashboard.html`
- `templates/admin/employees.html`, `employee-detail.html`, `users-pending.html`, `user-permissions.html`
- `templates/sales/profiles.html`, `members.html`
- `templates/signup.html` (직급 select)
- 헤더 fragment (권한 배지)

## Tests (선택)
- Position.MANAGING_DIRECTOR.getDisplayName() == "상무"
- UserRole.SALES.getDisplayName() == "영업"
