# Stage 06 — Context

## SSOT
PROJECT_SPEC §11 파일 접근 방식, §5.6 미리보기, §5.8/5.9 다운로드/로그.

## 이전 단계 결과
- 본인 폴더 업로드 + 버전 관리 동작.
- audit_logs에 UPLOAD/UPDATE_DOCUMENT 기록.
- DocumentVersion에 preview_storage_path가 채워지는 케이스가 있음 (DOCX 등).

## 이번 단계 핵심 제약
- 정적 리소스 핸들러로 `/uploads/**` 같은 직접 노출 금지.
- 미리보기/다운로드는 모두 컨트롤러 + Service.
- 다운로드 사유는 필수, reason 빈 값 또는 1자 이하 금지.
- VIEW와 DOWNLOAD는 별개 로그.
- 권한 규칙은 7단계에서 확장될 예정 — 이번엔 ADMIN/본인만.

## 코드가 들어갈 위치
- `document/service/DocumentAccessService.java`
- `document/controller/DocumentPreviewController.java`, `DocumentDownloadController.java`, `DocumentDetailController.java`
- `audit/service/AuditLogService.java`
- 템플릿: `templates/my/document-detail.html`, `templates/admin/employee-document-detail.html`, `templates/document-download-reason.html`
