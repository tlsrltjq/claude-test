CREATE TABLE column_view_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(80) NOT NULL,
    columns_json TEXT NOT NULL,
    sort_json TEXT,
    career_display VARCHAR(8) NOT NULL DEFAULT 'ymd',
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT cvp_user_name_unique UNIQUE(user_id, name)
);
CREATE INDEX idx_cvp_user ON column_view_preferences(user_id);
