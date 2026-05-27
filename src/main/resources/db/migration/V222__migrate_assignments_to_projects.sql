-- 기존 배정 데이터에서 유니크 프로젝트(이름+고객사+기간 기준)를 projects 테이블에 삽입
INSERT INTO projects (name, client_name, start_date, end_date, status, memo, created_at, updated_at)
SELECT DISTINCT ON (project_name, COALESCE(client_name, ''), start_date, end_date)
       project_name, client_name, start_date, end_date, status, memo, now(), now()
FROM project_assignments
WHERE project_id IS NULL
ORDER BY project_name, COALESCE(client_name, ''), start_date, end_date, created_at;

-- 각 배정에 해당 project_id 연결
UPDATE project_assignments pa
SET project_id = (
    SELECT p.id FROM projects p
    WHERE p.name = pa.project_name
      AND COALESCE(p.client_name, '') = COALESCE(pa.client_name, '')
      AND p.start_date = pa.start_date
      AND p.end_date   = pa.end_date
    LIMIT 1
)
WHERE pa.project_id IS NULL;
