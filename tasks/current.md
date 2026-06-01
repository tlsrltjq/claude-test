# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V229.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 18/18 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓

## 이전 세션에서 멈춘 곳
2026-06-01: 개인정보 동의 기록 + 문서 최신화 완료.

완료 항목:
- feat: V228__allowed_emails_initial_role.sql — initial_role 컬럼 추가
- feat: AllowedEmail.initialRole 필드 + create() 시그니처 변경
- feat: EmailAllowlistService add/addBulk/addBulkFromExcel에 initialRole 파라미터 추가
- feat: SignupService.completeSignup — allowedEmail.initialRole 읽어 SALES 등 자동 부여
- feat: AdminController 엔드포인트 initialRole 파라미터 + activeTab flash
- feat: allowed-emails.html 일반/영업 탭 분리, 목록 배지, 탭 복원
- feat: V229__add_privacy_consent_to_users.sql — privacy_consent_at, privacy_consent_version 추가
- feat: User.privacy_consent_at/version 필드 + recordConsent() 메서드
- feat: SignupService.CONSENT_VERSION = "1.0" + completeSignup() 시 recordConsent() 호출
- test: SignupServiceTest — completeSignup_동의일시와_버전이_저장된다 추가
- docs: HARNESS·data-model·architecture·decisions·spec·testing·CHANGELOG·tasks V229 기준 최신화
- test: SalesMemberServiceTest 20케이스 — 만료 필터링 회귀 보호, findActiveMembers, autofill 전 케이스
- 빌드 BUILD SUCCESSFUL, 보안 린트 18/18 PASS, 테스트 512개 전 통과

**다음 작업 없음 — 사용자 지시 대기**
