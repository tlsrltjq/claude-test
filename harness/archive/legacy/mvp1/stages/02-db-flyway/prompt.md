# Stage 02 — Prompt

> PDF §22 2단계 프롬프트 원본

---

2단계 작업을 진행해줘.

현재 프로젝트는 Java 21, Spring Boot 3.5.x, Gradle 기반의 eActive Resource Hub야.
패키지명은 `com.eactive.resourcehub`야.

이번 단계의 목표는 기능 구현이 아니라 DB/Flyway 기본 스키마와 JPA Entity 구조를 만드는 것이야.

다음 테이블을 Flyway 마이그레이션 SQL로 생성해줘.

1. users
2. teams
3. employee_profiles
4. folders
5. documents
6. document_versions
7. permissions
8. audit_logs

요구사항은 다음과 같아.

- `src/main/resources/db/migration/V1__create_base_tables.sql` 파일을 만들어줘.
- 모든 주요 테이블에는 `id`, `created_at`, `updated_at`을 포함해줘.
- `id`는 BIGSERIAL 또는 PostgreSQL에 적합한 자동 증가 방식을 사용해줘.
- `users`는 `login_id`, `password`, `name`, `email`, `team_id`, `position`, `role`, `status`, `email_verified`를 가진다.
- `teams`는 `name`, `description`을 가진다.
- `employee_profiles`는 `user_id`, `job_title`, `career_summary`, `skills`, `available_status`를 가진다.
- `folders`는 `owner_user_id`, `folder_name`을 가진다.
- `documents`는 `folder_id`, `document_type`, `title`, `current_version_id`, `status`를 가진다.
- `document_versions`는 `document_id`, `version_no`, `original_file_name`, `stored_file_name`, `storage_path`, `preview_file_name`, `preview_storage_path`, `file_size`, `content_type`, `checksum`, `uploaded_by`를 가진다.
- `permissions`는 `user_id`, `permission_type`, `target_type`, `target_id`, `granted_by`를 가진다.
- `audit_logs`는 `user_id`, `action_type`, `target_type`, `target_id`, `reason`, `ip_address`, `user_agent`를 가진다.
- 외래키를 적절히 설정해줘.
- 자주 조회할 컬럼에는 인덱스를 추가해줘.
- `email`, `login_id`는 중복되지 않게 unique 제약을 걸어줘.

그리고 JPA Entity도 만들어줘.

패키지 구조는 다음 기준으로 해줘.

- `common.entity.BaseEntity`
- `user.entity.User`
- `user.entity.UserRole`
- `user.entity.UserStatus`
- `team.entity.Team`
- `employee.entity.EmployeeProfile`
- `employee.entity.AvailableStatus`
- `document.entity.Folder`
- `document.entity.Document`
- `document.entity.DocumentVersion`
- `document.entity.DocumentType`
- `document.entity.DocumentStatus`
- `permission.entity.Permission`
- `permission.entity.PermissionType`
- `permission.entity.PermissionTargetType`
- `audit.entity.AuditLog`
- `audit.entity.AuditActionType`
- `audit.entity.AuditTargetType`

각 Entity는 `BaseEntity`를 상속하게 해줘.
`BaseEntity`에는 `createdAt`, `updatedAt`을 두고 JPA Auditing을 사용해줘.

각 Entity에 대한 Repository도 만들어줘.
아직 Service, Controller, 화면은 만들지 마.
