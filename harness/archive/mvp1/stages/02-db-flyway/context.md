# Stage 02 — Context

## SSOT
`docs/PROJECT_SPEC.md` §16 패키지 구조, §17 DB 테이블, §18 권한 정책.

## 이전 단계 결과
- 빈 패키지 골격이 잡혀있다.
- Spring Boot 부팅 가능, `/health` OK.
- Flyway 의존성은 들어있지만 `db/migration/` 아래 파일은 없음.

## 이번 단계 핵심 제약
- **Service/Controller/View 만들지 말 것.** 오직 SQL + Entity + Repository.
- `BaseEntity`는 `@MappedSuperclass`로, JPA Auditing(`@EntityListeners(AuditingEntityListener.class)`)와 `@CreatedDate`/`@LastModifiedDate` 사용.
- `@EnableJpaAuditing`은 `common.config`에 별도 Configuration으로.
- enum은 DB 컬럼에 `VARCHAR`로 저장 (`@Enumerated(EnumType.STRING)`).
- 외래키:
  - `users.team_id → teams.id` (NULL 허용)
  - `employee_profiles.user_id → users.id`
  - `folders.owner_user_id → users.id`
  - `documents.folder_id → folders.id`
  - `documents.current_version_id → document_versions.id` (NULL 허용 — 첫 업로드 전)
  - `document_versions.document_id → documents.id`
  - `document_versions.uploaded_by → users.id`
  - `permissions.user_id → users.id`, `permissions.granted_by → users.id`
  - `audit_logs.user_id → users.id`
- 인덱스 후보: `users(email)`, `users(team_id)`, `documents(folder_id)`, `document_versions(document_id)`, `audit_logs(user_id, created_at)`, `audit_logs(target_type, target_id)`, `permissions(user_id, target_type, target_id)`.
- `documents.current_version_id`는 self-referencing 회피 위해 FK는 deferred constraint나 nullable + post-insert update.

## 코드가 들어갈 위치
- SQL: `eactive-resource-hub/src/main/resources/db/migration/V1__create_base_tables.sql`
- Entity/Repository: 각 도메인 패키지 아래 `entity/`, `repository/`
