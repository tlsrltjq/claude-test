ALTER TABLE teams ADD COLUMN project_team BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE teams SET project_team = FALSE WHERE name IN ('영업본부', '경영본부');
