CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id),
    email       VARCHAR(255) NOT NULL,
    token       VARCHAR(10)  NOT NULL,
    expired_at  TIMESTAMPTZ  NOT NULL,
    verified_at TIMESTAMPTZ,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_prt_user  ON password_reset_tokens(user_id);
CREATE INDEX idx_prt_email ON password_reset_tokens(email);
