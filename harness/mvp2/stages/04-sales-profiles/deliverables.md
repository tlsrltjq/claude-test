# MVP2 Stage 04 — Deliverables

## SQL
- `V103__add_developer_grade_career.sql` — employee_profiles에 컬럼 추가, DocumentType 체크 제약 갱신

## Entity / Enum
- `employee/entity/EmployeeProfile.java` — `developerGrade`, `careerMonths`
- `document/entity/DocumentType.java` — `HEALTH_INSURANCE_PROOF` 추가

## Service
- `sales/service/SalesProfileQueryService.java` — 사용자 + 프로필 + 최신 APPROVED 문서 그룹 조회

## Controller
- `sales/controller/SalesProfileController` — `GET /sales/profiles`
- `sales/controller/SalesEmployeeDocumentController` — `GET /sales/employees/{userId}/documents`

## Templates
- `templates/sales/profiles.html` — 14컬럼 표
- `templates/sales/employee-documents.html` — read-only 카드 뷰
- 헤더 메뉴 갱신 (ADMIN/SALES "인력 표")
