# MVP2 Stage 03 — Acceptance

## 자동 검증
- [ ] `V102__add_profile_fields_to_users.sql` 존재 + birth_date/phone 컬럼 추가
- [ ] `user/entity/Position.java` 존재 + 8개 enum 값
- [ ] `User.java` 에 `birthDate`, `phone`, `Position position`
- [ ] `templates/signup.html` 에 birth date 입력 필드(`type="date"` 또는 동급)
- [ ] `templates/signup.html` 에 직급 select
- [ ] `templates/signup.html` 에 회사 도메인 suffix 표시 (eactive.co.kr)
- [ ] `templates/signup-verify.html` 에서 email 입력 input 부재 (또는 hidden 만)

## 수동 검증
- [ ] `./gradlew bootRun` → V102 적용
- [ ] 회원가입 → 새 필드 모두 입력 후 가입 가능
- [ ] 이메일 앞부분만 입력 → 저장 시 `@eactive.co.kr` 붙어서 저장
- [ ] 비밀번호 `1234` 입력 → 거부, `Abcd1234` 입력 → 거부 (특수문자 없음), `Abcd123!` 입력 → 통과
- [ ] 인증 화면에서 이메일 입력란 없음, 6자리 코드만
- [ ] 직급 select 8개 옵션 모두 노출
- [ ] DB 직접 조회 → 신규 사용자에 birth_date/phone 채워짐, position이 enum 값
- [ ] 기존 사용자(예: admin) 로그인 그대로 됨

## NOT-DOING
- [ ] employee_profiles 변경 없음 (developer_grade는 04에서)
- [ ] /sales/profiles 표 없음
- [ ] 검색/필터/정렬 없음

## MVP1 회귀
```bash
bash mvp2/harness/scripts/verify.sh 03 --with-mvp1
```
mvp1 3단계(인증)는 이메일 인증 화면이 변했으므로 일부 수동 검증 항목이 의도된 차이임.
