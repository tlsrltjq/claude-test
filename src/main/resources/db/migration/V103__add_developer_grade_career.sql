ALTER TABLE employee_profiles
    ADD COLUMN IF NOT EXISTS developer_grade VARCHAR(20),
    ADD COLUMN IF NOT EXISTS career_months   INT NOT NULL DEFAULT 0;
