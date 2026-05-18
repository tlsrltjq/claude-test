# MVP2 Stage 03 — Deliverables

## SQL
- `V102__add_profile_fields_to_users.sql` — birth_date / phone 추가, position 정형화, 인덱스 추가

## Enum
- `user/entity/Position.java` — 8개 직급 (한글 displayName 포함)

## Entity
- `user/entity/User.java` — `Position position`, `LocalDate birthDate`, `String phone`, `@Transient int getAge()`

## DTO / Form
- `user/dto/SignupForm.java` — 새 필드 + 검증 어노테이션
- 비밀번호 검증 — 커스텀 `@PasswordPolicy` 또는 ConstraintValidator

## Service
- `user/service/SignupService.java` — 이메일 자동 조합, 비밀번호 정책 적용
- `user/service/EmailVerificationService.java` — 이메일 입력 없이 토큰 기반 검증

## Templates
- `templates/signup.html` — 직급/생년월일/연락처/이메일 앞부분 + suffix 표시
- `templates/signup-verify.html` — 이메일 입력란 제거, 6자리 코드만

## Bootstrapping
- `AdminBootstrapper` (또는 동급) — 기본 관리자에 새 필드 default 값 채움
