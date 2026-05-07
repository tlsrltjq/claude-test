-- backfill career_total_days from career_months (1개월 = 30일)
UPDATE employee_profiles
SET career_total_days = career_months * 30
WHERE career_total_days = 0 AND career_months > 0;
