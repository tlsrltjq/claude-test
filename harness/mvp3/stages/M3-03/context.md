# MVP3 M3-03 — Context

## SSOT
PROJECT_SPEC_MVP3 §5 회원가입.
mvp2 03 (회원가입 필드 확장) 결과 기반.

## 이전 결과
- birth_date / phone / position(enum) / 자동 도메인 조합 / 비번 정책 적용 중
- 생년월일은 input type="date" (yyyy-MM-dd) 였을 가능성

## 핵심 제약
- 입력값 검증 실패 시 비밀번호 필드 유지 — 단 로그/응답에 평문 비번 노출 금지
- DB는 birth_date LocalDate 그대로

## 코드 위치
- `templates/signup.html` (필드 순서 + 8자 input + suffix)
- `user/dto/SignupForm.java` (생년월일 String → LocalDate 변환)
- `user/service/SignupService.java` (검증 로직)
