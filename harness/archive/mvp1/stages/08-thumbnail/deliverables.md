# Stage 08 — Deliverables

## SQL
- `src/main/resources/db/migration/V3__add_thumbnail_columns_to_document_versions.sql`

## Entity
- `DocumentVersion.java` — thumbnailFileName / thumbnailStoragePath / thumbnailContentType / thumbnailGeneratedAt

## Services
- `document/service/ThumbnailService.java` (필요 시 generator 클래스 분리)

## Controllers
- `document/controller/ThumbnailController.java`
  - `GET /documents/{documentVersionId}/thumbnail`
  - `POST /documents/{documentVersionId}/thumbnail/regenerate`

## Templates / Static
- 카드 뷰로 갱신: `templates/my/folder.html`, `templates/admin/employee-documents.html`, `templates/team/member-documents.html`, `templates/shared/folder-documents.html`
- 종류별 기본 아이콘 (`static/images/icons/{document-type}.png` 또는 단일 `default.png`)

## audit_logs
- `AuditActionType.REGENERATE_THUMBNAIL` 추가

## Build dependency
- `build.gradle`에 PDFBox(또는 동급), 이미지 리사이즈 라이브러리 의존성 추가
