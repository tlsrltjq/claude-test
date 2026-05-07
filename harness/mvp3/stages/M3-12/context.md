# MVP3 M3-12 — Context

이전: mvp2 07 career-calculator 단독 도구 verified. 사용자가 그 후 검색 input 을 추가했지만 동작 안 함.

핵심:
- 원인 파악이 우선 — 코드 변경 전 화면/컨트롤러 일치 여부 검토
- 사용자 의도: "선택한 사람 경력 불러오기는 나중" → 검색은 일단 결과 표시까지

위치:
- `templates/sales/career-calculator.html`
- `sales/controller/CareerCalculatorController` (검색 파라미터 처리)
