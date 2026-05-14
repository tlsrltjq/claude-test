# MVP2 Stage 05 — Prompt

MVP2 5단계 작업을 진행해줘.

이전 단계까지: 권한 단순화 / 다운로드 정책 / 회원가입 필드 / 영업부 기본 표.

이번 단계 목표는 `/sales/profiles` 표에 **검색·필터·정렬·컬럼 토글**을 추가하는 것이야.

요구사항:

1. 검색 (서버 사이드)
   - 쿼리 파라미터: `q` (이름·이메일 contains), `position` (직급 enum 값), `developerGrade` (개발자 등급 String)
   - 빈 값은 무시. 여러 파라미터가 들어오면 AND.
   - case-insensitive contains.

2. 필터 — 문서 보유 여부
   - 쿼리 파라미터: `hasResume`, `hasCareerDescription`, `hasLicense`, `hasGraduationCertificate`, `hasHealthInsuranceProof`, `hasEmploymentCertificate`, `hasEtc` — 각각 `true`/`false`/없음.
   - true 면 최신 APPROVED DocumentVersion 존재하는 사용자만, false 면 없는 사용자만.

3. 정렬
   - 쿼리 파라미터: `sort` (예: `name`, `position`, `developerGrade`, `careerMonths`), `direction` (`asc`/`desc`).
   - 기본: `name asc`.
   - 직급 정렬은 enum 정의 순서 (REPRESENTATIVE → STAFF) 또는 displayName 가나다 순 — 어느 쪽이든 일관되게.
   - 경력 정렬은 careerMonths 숫자 정렬.

4. 페이지네이션은 1차에서 단순화 (전체 표시 또는 페이지당 50건).

5. 컬럼 토글 (서버 사이드 + 화면)
   - 쿼리 파라미터 `cols=position,name,age,birthDate,phone,email,developerGrade,career,resume,...,updatedAt` (콤마 구분).
   - 명시 안 하면 기본 14컬럼 모두.
   - 사용자 선택을 cookie 또는 localStorage 에 저장해 다음 방문 시 복원 (둘 중 하나로 충분, 1차).

6. 화면 (`templates/sales/profiles.html`)
   - 상단에 검색/필터 폼 (GET 방식).
   - 테이블 헤더 클릭 시 정렬 토글 (asc → desc → 해제). 클릭 시 같은 폼이 새 sort/direction 값으로 다시 GET.
   - 컬럼 토글 패널: 체크박스 14개. 토글 시 GET 또는 client JS로 표시/숨김 + cookie 갱신.
   - "초기화" 버튼.

7. SalesProfileQueryService 갱신
   - Spring Data JPA Specification 또는 QueryDSL 또는 동적 JPQL 로 검색 조건 조합.
   - JPQL이 가장 단순. JPA Specification 권장.

8. NOT-DOING
   - 컬럼 순서 드래그 (2차)
   - 엑셀 내보내기

검증:
- 이름 검색 입력 → 결과 좁혀짐
- 직급 select → 해당 직급만
- 문서 보유 필터 → 해당 직원만
- 컬럼 정렬 동작
- 컬럼 토글 후 새로고침 → 토글 상태 유지 (cookie or localStorage)
- 초기화 버튼 → 기본 상태 복귀
