# MVP3 M3-05 — Deliverables

## SQL
- `V202__update_document_type_check.sql`

## Enum / 검증
- `document/entity/DocumentType.java` — LICENSE displayName 변경 / EMPLOYMENT_CERTIFICATE @Deprecated / PROFILE_PHOTO 추가
- `document/service/DocumentUploadService` — PROFILE_PHOTO일 때 jpg/png만

## Config
- `application.yml` allowed-extensions: `pdf,jpg,jpeg,png,docx,hwp,hwpx,ppt,pptx`

## Templates (태그 제거)
- `templates/my/folder.html`, `templates/my/document-detail.html`
- `templates/admin/employee-documents.html`, `templates/admin/employee-document-detail.html`
- `templates/sales/employee-documents.html`
- (있다면) `templates/document-tags*.html` 삭제
