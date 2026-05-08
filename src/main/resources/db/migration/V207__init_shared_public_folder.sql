-- MVP3 M3-07: SHARED_PUBLIC 공용 폴더 초기화
-- AdminInitializer 에서 ensurePublicFolder() 호출로 생성되지만,
-- 마이그레이션 레벨에서도 없으면 생성해 Flyway 실행 순서와 일치시킴.
INSERT INTO folders (owner_user_id, folder_name, type, created_at, updated_at)
SELECT u.id, '전사 공용 폴더', 'SHARED_PUBLIC', NOW(), NOW()
FROM users u
WHERE u.role = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM folders f WHERE f.type = 'SHARED_PUBLIC'
  )
LIMIT 1;
