# Stage 04 — Deliverables

## Services
- `user/service/AdminUserApprovalService.java`
- `team/service/TeamService.java`
- `employee/service/EmployeeManagementService.java`
- `document/service/FolderService.java`

## Controllers
- `admin/AdminController.java` (또는 `user/controller/AdminController.java`) — `/admin`
- `user/controller/AdminUserController.java` — `/admin/users/pending`, 승인/반려
- `team/controller/AdminTeamController.java` — `/admin/teams`
- `employee/controller/AdminEmployeeController.java` — `/admin/employees`, `/admin/employees/{userId}`

## Views
- `templates/admin/dashboard.html`
- `templates/admin/users-pending.html`
- `templates/admin/teams.html`, `team-form.html`
- `templates/admin/employees.html`, `employee-detail.html`

## Bootstrap
- 초기 팀 시드 (개발팀/영업팀/기술지원팀/경영지원팀) — `CommandLineRunner` 또는 V3 데이터 마이그레이션
