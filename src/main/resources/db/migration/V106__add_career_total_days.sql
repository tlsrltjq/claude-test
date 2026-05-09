ALTER TABLE employee_profiles
    ADD COLUMN IF NOT EXISTS career_total_days INTEGER NOT NULL DEFAULT 0;
