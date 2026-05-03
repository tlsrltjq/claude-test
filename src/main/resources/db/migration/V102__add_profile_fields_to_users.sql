-- birth_date, phone 컬럼 추가
ALTER TABLE users ADD COLUMN IF NOT EXISTS birth_date DATE NOT NULL DEFAULT '1970-01-01';
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20) NOT NULL DEFAULT '';

-- position 컬럼을 enum 문자열로 정규화 (매핑 안 되는 값은 STAFF로)
UPDATE users
SET position = 'STAFF'
WHERE position IS NULL
   OR position NOT IN (
       'REPRESENTATIVE','EXECUTIVE_DIRECTOR','DIRECTOR',
       'GENERAL_MANAGER','DEPUTY_GENERAL_MANAGER','MANAGER',
       'ASSISTANT_MANAGER','STAFF'
   );

-- NULL 불허 + 길이 정리
ALTER TABLE users ALTER COLUMN position SET DEFAULT 'STAFF';
ALTER TABLE users ALTER COLUMN position SET NOT NULL;
ALTER TABLE users ALTER COLUMN position TYPE VARCHAR(50);

-- 검색 대비 인덱스
CREATE INDEX IF NOT EXISTS idx_users_name     ON users(name);
CREATE INDEX IF NOT EXISTS idx_users_position ON users(position);
