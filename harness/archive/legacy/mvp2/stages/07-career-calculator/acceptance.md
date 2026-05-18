# MVP2 Stage 07 — Acceptance

## 자동 검증
- [ ] `/sales/career-calculator` 매핑 (GET, POST)
- [ ] `CareerCalculator` 클래스 존재
- [ ] `templates/sales/career-calculator.html` 존재
- [ ] (있으면) `CareerCalculatorTest` 단위 테스트 파일 존재

## 수동 검증
- [ ] ADMIN/SALES 로그인 → `/sales/career-calculator` 접근
- [ ] 기간 3개 입력 → 결과 "N년 N개월"
- [ ] 중복 제거 체크 → 머지 결과 다름 확인
- [ ] 빈 입력 → 안내 메시지
- [ ] 종료일 < 시작일 → 거부 메시지
- [ ] 행 추가/삭제 JS 동작
- [ ] EMPLOYEE → 403
- [ ] employee_profiles.career_months 는 변경되지 않음 (저장 연동 X)

## NOT-DOING
- [ ] 프로필 저장 연동 안 함 (M2-09)
- [ ] 엑셀 내보내기 안 함 (M2-08)

## MVP1 회귀
```bash
bash mvp2/harness/scripts/verify.sh 07 --with-mvp1
```
