# Stage 06 — Deliverables

## Services
- `document/service/DocumentAccessService.java`
- `audit/service/AuditLogService.java` (VIEW/DOWNLOAD 메서드)

## Controllers
- `document/controller/DocumentPreviewController.java` — `GET /documents/{documentVersionId}/preview`
- `document/controller/DocumentDownloadController.java` — `GET /documents/{documentVersionId}/download/reason`, `POST /documents/{documentVersionId}/download`
- `document/controller/DocumentDetailController.java` — `/my/folder/documents/{documentId}`, `/admin/employees/{userId}/documents/{documentId}`

## Views
- `templates/my/document-detail.html`
- `templates/admin/employee-document-detail.html`
- `templates/document-download-reason.html`
- `templates/error/access-denied.html` (재사용)

## audit_logs Action 추가
- `VIEW`, `DOWNLOAD` 가 AuditActionType enum에 존재
