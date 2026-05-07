# MVP3 M3-12 — Prompt

작업: /sales/career-calculator 검색 기능 동작 복구.

배경: 현재 화면에 검색 input 이 있지만 동작하지 않음. 다음 둘 다 가능하도록:
1. input 오른쪽 "검색" 버튼 클릭
2. input 에서 Enter 키

요구사항:

1. 디버깅 — 현재 동작 안 하는 원인 파악
   - 폼 action 누락? method 잘못? button type=submit 누락?
   - JS 핸들러 누락 또는 preventDefault 위치?
   - 컨트롤러 매핑 파라미터 이름 mismatch?
   원인을 PR 메시지/주석에 기록.

2. UI:
   - input 옆에 `<button type="submit">검색</button>` 또는 아이콘 버튼
   - input 에 keypress / keydown 핸들러 — Enter (keyCode 13) 시 form submit (또는 form 안에 있으면 type="submit"이 자동 처리)
   - 버튼이 form 외부에 있다면 JS 로 form submit 호출

3. 폼 — GET 또는 POST. 결과는 같은 화면에 표시 (결과 영역 + 입력값 다시 채움).

4. 검색 대상이 무엇인지 확인 — 직원 목록인지, 이전 계산 기록인지. 화면 의도에 맞게.
   - mvp2 07 단독 계산기였다면 직원 검색이 화면에 있을 이유가 없을 수 있음 — 사용자가 추가한 검색이라면 위치/대상을 확인.
   - 사용자 요청: "선택한 사람 경력 불러오기는 나중" → 즉, 직원 검색은 향후 직원 선택용으로 보임. 그렇다면 이번 단계에서 검색은 "결과 표시"만 하고 선택/저장 액션은 비활성.

5. NOT-DOING
   - 검색 결과에서 사람 클릭 → 그 사람 경력 자동 입력 — 이건 사용자가 "나중"으로 미룸

검증:
- 검색 input 에 텍스트 입력 + Enter → 결과 화면 갱신
- 검색 버튼 클릭 → 동일 동작
- 빈 입력 → 전체 결과 또는 안내 (의도에 따라)
- mvp2 07 의 계산기 자체는 그대로 동작
