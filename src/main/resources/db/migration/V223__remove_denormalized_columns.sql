ALTER TABLE project_assignments
    ALTER COLUMN project_id SET NOT NULL,
    DROP COLUMN project_name,
    DROP COLUMN client_name,
    DROP COLUMN memo;
