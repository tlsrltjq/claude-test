CREATE TABLE project_assignments (
    id              BIGSERIAL    PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id),
    project_name    VARCHAR(200) NOT NULL,
    client_name     VARCHAR(200),
    role            VARCHAR(100),
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    allocation_rate SMALLINT     NOT NULL DEFAULT 100,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PLANNED',
    memo            TEXT,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,

    CONSTRAINT chk_pa_dates  CHECK (end_date >= start_date),
    CONSTRAINT chk_pa_alloc  CHECK (allocation_rate BETWEEN 0 AND 100),
    CONSTRAINT chk_pa_status CHECK (status IN ('PLANNED','ACTIVE','ENDED','CANCELLED'))
);

CREATE INDEX idx_pa_user_id    ON project_assignments(user_id);
CREATE INDEX idx_pa_status     ON project_assignments(status);
CREATE INDEX idx_pa_dates      ON project_assignments(start_date, end_date);
CREATE INDEX idx_pa_user_dates ON project_assignments(user_id, start_date, end_date);
