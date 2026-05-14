# Stage 01 — Acceptance

> 모든 항목이 [x]가 되어야 다음 단계로 진행한다.

## 자동 검증 (verify.sh)
- [ ] `build.gradle` 존재
- [ ] `settings.gradle` 존재
- [ ] `application.yml` 존재
- [ ] `docker-compose.yml` 존재
- [ ] `.env.example` 존재
- [ ] `.gitignore` 존재 + `.env`, `storage/`, `logs/`, `*.log` 포함
- [ ] `ResourceHubApplication.java` 존재
- [ ] 패키지 디렉토리 10개 존재 (common.{config,exception,security,file}, user, team, employee, document, permission, audit)

## 수동 검증
- [ ] `docker compose up -d postgres` → PostgreSQL 컨테이너 정상 기동
- [ ] `./gradlew bootRun` 또는 IDE 실행으로 애플리케이션 부팅 성공
- [ ] `curl http://localhost:8080/health` → `OK` 응답 + 200
- [ ] `application.yml`의 `resourcehub.upload.base-dir`가 `${RESOURCEHUB_UPLOAD_BASE_DIR:./storage/uploads}` 형태
- [ ] Spring Boot 버전이 3.5.x, Java 21 사용
- [ ] Lombok 어노테이션 처리 정상 (간단한 엔티티 만들어 컴파일 가능)
- [ ] `RESOURCEHUB_UPLOAD_BASE_DIR=/tmp/foo`로 환경변수 주고 실행하면 application.yml 로그/설정값에 반영되는지 확인
