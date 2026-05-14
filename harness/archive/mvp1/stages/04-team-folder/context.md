# Stage 04 — Context

## SSOT
PROJECT_SPEC §4 핵심 사용자, §5.10 관리자 권한 부여, §18 권한 정책.

## 이전 단계 결과
- 회원가입/인증/로그인 동작.
- 승인 대기 사용자가 존재할 수 있음 (`PENDING_ADMIN_APPROVAL`).
- 기본 관리자 계정이 ACTIVE로 자동 생성되어 있음.

## 이번 단계 핵심 제약
- 새 마이그레이션이 필요하면 V3로. 다만 이 단계에서는 기본 팀 시드 정도라 SQL이 아니라 `CommandLineRunner`로 처리해도 됨.
- 폴더 자동 생성은 트랜잭션 안에서: 승인 + 폴더 생성이 한 트랜잭션.
- 승인 시 부여하는 직급은 `users.position` 컬럼 활용.
- 권한 검사는 Service 단에서. Controller에서는 `@PreAuthorize` 또는 `SecurityConfig`의 `requestMatchers` 둘 다 가능 — 단 `/admin/**`는 명시적으로 ADMIN.

## 코드가 들어갈 위치
- `user/`, `team/`, `employee/`, `document/` (Folder는 document 패키지).
- 화면: `templates/admin/` 하위에 정리.
