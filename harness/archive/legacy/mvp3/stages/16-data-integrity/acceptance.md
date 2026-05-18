# M3-16 합격 기준

## FAIL 없어야 하는 항목

- [ ] `Collectors.toMap()` 2인수 호출 없음 (머지 함수 필수)
- [ ] `invalidSessionUrl()` SecurityConfig에 없음
- [ ] `application-prod.yml` Secure=true 시 docker-compose.yml에 HTTP 오버라이드 존재
- [ ] `folders` 테이블 `(owner_user_id, type)` 중복 없음

## WARN 허용 (경고만)

- [ ] 일부 사용자 PERSONAL 폴더 누락 (신규 가입자 처리 중인 경우 허용)
- [ ] `current_version_id` NULL 비율 10% 이하
- [ ] DB 연결 불가 시 런타임 검사 생략 (WARN 처리)
