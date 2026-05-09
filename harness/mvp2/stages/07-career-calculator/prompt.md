# MVP2 Stage 07 — Prompt

MVP2 7단계 작업을 진행해줘.

이전 단계까지 1차 6단계 모두 완료.

이번 단계 목표는 **경력 계산기 단독 도구**를 만드는 것이야. 영업부/관리자가 인력 검토 시 보조 도구로 사용. 직원 프로필 저장과는 연결하지 마 (2차).

요구사항:

1. 도메인이 따로 필요 없는 순수 계산. DB 변경 없음.
2. 컨트롤러 — `sales/controller/CareerCalculatorController`
   - `GET /sales/career-calculator` — 입력 폼 + 계산 결과 표시
   - `POST /sales/career-calculator` — 계산 후 같은 페이지로 결과 렌더링 (Post-Redirect-Get 또는 단순 form post)
   - 권한: ADMIN 또는 SALES
3. 입력 폼
   - 기간 N개 (행 추가/삭제 JS): 각 행에 `시작일(date)` + `종료일(date)`
   - 체크박스: "중복 기간 제거"
   - "계산하기" 버튼 + "초기화" 버튼
4. 도메인 서비스 — `sales/service/CareerCalculator.java` (또는 util 클래스)
   - 입력: `List<Period>` (start, end), `boolean removeOverlap`
   - 출력: `int totalMonths` 와 `String displayText` ("N년 N개월")
   - 알고리즘:
     - removeOverlap=false: 단순 합 (각 구간 month 단위 합)
     - removeOverlap=true: 구간 정렬 + 머지 후 합. 일 단위 정확도가 필요하면 개월 단위로 절상/절사 정책 결정 (월말 - 월초 = 1개월 등 합리적 기본). 1차에서는 month 단위 단순 계산으로 OK.
   - 빈 입력 / 종료일 < 시작일 같은 경우 검증.
5. 화면 — `templates/sales/career-calculator.html`
   - 행 추가 JS (기간 + 또는 - 버튼)
   - 결과 영역: "총 경력: 5년 3개월" + 디테일 ("머지 후 18개 구간, 총 63개월" 같은 보조 정보)
   - 폼 다시 제출 가능
6. 헤더 메뉴 — ADMIN/SALES 에 "경력 계산기" 링크 추가 (`/sales/career-calculator`)
7. EmployeeProfile.career_months 와는 **연결하지 마**. 2차에서 진행.
8. 권한 — EMPLOYEE 접근 시 403
9. NOT-DOING
   - 직원 프로필 저장 연동
   - 엑셀 내보내기

검증:
- ADMIN/SALES → `/sales/career-calculator` 접근
- 기간 3개 입력 → 합산 결과 표시
- 중복 제거 체크 → 머지 결과 표시
- 빈 입력 → 안내 메시지
- 종료일 < 시작일 → 거부
- EMPLOYEE → 403
