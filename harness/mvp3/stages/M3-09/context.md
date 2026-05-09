# MVP3 M3-09 — Context

이전: M3-01 한글 + 상무. mvp2 05 sales/profiles 검색·필터·정렬 패턴 그대로 재사용.

핵심: 직급 정렬은 enum 순서가 아닌 sortOrder() 메서드로 추상화 권장 (M3-01 시 분리됐다면 그대로 사용).

위치:
- `sales/controller/SalesMembersController` (mvp2 01에서 만든 거 그대로) — 정렬 파라미터 추가
- `employee/controller/AdminEmployeeController` 갱신 — 검색 + 정렬 파라미터
- `templates/sales/members.html`, `templates/admin/employees.html`
