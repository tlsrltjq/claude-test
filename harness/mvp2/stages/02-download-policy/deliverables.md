# MVP2 Stage 02 — Deliverables

## SQL (조건부)
- `V101__make_audit_reason_nullable.sql` — 기존 audit_logs.reason이 NOT NULL일 경우 ALTER

## Controllers / Services
- `document/controller/DocumentDownloadController` — `/documents/{id}/download/reason` 제거, `GET /documents/{id}/download` 추가/단일화
- `document/controller/AdminDocumentDeleteController` (또는 AdminDocumentController에 추가) — `DELETE /admin/documents/{id}` 또는 `POST /admin/documents/{id}/delete`
- `document/service/DocumentDeleteService` — 디스크 + DB + 썸네일/preview 일괄 삭제
- `document/service/DocumentAccessService` — EMPLOYEE 본인 한정, ADMIN/SALES 전체 (이미 01에서 SALES 추가됐을 수 있음)

## Enums
- `audit/entity/AuditActionType.DELETE_DOCUMENT` 추가

## Templates
- `templates/document-download-reason.html` 삭제
- 모든 다운로드 버튼/링크에서 사유 입력 단계 제거 (직접 GET)
- 관리자 문서 목록/상세에 삭제 버튼

## README
- 다운로드 정책 변경 안내
