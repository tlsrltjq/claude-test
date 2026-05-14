# Stage 02 — Deliverables

## SQL
- `src/main/resources/db/migration/V1__create_base_tables.sql` — 8개 테이블 + 인덱스 + FK + unique 제약

## Entity
- `common/entity/BaseEntity.java`
- `user/entity/User.java`, `UserRole.java`, `UserStatus.java`
- `team/entity/Team.java`
- `employee/entity/EmployeeProfile.java`, `AvailableStatus.java`
- `document/entity/Folder.java`, `Document.java`, `DocumentVersion.java`, `DocumentType.java`, `DocumentStatus.java`
- `permission/entity/Permission.java`, `PermissionType.java`, `PermissionTargetType.java`
- `audit/entity/AuditLog.java`, `AuditActionType.java`, `AuditTargetType.java`

## Repository
- 각 Entity별 `*Repository extends JpaRepository<Entity, Long>`
  - `UserRepository`, `TeamRepository`, `EmployeeProfileRepository`, `FolderRepository`, `DocumentRepository`, `DocumentVersionRepository`, `PermissionRepository`, `AuditLogRepository`

## Configuration
- `common/config/JpaAuditingConfig.java` (@EnableJpaAuditing)
