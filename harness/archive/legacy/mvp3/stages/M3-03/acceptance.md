# MVP3 M3-03 — Acceptance

## 자동 검증
- [ ] `templates/signup.html` 의 input 순서가 이름 → 생년월일 → 연락처 → 이메일 → 팀 → 직급 → 비번 순
- [ ] birth_date input type="text" + maxlength=8 (또는 pattern="\d{8}")
- [ ] 회사 이메일 suffix(@eactive.co.kr) 표기 유지

## 수동 검증
- [ ] /signup 진입 → 입력 순서 확인
- [ ] "20010904" 입력 → 가입 통과
- [ ] "2001-09-04" 입력 → 거부 + 한글 메시지
- [ ] "20251301" 같이 잘못된 날짜 → 거부
- [ ] 직급 select 에 상무 노출
- [ ] 이메일 오타 + 비번 입력 후 제출 → 거부 시 비번 input value 유지
- [ ] 가입 성공 시 DB users.birth_date 에 LocalDate 저장
- [ ] mvp2 03 acceptance 그대로 통과

## NOT-DOING
- [ ] /dashboard 보강 X (M3-04)

## 회귀
```bash
bash mvp3/harness/scripts/verify.sh M3-03 --with-mvp2
```
