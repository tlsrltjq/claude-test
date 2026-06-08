# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V230.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 21/21 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓
- 531개 테스트 전 통과 ✓

## 이전 세션에서 멈춘 곳
2026-06-08: 로컬 Docker 환경 초기 세팅 완료. .env 구성(PostgreSQL·R2·Gmail SMTP·관리자 계정), V209 시드 마이그레이션 삭제(불필요한 하드코딩 제거), 빈 DB에서 앱 정상 기동 확인(http://localhost:8080).

완료 항목:
- feat: V230__add_expiry_notice_tracking.sql — documents.expiry_warn_sent_at, expired_notice_sent_at 추가 + 배포 백필(기존 임박·만료 문서 발송 처리)
- feat: Document.expiryWarnSentAt/expiredNoticeSentAt 필드 + mark 메서드, updateExpiresAt 시 이력 초기화
- feat: DocumentRepository.findExpiringSoonNeedingWarn/findExpiredNeedingNotice 알림 전용 쿼리 추가 (관리자 목록용 findExpired/findExpiringSoon은 유지)
- feat: DocumentExpiryService.sendExpiryNotifications — 임박 1회+만료 1회 고정, 발송 성공 시 마킹·실패 시 재시도, @Transactional(쓰기)로 변경 (ADR-045)
- test: DocumentExpiryServiceTest — 새 쿼리 모킹 전환 + 발송 성공/실패 시 마킹 검증 추가
- fix: 스케줄러·기동 초기화 예외 가드 — DocumentFileGcService(독립 try-catch)·ProjectStatusScheduler·AdminInitializer에 try-catch + log.error
- refactor: DocumentFileGcScheduler 신설 — 파일 GC 정기실행 분리로 self-invocation @Transactional 우회 해소, runGc/runOrphanScan 시그니처 무변경 (ADR-046)
- docs: HARNESS·data-model·spec·architecture·decisions(ADR-045·046)·CHANGELOG·tasks 최신화
- chore: 미추적 파일 정리 (AGENTS.md, scripts/seed-demo-*.sql 2개 삭제)
- 빌드 BUILD SUCCESSFUL, 보안 린트 21/21 PASS, 테스트 531개 전 통과

리팩터링 미진행 후보(사용자에게 보고만 함): AdminController 분리(god controller), Flash 메시지 헬퍼, userDetails 추출 — 필요 시 재논의

## 다음 작업 (백로그)

| 순서 | 작업 | 비고 |
|------|------|------|
| 1 | 고정 도메인 구매·적용 | 도메인 확정 후 Caddyfile·application-prod.yml·HSTS 설정 연동 |
| 2 | NAS 저장소 연동 | 현재 LocalFileStorage → NAS 마운트 경로 또는 SMB/NFS 연동으로 교체 (`docs/archive/storage-nas-migration.md` 참고) |
