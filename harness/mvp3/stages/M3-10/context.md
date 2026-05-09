# MVP3 M3-10 — Context

DECISIONS D-01, D-02, D-03.

이전: mvp2 04 14컬럼 + mvp2 05 검색·필터·정렬·컬럼 토글 + 경력 column 표시 (months 기반).

핵심:
- career_months → career_total_days 정확도 향상 (months는 derived)
- 프리셋은 본인 격리 (CONSTRAINT cvp_user_name_unique)
- 등급 위젯은 검색 결과 반영

위치:
- `V204`, `V205`
- `employee/entity/EmployeeProfile.java` (career_total_days)
- `sales/entity/ColumnViewPreference.java`, repository, service
- `sales/controller/ColumnPresetController` — `/sales/profiles/preset` (CRUD)
- `templates/sales/profiles.html` 갱신
