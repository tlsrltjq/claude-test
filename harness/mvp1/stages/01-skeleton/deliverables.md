# Stage 01 — Deliverables

## 새로 생성되는 파일
- `eactive-resource-hub/build.gradle`
- `eactive-resource-hub/settings.gradle`
- `eactive-resource-hub/src/main/resources/application.yml`
- `eactive-resource-hub/docker-compose.yml`
- `eactive-resource-hub/.env.example`
- `eactive-resource-hub/.gitignore`
- `eactive-resource-hub/README.md`
- `eactive-resource-hub/src/main/java/com/eactive/resourcehub/ResourceHubApplication.java`
- `eactive-resource-hub/src/main/java/com/eactive/resourcehub/HomeController.java` (또는 common 안에)

## 새로 생성되는 패키지
- `com.eactive.resourcehub.common.config`
- `com.eactive.resourcehub.common.exception`
- `com.eactive.resourcehub.common.security`
- `com.eactive.resourcehub.common.file`
- `com.eactive.resourcehub.user`
- `com.eactive.resourcehub.team`
- `com.eactive.resourcehub.employee`
- `com.eactive.resourcehub.document`
- `com.eactive.resourcehub.permission`
- `com.eactive.resourcehub.audit`

## 새로 생성되는 엔드포인트
- `GET /health` → 200 OK with body `"OK"`

## DB
- (없음) Flyway 의존성만, 마이그레이션 파일은 없음
