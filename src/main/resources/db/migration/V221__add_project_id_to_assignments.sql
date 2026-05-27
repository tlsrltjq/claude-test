ALTER TABLE project_assignments
    ADD COLUMN project_id BIGINT REFERENCES projects(id);

CREATE INDEX idx_pa_project_id ON project_assignments(project_id);
