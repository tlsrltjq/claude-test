CREATE TABLE allowed_emails (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    note       VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    created_by BIGINT       REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_allowed_emails_email ON allowed_emails(email);
