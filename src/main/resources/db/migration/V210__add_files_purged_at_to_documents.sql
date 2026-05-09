-- GC: 물리 파일 삭제 완료 시각 기록 (NULL = 미정리)
ALTER TABLE documents ADD COLUMN IF NOT EXISTS files_purged_at TIMESTAMP;

COMMENT ON COLUMN documents.files_purged_at IS
  '파일 GC 완료 시각. NULL이면 아직 물리 파일이 삭제되지 않음.';

CREATE INDEX IF NOT EXISTS idx_documents_gc_candidates
    ON documents (status, deleted_at, files_purged_at)
    WHERE status = 'DELETED' AND files_purged_at IS NULL;
