# M3-14 — 보안·성능·UX 최종 품질

## 목적

MVP 기능 완성 후 전체 코드베이스 감사(audit) 결과를 자동화된 검증으로 고정한다.  
이 스테이지가 통과되면 보안·성능·UX 기본 기준을 충족한 것으로 간주한다.

## 검사 항목

| 범주 | 항목 |
|------|------|
| 보안 헤더 | SecurityConfig `.headers()` 블록, X-Frame-Options DENY, nosniff, CSP |
| 로그 보안 | PasswordResetService 재설정 코드 로그 제거 |
| 환경변수 | application.yml 비밀번호 기본값 하드코딩 금지 |
| 에러 페이지 | 커스텀 404 / 403 / 500 Thymeleaf 템플릿 |
| 예외 처리 | GlobalExceptionHandler ResponseStatusException + Exception 핸들러 |
| 비동기 | ThumbnailService @Async, @EnableAsync 선언 |
| 페이지네이션 | employees.html 서버사이드 페이지 파라미터 |

## 관련 문서

- `docs/security-policy.md` §7 HTTP 보안 헤더  
- `docs/security-policy.md` §8 환경변수 기본값 정책  
- `docs/security-policy.md` §9 비동기 처리 정책
