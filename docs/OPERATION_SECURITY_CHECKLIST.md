# 운영 보안 체크리스트

배포 전·정기 점검에 사용하는 체크리스트.

## 배포 전 확인

- [ ] `.env` 파일이 git에 포함되어 있지 않음 (`.gitignore` 확인)
- [ ] `POSTGRES_PASSWORD` 환경변수 설정됨 (기본값 없음)
- [ ] `RESOURCEHUB_ADMIN_PASSWORD` 환경변수 설정됨 (기본값 없음)
- [ ] `docker-compose.prod.yml` 사용 (개발용 `docker-compose.yml` 미사용)
- [ ] `SPRING_PROFILES_ACTIVE=prod` 설정됨
- [ ] 앱 포트(8080)가 직접 외부 노출되지 않고 리버스 프록시(nginx/caddy) 경유
- [ ] HTTPS 인증서 적용 및 세션 쿠키 `Secure=true` 확인

## 백업

- [ ] `bash scripts/backup-db.sh` 크론탭 등록 (일 1회 이상)
- [ ] `bash scripts/backup-uploads.sh` 크론탭 등록
- [ ] 백업 파일 외부 스토리지 이전 확인
- [ ] `backups/` 디렉터리가 서버 외부(S3, NAS 등)에 이중 보관

## 정기 점검

- [ ] `bash scripts/security-lint.sh` 통과 (0 FAIL)
- [ ] 만료 문서 현황 확인 (`/admin/documents/expiry`)
- [ ] 감사 로그 이상 징후 확인 (`audit_logs` 테이블)
- [ ] PostgreSQL 18 및 Spring Boot 패치 버전 최신 유지
