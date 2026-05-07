# Stage 05 — Deliverables

## File storage
- `common/file/FileStorage.java` (interface)
- `common/file/LocalFileStorage.java`
- `common/file/StoredFile.java` / `common/file/FileUploadCommand.java` (DTO)

## Configuration
- `application.yml` 수정
  - `spring.servlet.multipart.max-file-size: 20MB`
  - `spring.servlet.multipart.max-request-size: 40MB`
  - `resourcehub.upload.allowed-extensions: pdf,jpg,jpeg,png,docx,hwp,hwpx`

## Services
- `document/service/DocumentUploadService.java`
- `document/service/MyFolderService.java`
- (필요 시) `document/service/DocumentQueryService.java`

## Controllers / Views
- `document/controller/MyFolderController.java`
  - `GET /my/folder`, `GET /my/folder/documents/upload`, `POST /my/folder/documents`
- `document/controller/AdminDocumentController.java`
  - `GET /admin/employees/{userId}/documents`
- `templates/my/folder.html`, `templates/my/upload.html`
- `templates/admin/employee-documents.html`

## Repositories (확장)
- `document/repository/DocumentRepository` — folder+type+title 조회 메서드
- `document/repository/DocumentVersionRepository` — document별 max(version_no)
