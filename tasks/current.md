# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V227.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 18/18 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓

## 이전 세션에서 멈춘 곳
2026-06-01: 허용 이메일 등록 폼 분리 완료.

완료 항목:
- feat: V228__allowed_emails_initial_role.sql — initial_role 컬럼 추가
- feat: AllowedEmail.initialRole 필드 + create() 시그니처 변경
- feat: EmailAllowlistService add/addBulk/addBulkFromExcel에 initialRole 파라미터 추가
- feat: SignupService.completeSignup — allowedEmail.initialRole 읽어 SALES 등 자동 부여
- feat: AdminController 엔드포인트 initialRole 파라미터 + activeTab flash
- feat: allowed-emails.html 일반/영업 탭 분리, 목록 배지, 탭 복원
- 빌드 BUILD SUCCESSFUL, 보안 린트 18/18 PASS, 테스트 508개 전 통과

**다음 작업 없음 — 사용자 지시 대기**
