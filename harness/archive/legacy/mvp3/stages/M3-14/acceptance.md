# M3-14 합격 기준

## 필수 (PASS 필요)

- [ ] `SecurityConfig.java` 에 `.headers()` 블록이 존재한다
- [ ] `X-Frame-Options: DENY` 설정이 코드에 있다
- [ ] `X-Content-Type-Options: nosniff` 설정이 코드에 있다
- [ ] `Content-Security-Policy` 헤더가 설정되어 있다
- [ ] `PasswordResetService.java` 에서 `code={}` 로그 라인이 없다
- [ ] `application.yml` 에 `:Admin` / `:Password` 형식의 환경변수 기본값이 없다
- [ ] `templates/error/404.html`, `403.html`, `500.html` 이 존재한다
- [ ] `GlobalExceptionHandler` 에 `ResponseStatusException` 핸들러가 있다
- [ ] `GlobalExceptionHandler` 에 `Exception` 핸들러가 있다
- [ ] `ThumbnailService.java` 에 `@Async` 가 선언되어 있다
- [ ] `@EnableAsync` 가 어딘가에 선언되어 있다
- [ ] `employees.html` 에 페이지 파라미터(pagination) 관련 코드가 있다
