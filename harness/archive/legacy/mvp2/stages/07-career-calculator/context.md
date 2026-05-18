# MVP2 Stage 07 — Context

## SSOT
`mvp2/docs/PROJECT_SPEC_MVP2.md` §9 경력 계산기.

## 이전 단계 결과
- 1차 1~6단계 모두 verified
- /sales/profiles 표 + 양식 이력서 동작 중

## 핵심 제약
- DB 변경 없음
- 순수 계산 — 단위 테스트로 알고리즘 검증 권장
- 결과를 employee_profiles.career_months 에 저장하지 마 (M2-09)

## 코드가 들어갈 위치
- `sales/controller/CareerCalculatorController.java`
- `sales/service/CareerCalculator.java`
- `sales/dto/CareerPeriod.java`, `CareerCalcRequest.java`, `CareerCalcResult.java`
- `templates/sales/career-calculator.html`
- `static/js/career-calculator.js` (행 추가/삭제 JS)
