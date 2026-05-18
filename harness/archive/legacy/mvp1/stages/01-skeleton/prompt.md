# Stage 01 — Prompt

> 이 prompt를 AI에게 그대로 전달한다. (PDF §22 1단계 프롬프트 원본)

---

Java 21, Spring Boot 3.5.x, Gradle 기반으로 eActive Resource Hub 프로젝트의 기본 골격을 만들어줘.

프로젝트 목적은 회사 내부 직원 문서 관리 포털이야.
패키지명은 `com.eactive.resourcehub`로 해줘.

사용 기술은 다음과 같아.
- Spring Web
- Spring Data JPA
- Spring Security
- Thymeleaf
- Validation
- Flyway
- PostgreSQL Driver
- Lombok

아직 회원가입, 로그인, 파일 업로드 같은 실제 기능은 만들지 말고, 프로젝트가 실행 가능한 기본 구조만 만들어줘.

다음 항목을 포함해줘.
1. `build.gradle`
2. `settings.gradle`
3. `application.yml`
4. `docker-compose.yml`
5. `.env.example`
6. `.gitignore`
7. `README.md`
8. 기본 패키지 구조
   - `common.config`
   - `common.exception`
   - `common.security`
   - `common.file`
   - `user`
   - `team`
   - `employee`
   - `document`
   - `permission`
   - `audit`
9. 기본 `HomeController`
10. `/health` 접속 시 `"OK"`를 반환하는 간단한 확인용 엔드포인트

DB는 로컬 Docker PostgreSQL을 사용하고, 업로드 경로는 `application.yml`에서 `resourcehub.upload.base-dir` 값으로 관리하게 해줘.

로컬 기본 업로드 경로는 `./storage/uploads`로 잡아줘.
운영에서는 환경변수 `RESOURCEHUB_UPLOAD_BASE_DIR`로 바꿀 수 있게 해줘.
