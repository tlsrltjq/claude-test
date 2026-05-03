CREATE TABLE resume_templates (
    id             BIGSERIAL PRIMARY KEY,
    file_name      VARCHAR(255)  NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    storage_path   VARCHAR(500)  NOT NULL,
    content_type   VARCHAR(100)  NOT NULL,
    file_size      BIGINT        NOT NULL,
    checksum       VARCHAR(128),
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    uploaded_by    BIGINT        NOT NULL REFERENCES users(id),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_resume_templates_status ON resume_templates(status);
