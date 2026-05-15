# 배포 체크리스트

> 작성일: 2026-05-15  
> 평가 기준: 보안 / 환경변수 / 에러 처리 / 성능 / 인프라

---

## 1단계 — 배포 블로커 (필수, 먼저 해결)

배포 전 반드시 완료해야 합니다. 미완료 시 배포 불가 또는 보안 사고 위험.

- [x] **HTTPS / SSL 설정** `보안`  
  Caddy 리버스 프록시 + Let's Encrypt 자동 인증서. `CADDY_DOMAIN` 환경변수로 도메인 주입.  
  `docker-compose.prod.yml`에 caddy 서비스 추가, `application-prod.yml`에 forward-headers-strategy 설정 완료.

- [x] **운영용 compose 파일 사용 확인** `인프라`  
  `scripts/deploy.sh` 생성 — 항상 `docker-compose.prod.yml` 사용, 필수 env 검증 및 SEED 계정 경고 포함.  
  `docker-compose.yml` 상단에 개발 전용 경고 주석 추가. README 배포 섹션 업데이트.

- [ ] **`POSTGRES_PASSWORD` 설정** `환경변수`  
  서버 `.env` 또는 환경변수에 미리 설정 필요.  
  미설정 시 앱 기동 불가.

- [ ] **`RESOURCEHUB_ADMIN_PASSWORD` 설정** `환경변수`  
  초기 관리자 계정 생성에 필요.  
  미설정 시 앱 기동 불가.

- [ ] **`RESOURCEHUB_SEED_TEST_PASSWORD` 제거** `보안`  
  운영 환경에 절대 설정 금지.  
  설정 시 SALES 권한 테스트 계정 자동 생성 → 무단 접근 가능. ⚠️

---

## 2단계 — 권장 사항 (배포 후 조기 처리)

서비스 첫날부터 필요할 수 있습니다. 빠르게 처리 권장.

- [x] **SMTP 환경변수 설정** `알림`  
  `EmailSenderConfig` — `@ConditionalOnMissingBean` 패턴으로 SMTP 미설정 시 콘솔 출력으로 자동 폴백.  
  `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD` 설정 시 실제 메일 발송.

- [x] **로그 파일 저장 설정** `운영`  
  `src/main/resources/logback-spring.xml` 생성 — prod 프로파일에서 롤링 파일 appender 활성화.  
  파일: `/data/logs/resourcehub.log`, 50MB 롤링, 30일·1GB 상한.  
  `docker-compose.prod.yml`에 `resourcehub_logs` 볼륨 및 `LOG_DIR=/data/logs` 환경변수 추가.

- [x] **DB 자동 백업 스크립트** `인프라`  
  `scripts/backup-db.sh` (DB), `scripts/backup-uploads.sh` (파일) 이미 존재.  
  `scripts/setup-cron.sh` 생성 — 매일 03:00 DB, 03:30 업로드 크론잡 등록. 30일 자동 삭제.  
  서버에서 `bash scripts/setup-cron.sh` 한 번만 실행하면 됨.

- [x] **nginx/Caddy 설정 파일 문서화** `인프라`  
  `Caddyfile` 레포 포함 완료. `docker-compose.prod.yml`에서 볼륨 마운트.  
  도메인은 `CADDY_DOMAIN` 환경변수로 주입.

---

## 3단계 — 선택 사항 (여유 있을 때)

서비스 안정화 후 검토.

- [ ] **HikariCP 풀 사이즈 튜닝** `성능`  
  현재 기본값(최대 10개). 사용자 수 증가 시 조정 검토.

- [ ] **테스트 코드 보강** `품질`  
  현재 테스트 1개뿐(`CareerCalculatorTest`).  
  주요 비즈니스 로직 커버리지 확대 권장.

- [ ] **애플리케이션 레벨 캐시 도입** `성능`  
  현재 규모에서는 불필요.  
  트래픽 증가 시 Spring Cache + Caffeine 또는 Redis 검토.
