# Stage 10 — Context

## SSOT
PROJECT_SPEC §15 설정 방식, §20 보안 기준, §21 백업 정책.

## 이전 단계 결과
- 모든 기능 구현 완료.
- 로컬에서 동작하는 application.yml, docker-compose.yml.

## 이번 단계 핵심 제약
- **기능 추가/변경 금지.** 환경 분리, 운영 자산만.
- 운영 시 secure=true, 로컬은 false. application-{local,prod}.yml 분리로 처리.
- 백업 스크립트는 cron 예시 정도까지 README에 적어도 됨 (실제 등록은 운영 단계에서).
- 14일 보관 (예: `find $BACKUP_DIR -type f -mtime +14 -delete`).
- `OPERATION_SECURITY_CHECKLIST.md`는 docs/ 아래.

## 코드가 들어갈 위치
- 기존: `application.yml` 분리 → `application.yml` (공통) + `application-local.yml` + `application-prod.yml`
- 신규: `docker-compose.prod.yml`
- 신규: `scripts/backup-db.sh`, `scripts/backup-uploads.sh`
- 신규: `docs/OPERATION_SECURITY_CHECKLIST.md`
- README.md 보강
