CREATE TABLE projects (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    client_name VARCHAR(200),
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    memo        TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PLANNED',
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,

    CONSTRAINT chk_proj_status CHECK (status IN ('PLANNED','ACTIVE','ENDED','CANCELLED')),
    CONSTRAINT chk_proj_dates  CHECK (end_date >= start_date)
);

CREATE INDEX idx_proj_status ON projects(status);
CREATE INDEX idx_proj_dates  ON projects(start_date, end_date);
