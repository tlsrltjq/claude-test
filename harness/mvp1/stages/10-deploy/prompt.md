# Stage 10 — Prompt

> PDF §22 10단계 프롬프트 원본

---

10단계 작업을 진행해줘.

이전 단계까지: 골격, DB, 인증, 관리자 승인, 팀/직원/폴더, 업로드, 미리보기/다운로드, 권한, 썸네일/카드뷰, 문서 승인/반려.

이번 단계의 목표는 로컬에서 개발한 프로젝트를 나중에 회사 VM 또는 내부망 서버로 옮길 수 있도록 운영 설정, 보안 설정, 백업 스크립트, 배포 가이드를 정리하는 것이야.

중요:
- 새로운 업무 기능을 추가하지 마.
- 운영 준비와 배포 가능성 정리에 집중해.
- 업로드 파일을 정적 리소스로 공개하지 않는 구조는 유지해.
- 운영 비밀번호나 민감정보를 코드에 직접 쓰지 마.
- .env 파일은 Git에 올라가지 않게 해.
- JWT는 사용하지 말고 기존 세션 기반 인증을 유지해.

요구사항:

1. 설정 파일 분리: `application.yml` + `application-local.yml` + `application-prod.yml`. 로컬 기본 profile=local. 운영 SPRING_PROFILES_ACTIVE=prod.

2. application-local.yml — 로컬 PostgreSQL, `./storage/uploads`, `eactive.co.kr`, secure=false, ConsoleEmailSender 사용.

3. application-prod.yml — DB 환경변수, RESOURCEHUB_UPLOAD_BASE_DIR(기본 `/data/eactive-resource-hub/uploads`), secure=true, timeout 30m, 로그 `/data/eactive-resource-hub/logs/application.log`.

4. 세션/쿠키 운영: 쿠키 RESOURCEHUB_SESSION/httpOnly/sameSite=strict/secure(prod만). README에 HTTPS 필요 명시.

5. `docker-compose.prod.yml` — resourcehub-app + postgres, /data/...uploads, /data/...logs 볼륨, 환경변수 기반, restart unless-stopped, 비밀번호는 .env.

6. `.env.example` 보강: POSTGRES_PASSWORD, RESOURCEHUB_ADMIN_EMAIL, RESOURCEHUB_ADMIN_PASSWORD, RESOURCEHUB_UPLOAD_BASE_DIR, RESOURCEHUB_COMPANY_EMAIL_DOMAIN, SPRING_DATASOURCE_URL/USERNAME/PASSWORD. 실값 금지.

7. `.gitignore` 점검 — .env, storage/, logs/, backups/, *.log.

8. 백업 스크립트.
   - `scripts/backup-db.sh` — pg_dump.
   - `scripts/backup-uploads.sh` — uploads tar.gz.
   - 기본 경로 `/data/eactive-resource-hub/backups`. 14일 지난 파일 삭제.

9. 로그 설정 — application-prod.yml 또는 logback. 파일 크기/보관 기간(예: 10MB, 30개/일). 비밀번호/인증코드/민감 경로는 로그에 안 남도록 README 주의.

10. `docs/OPERATION_SECURITY_CHECKLIST.md` — 기본 관리자 비밀번호 변경, 운영 DB 비밀번호, .env Git 제외, HTTPS, secure=true, 내부망/VPN, 업로드 폴더 직접 접근 차단, 운영 ConsoleEmailSender 금지, 관리자 권한 최소화, 퇴사자 계정 비활성화, DB/파일 백업 + 복구 테스트, 로그 민감정보 금지.

11. README에 운영 배포 가이드 — `/opt/eactive-resource-hub`, `/data/.../uploads,logs,backups` 디렉터리 생성 → .env → docker-compose.prod.yml → 접속 확인 → 관리자 비밀번호 변경 → 백업 실행 → VPN 차단 확인 순.

12. README에 운영 점검 체크리스트 — 실행/DB/로그인/업로드/미리보기/다운로드 로그/백업/접근 차단/업로드 폴더 차단.

13. 기존 기능은 변경하지 마.

14. 아직 만들지 마: 실제 VM 배포, 실제 SMTP, 실제 사내 LDAP/SSO, 실제 NAS/S3, 무중단 배포, 모니터링, 워터마크, DOCX/HWP 자동 PDF 변환.
