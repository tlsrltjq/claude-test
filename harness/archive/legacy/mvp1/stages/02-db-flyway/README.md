# Stage 02 — DB/Flyway 기본 스키마 + JPA Entity

## 목적
8개 핵심 테이블의 Flyway 마이그레이션과 JPA Entity/Repository를 만들어, 다음 단계부터 도메인 작업이 가능하게 한다.

## 진입 조건
- 01-skeleton verified

## 핵심 산출물 요약
- `V1__create_base_tables.sql` (Flyway)
- 8개 도메인의 Entity + Repository
- `BaseEntity` (createdAt/updatedAt + JPA Auditing)
- 13개 enum (UserRole, UserStatus, AvailableStatus, DocumentType, DocumentStatus, PermissionType, PermissionTargetType, AuditActionType, AuditTargetType …)

## 절대 하지 말 것
- Service / Controller / 화면 (3단계 이후)
- 회원가입/로그인 로직
- 이메일 인증 토큰 테이블 (3단계에서 V2로)
