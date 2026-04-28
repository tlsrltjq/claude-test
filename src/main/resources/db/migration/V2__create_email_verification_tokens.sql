-- ============================================================
-- V2 — 이메일 인증 토큰 테이블 + users.status 기본값 수정
-- ============================================================

ALTER TABLE users ALTER COLUMN status SET DEFAULT 'PENDING_EMAIL_VERIFICATION';

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    email       VARCHAR(255) NOT NULL,
    token       VARCHAR(6)   NOT NULL,
    expired_at  TIMESTAMP    NOT NULL,
    verified_at TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_email_verification_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_tokens_email   ON email_verification_tokens(email);
