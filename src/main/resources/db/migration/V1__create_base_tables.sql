-- ============================================================
-- V1 — eActive Resource Hub 기본 테이블 생성
-- ============================================================

-- teams (users보다 먼저: users.team_id → teams.id)
CREATE TABLE IF NOT EXISTS teams (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL
);

-- users
CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL PRIMARY KEY,
    login_id       VARCHAR(100) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    team_id        BIGINT,
    position       VARCHAR(100),
    role           VARCHAR(50)  NOT NULL DEFAULT 'EMPLOYEE',
    status         VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    CONSTRAINT uk_users_login_id UNIQUE (login_id),
    CONSTRAINT uk_users_email    UNIQUE (email),
    CONSTRAINT fk_users_team_id  FOREIGN KEY (team_id) REFERENCES teams(id)
);

-- employee_profiles
CREATE TABLE IF NOT EXISTS employee_profiles (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    job_title        VARCHAR(100),
    career_summary   TEXT,
    skills           TEXT,
    available_status VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT fk_employee_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

-- folders
CREATE TABLE IF NOT EXISTS folders (
    id            BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT       NOT NULL,
    folder_name   VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    CONSTRAINT fk_folders_owner_user_id FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

-- documents (current_version_id FK는 document_versions 생성 후 추가)
CREATE TABLE IF NOT EXISTS documents (
    id                 BIGSERIAL PRIMARY KEY,
    folder_id          BIGINT       NOT NULL,
    document_type      VARCHAR(50)  NOT NULL,
    title              VARCHAR(255) NOT NULL,
    current_version_id BIGINT,
    status             VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL,
    CONSTRAINT fk_documents_folder_id FOREIGN KEY (folder_id) REFERENCES folders(id)
);

-- document_versions
CREATE TABLE IF NOT EXISTS document_versions (
    id                   BIGSERIAL PRIMARY KEY,
    document_id          BIGINT       NOT NULL,
    version_no           INTEGER      NOT NULL DEFAULT 1,
    original_file_name   VARCHAR(255) NOT NULL,
    stored_file_name     VARCHAR(255) NOT NULL,
    storage_path         VARCHAR(500) NOT NULL,
    preview_file_name    VARCHAR(255),
    preview_storage_path VARCHAR(500),
    file_size            BIGINT       NOT NULL,
    content_type         VARCHAR(100),
    checksum             VARCHAR(64),
    uploaded_by          BIGINT       NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    CONSTRAINT fk_document_versions_document_id FOREIGN KEY (document_id)  REFERENCES documents(id),
    CONSTRAINT fk_document_versions_uploaded_by FOREIGN KEY (uploaded_by)  REFERENCES users(id)
);

-- circular FK: documents.current_version_id → document_versions.id (deferred)
ALTER TABLE documents
    ADD CONSTRAINT fk_documents_current_version_id
        FOREIGN KEY (current_version_id) REFERENCES document_versions(id)
        DEFERRABLE INITIALLY DEFERRED;

-- permissions
CREATE TABLE IF NOT EXISTS permissions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    permission_type VARCHAR(50) NOT NULL,
    target_type     VARCHAR(50) NOT NULL,
    target_id       BIGINT      NOT NULL,
    granted_by      BIGINT      NOT NULL,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL,
    CONSTRAINT fk_permissions_user_id    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_permissions_granted_by FOREIGN KEY (granted_by) REFERENCES users(id)
);

-- audit_logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    action_type VARCHAR(50)  NOT NULL,
    target_type VARCHAR(50)  NOT NULL,
    target_id   BIGINT       NOT NULL,
    reason      VARCHAR(500),
    ip_address  VARCHAR(50),
    user_agent  VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_audit_logs_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================================
-- 인덱스
-- ============================================================
CREATE INDEX idx_users_email              ON users(email);
CREATE INDEX idx_users_team_id            ON users(team_id);
CREATE INDEX idx_documents_folder_id      ON documents(folder_id);
CREATE INDEX idx_doc_versions_document_id ON document_versions(document_id);
CREATE INDEX idx_audit_logs_user_created  ON audit_logs(user_id, created_at);
CREATE INDEX idx_audit_logs_target        ON audit_logs(target_type, target_id);
CREATE INDEX idx_permissions_user_target  ON permissions(user_id, target_type, target_id);
