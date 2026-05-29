-- ================================================================
-- V226: 계정 삭제 안전 처리
--   1. project_assignments: user_name 스냅샷 + SET NULL (이름 보존)
--   2. 누락된 FK 수정 (삭제 시 오류 방지)
-- ================================================================

-- 1. project_assignments에 이름 스냅샷 컬럼 추가 & 백필
ALTER TABLE project_assignments ADD COLUMN user_name VARCHAR(100);
UPDATE project_assignments pa
    SET user_name = (SELECT name FROM users u WHERE u.id = pa.user_id);

-- user_id: NOT NULL 해제 + CASCADE → SET NULL
ALTER TABLE project_assignments ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE project_assignments DROP CONSTRAINT project_assignments_user_id_fkey;
ALTER TABLE project_assignments ADD CONSTRAINT project_assignments_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- 2. email_verification_tokens: CASCADE (임시 토큰, 삭제 시 같이 제거)
ALTER TABLE email_verification_tokens DROP CONSTRAINT fk_email_verification_tokens_user_id;
ALTER TABLE email_verification_tokens ADD CONSTRAINT fk_email_verification_tokens_user_id
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 3. password_reset_tokens: CASCADE
ALTER TABLE password_reset_tokens DROP CONSTRAINT password_reset_tokens_user_id_fkey;
ALTER TABLE password_reset_tokens ADD CONSTRAINT password_reset_tokens_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 4. column_view_preferences: CASCADE (개인 설정, 계정 삭제 시 제거)
ALTER TABLE column_view_preferences DROP CONSTRAINT column_view_preferences_user_id_fkey;
ALTER TABLE column_view_preferences ADD CONSTRAINT column_view_preferences_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 5. resume_templates.uploaded_by: nullable + SET NULL
ALTER TABLE resume_templates ALTER COLUMN uploaded_by DROP NOT NULL;
ALTER TABLE resume_templates DROP CONSTRAINT resume_templates_uploaded_by_fkey;
ALTER TABLE resume_templates ADD CONSTRAINT resume_templates_uploaded_by_fkey
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL;

-- 6. document_versions.reviewed_by: SET NULL (문서 이력 보존)
ALTER TABLE document_versions DROP CONSTRAINT fk_document_versions_reviewed_by;
ALTER TABLE document_versions ADD CONSTRAINT fk_document_versions_reviewed_by
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL;

-- 7. documents.deleted_by: SET NULL
ALTER TABLE documents DROP CONSTRAINT IF EXISTS documents_deleted_by_fkey;
ALTER TABLE documents ADD CONSTRAINT documents_deleted_by_fkey
    FOREIGN KEY (deleted_by) REFERENCES users(id) ON DELETE SET NULL;
