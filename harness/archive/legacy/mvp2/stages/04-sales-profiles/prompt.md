# MVP2 Stage 04 — Prompt

MVP2 4단계 작업을 진행해줘.

이전 단계까지: 권한 단순화, 다운로드 정책, 회원가입 필드 확장 완료.

이번 단계 목표는 **영업부 인력 프로필 표 `/sales/profiles`** 화면을 만드는 것이야. 검색/필터/정렬은 다음 단계로 미루고, 표 자체와 행 동작(이름 클릭→폴더, 문서 칸→다운로드)을 완성한다.

요구사항:

1. Flyway 마이그레이션 `V103__add_developer_grade_career.sql`
   - `employee_profiles` 에 컬럼 추가:
     - `developer_grade VARCHAR(20)` — null 허용
     - `career_months INT NOT NULL DEFAULT 0`
   - `DocumentType` enum에 `HEALTH_INSURANCE_PROOF` 신규 값 — 만약 DB enum 체크 제약이 있으면 그 제약을 갱신.

2. Entity 갱신
   - `employee/entity/EmployeeProfile.java` — `String developerGrade`, `int careerMonths`
   - `document/entity/DocumentType.java` — `HEALTH_INSURANCE_PROOF` 추가

3. (선택) `DeveloperGrade` enum도 만들 수 있지만 1차에서는 String 으로 두고 SALES가 조회/정렬할 때 정렬 가능하게.
   - 권장 값: `JUNIOR`, `INTERMEDIATE`, `SENIOR`, `EXPERT` (기본 옵션 dropdown으로). 실제 enum 매핑은 선택.

4. 컨트롤러 신설 — `sales/controller/SalesProfileController`
   - `GET /sales/profiles` — ADMIN/SALES만
   - 데이터: 모든 ACTIVE 사용자 + EmployeeProfile + 각 DocumentType별 최신 APPROVED DocumentVersion (없으면 null)
   - 검색/필터/정렬 파라미터는 받지 않음 (다음 단계). N+1 안 나도록 적절한 fetch join 또는 미리 group by 쿼리.

5. 화면 — `templates/sales/profiles.html`
   - 14개 컬럼: 직급, 이름, 나이, 생년월일, 연락처, 이메일, 개발자 등급, 경력, 이력서, 경력기술서, 정보처리기사/자격증, 졸업증명서, 건강보험자격득실확인서, 재직증명서, 기타자료, 업데이트 날짜
   - 이름 클릭 → ADMIN 이면 `/admin/employees/{id}/documents`, SALES 이면 `/sales/employees/{id}/documents`
   - 문서 셀: 있으면 `[보기]` `[다운로드]` 버튼, 없으면 `없음` 텍스트
   - "보기"는 미리보기 화면(`/documents/{versionId}/preview`), "다운로드"는 즉시 다운로드(`/documents/{versionId}/download`)
   - 경력은 N년 N개월 형식 표시 (career_months / 12, % 12)
   - 나이는 server-side 계산 또는 Thymeleaf 헬퍼

6. SALES용 직원 폴더 화면 — `/sales/employees/{userId}/documents`
   - 이름/팀/직급/문서 목록 (MVP1 admin/employees/{id}/documents와 유사한 read-only 카드 뷰 재사용)
   - 다운로드 버튼은 사유 없이 바로 다운로드 (M2-02 정책)
   - 업로드/수정/삭제 버튼은 노출하지 마

7. 헤더 메뉴 갱신 — ADMIN/SALES 에 "인력 표" 링크 (`/sales/profiles`) 노출

8. 권한 검사
   - SALES 의 다른 사람 폴더 접근은 read-only (MVP2 01에서 이미 적용됐을 것)
   - EMPLOYEE 가 `/sales/profiles` 접근 시 403

9. NOT-DOING
   - 검색/필터/정렬 (05)
   - 컬럼 토글
   - 양식 이력서 표시 변경 (양식 등록은 06에서)
   - 엑셀 내보내기

검증:
- ADMIN/SALES 로그인 → /sales/profiles 14컬럼 표 표시
- 이름 클릭 → 직원 폴더로 이동
- 문서 있는 경우 보기/다운로드 동작
- 문서 없는 경우 "없음"
- EMPLOYEE 접근 시 403
- DB: employee_profiles에 developer_grade/career_months 컬럼 존재
