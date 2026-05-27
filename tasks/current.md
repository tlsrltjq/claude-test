# 현재 작업 컨텍스트

## 지금 단계: 기능 개편 — 회원가입·설정·폴더·인증서·통계

## 목표
- [ ] V214 users.address 컬럼 추가
- [ ] V215 allowed_emails 테이블 신설 (이메일 사전등록 방식)
- [ ] 회원가입 폼 개편 (전체 이메일 입력, 주소, 개인정보 동의 모달, 허용 이메일 검증)
- [ ] 비밀번호 찾기 폼 개편 (전체 이메일 입력)
- [ ] 설정 화면 (이름·주소·팀 편집, 아이디 중복 제거, 상태 제거)
- [ ] 관리자 화면 허용 이메일 관리 UI 추가
- [ ] 대시보드 간소화 + B안 통합 워크스페이스(/workspace) 신설
- [ ] 공유 폴더(Permission 기반) 제거, 공용 폴더만 유지
- [ ] 대시보드 다운로드 이력 카드 제거 (라우트는 유지)
- [ ] 재직증명서 레이아웃 개선 + 템플릿 생성 버튼 제거
- [ ] 통계 화면 확장 (AuditActionType 확인 후)

## 현재 진행 중
- [x] V214 SQL 파일 작성
- [x] V215 SQL 파일 작성
- [x] User.java address 필드 추가
- [ ] User.updateProfile() 시그니처 확장 (name, address 추가)
- [ ] allowed_emails 엔티티/리포지토리/서비스 작성
- [ ] SignupRequest·SignupService 개편 (도메인 제거, 허용 이메일 검증)
- [ ] 나머지 항목들

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V213.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준
- `bash scripts/security-lint.sh` 15/15 PASS
- `./gradlew build` BUILD SUCCESSFUL
- 모든 기능이 실제 화면에서 동작 확인

## 이전 세션에서 멈춘 곳
2026-05-27: 영향도 분석 완료, 구현 시작.
V214·V215 SQL 작성, User.java address 필드 추가까지 완료.
