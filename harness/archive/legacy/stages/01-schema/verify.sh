#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 01-02: Schema & Entities ==="

# Flyway 마이그레이션 적용 확인
tables=("users" "teams" "folders" "documents" "document_versions" "audit_logs" "permissions")
for t in "${tables[@]}"; do
  count=$(db_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='$t';")
  check "테이블 존재: $t" "$([[ "$count" -ge 1 ]] && echo PASS || echo FAIL)"
done

# V1~V6 마이그레이션 모두 적용됐는지
v6=$(db_query "SELECT COUNT(*) FROM flyway_schema_history WHERE version='6' AND success=true;")
check "V6 마이그레이션 완료" "$([[ "$v6" -ge 1 ]] && echo PASS || echo FAIL)"

# 핵심 컬럼 존재
col=$(db_query "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='documents' AND column_name='expires_at';")
check "documents.expires_at 컬럼" "$([[ "$col" -ge 1 ]] && echo PASS || echo FAIL)"

col2=$(db_query "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='document_versions' AND column_name='review_status';")
check "document_versions.review_status 컬럼" "$([[ "$col2" -ge 1 ]] && echo PASS || echo FAIL)"

tags_table=$(db_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='tags';")
check "tags 테이블 존재" "$([[ "$tags_table" -ge 1 ]] && echo PASS || echo FAIL)"

summary
