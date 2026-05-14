# Stage 10 — Acceptance

## 자동 검증 (verify.sh)
- [ ] `application-local.yml`, `application-prod.yml` 존재
- [ ] `docker-compose.prod.yml` 존재
- [ ] `scripts/backup-db.sh`, `scripts/backup-uploads.sh` 존재
- [ ] `docs/OPERATION_SECURITY_CHECKLIST.md` 존재
- [ ] `.env.example`에 8개 운영 키 모두 포함
- [ ] `.gitignore`에 `backups/` 추가
- [ ] application-prod.yml에 secure=true 흔적

## 수동 검증
- [ ] `SPRING_PROFILES_ACTIVE=local`로 실행 → 로컬 정상 동작
- [ ] `SPRING_PROFILES_ACTIVE=prod`로 실행 (DB/uploads 환경변수 주입) → 정상 동작
- [ ] secure=true가 prod에만 적용되는지 (로컬에서 secure=false로 쿠키 발급되는지 확인)
- [ ] backup-db.sh 실행 → backups/ 아래 dump 파일 생성
- [ ] backup-uploads.sh 실행 → backups/ 아래 tar.gz 생성
- [ ] 14일 지난 더미 파일을 만들고 스크립트 실행 시 삭제되는지 (수동 테스트 가능)
- [ ] OPERATION_SECURITY_CHECKLIST.md 의 항목들이 다 들어갔는지 (기본 관리자 비번, .env Git 제외, HTTPS, secure, VPN, 업로드 폴더 차단, ConsoleEmailSender 금지, 백업 + 복구 테스트, 민감정보 로그 금지 등)
- [ ] README의 운영 배포 가이드 순서가 PDF §22 10단계 11번과 일치
- [ ] 기존 기능 회귀 없음 — 1~9단계 acceptance가 여전히 통과

## NOT-DOING 확인
- [ ] 실제 VM 배포 안 함
- [ ] 실제 SMTP 연동 안 함
- [ ] 실제 LDAP/SSO 안 함
- [ ] 실제 NAS/S3 안 함
- [ ] 무중단 배포 / 모니터링 안 함
- [ ] 워터마크 / DOCX·HWP 자동 PDF 변환 안 함
