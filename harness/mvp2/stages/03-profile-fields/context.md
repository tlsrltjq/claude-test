# MVP2 Stage 03 — Context

## SSOT
`mvp2/docs/PROJECT_SPEC_MVP2.md` §5 회원가입/프로필 입력 확장.
`mvp2/docs/MIGRATION_FROM_MVP1.md` §5, §7.

## 이전 단계 결과
- 권한 단순화 완료
- 다운로드 정책 변경 완료, 관리자 삭제 추가됨

## 이번 단계 핵심 제약
- 기존 사용자 데이터를 보호 (NOT NULL 추가 시 default 사용)
- 이메일 자동 조합 — 회사 도메인은 application.yml의 `resourcehub.company-email-domain` 사용 (이미 있음)
- 비밀번호 정책 검증은 서버 우선, 클라이언트는 보조 힌트
- ddl-auto=validate 모드라 Entity 변경과 마이그레이션이 정확히 일치해야 함

## 코드가 들어갈 위치
- 마이그레이션: `V102__add_profile_fields_to_users.sql`
- enum: `user/entity/Position.java`
- entity: `user/entity/User.java`
- DTO: `user/dto/SignupForm.java` (이미 있을 것 — 확장)
- service: `user/service/SignupService.java` (검증 로직)
- 템플릿: `templates/signup.html`, `templates/signup-verify.html`
