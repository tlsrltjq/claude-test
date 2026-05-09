-- MVP2 Stage 14: 소프트 삭제 지원
-- status=DELETED 상태 유지 + 삭제 시각·삭제자 기록
ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS deleted_at  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by  BIGINT REFERENCES users(id);
