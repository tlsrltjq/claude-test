# eActive Resource Hub

회사 내부 직원 문서 관리 포털.
직원별 이력서·경력기술서·졸업증명서·자격증·기타 증빙자료를 한곳에서 관리한다.

## 기술 스택

- Java 21
- Spring Boot 3.5.x
- Gradle
- PostgreSQL 18 (Docker)
- Spring Data JPA / Spring Security (세션 인증, JWT 미사용) / Thymeleaf / Validation / Flyway
- Lombok
- 파일 저장: 로컬 디스크 (UUID 파일명, DB는 메타데이터만)

## 디렉터리

```
eactive-resource-hub/
├─ src/
│  └─ main/
│     ├─ java/com/eactive/resourcehub/
│     │  ├─ ResourceHubApplication.java
│     │  ├─ HomeController.java          (/health → "OK")
│     │  ├─ common/{config,exception,security,file}
│     │  ├─ user, team, employee, document, permission, audit
│     └─ resources/
│        ├─ application.yml
│        └─ db/migration/                (V1은 2단계에서 추가)
├─ build.gradle / settings.gradle
├─ docker-compose.yml
├─ .env.example
└─ .gitignore
```

## 1단계 — 골격 실행/검증

### 사전 준비

```bash
cp .env.example .env
# 필요시 POSTGRES_PASSWORD 등을 수정
```

### PostgreSQL 띄우기

```bash
docker compose up -d postgres
docker compose ps           # resourcehub-postgres 가 healthy 또는 running
```

### 애플리케이션 실행

```bash
./gradlew bootRun
# 또는 IntelliJ에서 ResourceHubApplication.java 실행
```

### Health 확인

```bash
curl -i http://localhost:8080/health
# HTTP/1.1 200
# OK
```

### 환경변수 치환 확인

```bash
RESOURCEHUB_UPLOAD_BASE_DIR=/tmp/foo ./gradlew bootRun
# 로그/설정에 /tmp/foo 가 반영되는지 확인
```

## 다음 단계

`../harness/scripts/start.sh 02` 부터 진행. 2단계에서 8개 테이블 + JPA Entity가 들어온다.

## 보안 주의 (모든 단계 공통)

- JWT 사용 금지. Spring Security 세션 기반.
- 운영에서는 HTTPS + 세션 쿠키 secure=true 필요 (현재는 로컬 골격이라 secure 미설정).
- `.env`, `storage/`, `logs/`는 Git에 들어가면 안 된다.
- 운영 전환 전 기본 관리자 비밀번호 반드시 변경.
