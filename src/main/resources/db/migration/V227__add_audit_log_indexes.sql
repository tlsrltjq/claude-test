-- 통계 쿼리 최적화: action_type + created_at 복합 인덱스
CREATE INDEX IF NOT EXISTS idx_audit_logs_action_created
    ON audit_logs (action_type, created_at);

-- TOP 문서 집계 최적화: action_type + target_id 복합 인덱스
CREATE INDEX IF NOT EXISTS idx_audit_logs_action_target
    ON audit_logs (action_type, target_id);
