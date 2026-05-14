# Stage 09 — Context

## SSOT
PROJECT_SPEC §6 MVP 기능 19번(관리자 승인/반려), §11 파일 접근.

## 이전 단계 결과
- 권한 분기, 썸네일, 카드 뷰 동작.
- 모든 문서가 즉시 팀장/개별 권한자에게 노출됨 → 이번에 차단.

## 이번 단계 핵심 제약
- 새 버전 업로드 시 current_version_id를 **건드리지 말 것** (승인 시점에만 갱신).
- 승인되지 않은 첫 버전 문서는 다른 사용자에게 보이지 않음 (current_version_id null).
- ARCHIVED는 이번 단계에서 사용 안 해도 OK (enum만 정의).
- documents.status와 review_status를 혼동하지 말 것 — review_status는 document_versions의 컬럼.

## 코드가 들어갈 위치
- `src/main/resources/db/migration/V4__add_review_columns_to_document_versions.sql`
- `document/entity/DocumentReviewStatus.java`
- `document/service/DocumentReviewService.java`
- `document/controller/AdminDocumentReviewController.java`
- 기존 DocumentAccessService / 카드 뷰 / 다운로드/미리보기 로직 일괄 갱신.
