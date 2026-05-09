# MVP3 M3-03 — /signup 폼 정비

## 목적
- 폼 입력 순서: 이름 → 생년월일 → 연락처 → 회사 이메일 → 팀 → 직급 → 비밀번호
- 생년월일을 8자(YYYYMMDD) 텍스트로 받아 LocalDate 변환
- 직급 select 에 상무 노출 (M3-01 결과 자동 반영)
- 이메일 잘못 입력 시 비밀번호 필드 유지 (현 상태 유지 — 회귀 보장)

## 진입 조건
- M3-01, M3-02 verified

## NOT-DOING
- /dashboard 보강 (M3-04)
- DocumentType 변경 (M3-05)
