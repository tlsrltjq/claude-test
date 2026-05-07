# Stage 07 — Deliverables

## Services
- `permission/service/PermissionService.java` (또는 GrantService)
- `document/service/DocumentAccessService.java` (확장)
- `permission/service/FolderAccessService.java` (또는 `document/service/FolderAccessService.java`)
- `user/service/UserRoleService.java`

## Controllers
- `user/controller/AdminUserRoleController.java` — `/admin/users/{userId}/role`
- `permission/controller/AdminPermissionController.java` — `/admin/users/{userId}/permissions`
- `team/controller/TeamLeaderController.java` — `/team/members`, `/team/members/{userId}/documents`
- `permission/controller/SharedFolderController.java` — `/shared/folders`, `/shared/folders/{folderId}/documents`

## Views
- `templates/admin/user-permissions.html`
- `templates/team/members.html`, `templates/team/member-documents.html`
- `templates/shared/folders.html`, `templates/shared/folder-documents.html`

## audit_logs
- `AuditActionType` 에 `CHANGE_ROLE`, `GRANT_PERMISSION`, `REVOKE_PERMISSION` 존재
