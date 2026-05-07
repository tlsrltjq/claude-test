# MVP3 M3-04 — Prompt

작업: `/dashboard` 내 정보 카드 보강.

요구사항:

1. 표시 필드:
   - 이름 (기존)
   - 이메일 (기존)
   - 권한(한글, M3-01)
   - 상태(한글, M3-01)
   - **개발자 등급** (employee_profiles.developer_grade)
   - **생년월일** (users.birth_date — 8자 형식 또는 yyyy-MM-dd 표시)
   - **본인 팀** (teams.name via users.team_id)
   - **연락처** (users.phone)
   - **직급** (Position displayName, M3-01)

2. 컨트롤러 — `DashboardController`
   - join: User → EmployeeProfile (LEFT JOIN, 프로필 미생성 사용자 대비) → Team
   - DTO `DashboardSelfView` 로 전달

3. 화면 — `templates/dashboard.html`
   - 카드 1개에 grid 또는 table 로 정보 표시
   - 미설정 값(예: developer_grade null)은 "미설정" 표시

4. NOT-DOING
   - 본인 정보 수정 화면 (다음 라운드)
   - 다른 화면 변경

검증:
- 로그인 후 /dashboard → 위 9개 항목 모두 표시
- developer_grade 미설정 사용자 → "미설정" 노출
- 권한/직급 한글 표시
- mvp2 회귀 없음
