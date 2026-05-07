# Stage 09 — Deliverables

## SQL
- `V4__add_review_columns_to_document_versions.sql` — review_status (default PENDING_REVIEW), reviewed_by, reviewed_at, reject_reason

## Entity / Enum
- `DocumentVersion.java` 필드 추가
- `DocumentReviewStatus.java` (PENDING_REVIEW/APPROVED/REJECTED/ARCHIVED)

## Services
- `document/service/DocumentReviewService.java`
- `DocumentAccessService.java` 갱신 (review_status 조건 반영)

## Controllers / Views
- `document/controller/AdminDocumentReviewController.java`
  - `GET /admin/documents/review`, `GET /admin/documents/review/{documentVersionId}`
  - `POST /admin/documents/review/{documentVersionId}/approve`
  - `POST /admin/documents/review/{documentVersionId}/reject`
- `templates/admin/documents-review.html`
- `templates/admin/document-review-detail.html`
- 카드 뷰 templates 갱신: 상태 배지 표시
- 반려 사유 표시: `templates/my/document-detail.html`, `templates/admin/employee-document-detail.html`

## audit_logs
- `AuditActionType` 에 `SUBMIT_REVIEW`, `APPROVE_DOCUMENT`, `REJECT_DOCUMENT` 존재

## 관리자 대시보드
- `/admin` 위젯에 PENDING_REVIEW 카운트
