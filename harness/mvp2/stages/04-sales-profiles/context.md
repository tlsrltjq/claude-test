# MVP2 Stage 04 — Context

## SSOT
`mvp2/docs/PROJECT_SPEC_MVP2.md` §6 인력 프로필 표.
`mvp2/docs/MIGRATION_FROM_MVP1.md` §5 (V103), §6 (HEALTH_INSURANCE_PROOF).

## 이전 단계 결과
- 권한 단순화 + SALES 전사 read-only
- 사유 없이 바로 다운로드, 관리자 삭제
- users 에 birth_date / phone / position(enum) 들어옴

## 이번 단계 핵심 제약
- N+1 회피 — 14컬럼이라 사용자 1명당 최대 7개 문서 종류 lookup. 미리 한 번의 group-by 쿼리로 (user_id, document_type, max(version_no)) 가져오기 또는 fetch join.
- 표는 read-only. 업로드/수정/삭제 버튼 미노출.
- `/sales/employees/{id}/documents`는 MVP1 admin 화면을 SALES용으로 재사용/복제하되 권한 분기.

## 코드가 들어갈 위치
- 마이그레이션: `V103__add_developer_grade_career.sql`
- Entity: `employee/entity/EmployeeProfile.java`, `document/entity/DocumentType.java`
- 컨트롤러: `sales/controller/SalesProfileController`, `sales/controller/SalesEmployeeDocumentController`
- 서비스: `sales/service/SalesProfileQueryService` (그룹 쿼리)
- 템플릿: `templates/sales/profiles.html`, `templates/sales/employee-documents.html`
