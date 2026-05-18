# Stage 01 — 프로젝트 골격

## 목적
실행 가능한 Spring Boot 3.5.x / Gradle / Java 21 프로젝트 골격을 만들고, 로컬 PostgreSQL이 Docker로 뜨고, `/health`가 OK를 반환할 때까지.

## 진입 조건
- 없음 (첫 단계)

## 핵심 산출물 요약
- `build.gradle`, `settings.gradle`, `application.yml`
- `docker-compose.yml`, `.env.example`, `.gitignore`, `README.md`
- 패키지 골격 (`common.config/exception/security/file`, `user/team/employee/document/permission/audit`)
- `HomeController`와 `/health` 엔드포인트

## 절대 하지 말 것
- 회원가입/로그인/이메일 인증 (3단계)
- DB 스키마/Flyway (2단계)
- 파일 업로드 (5단계)
- 화면 (대시보드 등) — `/health`만
