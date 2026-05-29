-- 직원 계정 삭제 기능을 위한 FK 제약 조건 정비
-- 삭제 대상 컬럼들을 nullable로 변경하고 ON DELETE 동작 추가

-- audit_logs.user_id: 감사 로그는 보존하되 actor 참조는 NULL로
ALTER TABLE audit_logs ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE audit_logs DROP CONSTRAINT fk_audit_logs_user_id;
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- document_versions.uploaded_by: 문서 버전은 보존하되 업로더 참조는 NULL로
ALTER TABLE document_versions ALTER COLUMN uploaded_by DROP NOT NULL;
ALTER TABLE document_versions DROP CONSTRAINT fk_document_versions_uploaded_by;
ALTER TABLE document_versions ADD CONSTRAINT fk_document_versions_uploaded_by
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL;

-- permissions: 본인 권한은 함께 삭제, 부여자 참조는 NULL로
ALTER TABLE permissions ALTER COLUMN granted_by DROP NOT NULL;
ALTER TABLE permissions DROP CONSTRAINT fk_permissions_user_id;
ALTER TABLE permissions ADD CONSTRAINT fk_permissions_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE permissions DROP CONSTRAINT fk_permissions_granted_by;
ALTER TABLE permissions ADD CONSTRAINT fk_permissions_granted_by
    FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL;

-- employee_profiles: 프로필은 사용자와 함께 삭제
ALTER TABLE employee_profiles DROP CONSTRAINT fk_employee_profiles_user_id;
ALTER TABLE employee_profiles ADD CONSTRAINT fk_employee_profiles_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- project_assignments: 배정 정보는 사용자와 함께 삭제
ALTER TABLE project_assignments DROP CONSTRAINT project_assignments_user_id_fkey;
ALTER TABLE project_assignments ADD CONSTRAINT project_assignments_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- folders.owner_user_id: 폴더 소유자 참조는 NULL로 (폴더 자체는 GC가 정리)
ALTER TABLE folders ALTER COLUMN owner_user_id DROP NOT NULL;
ALTER TABLE folders DROP CONSTRAINT fk_folders_owner_user_id;
ALTER TABLE folders ADD CONSTRAINT fk_folders_owner_user_id
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE SET NULL;
