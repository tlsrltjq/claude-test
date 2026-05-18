#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 09: Document Review (승인·반려) ==="

wait_for_app

# DB: review 컬럼 존재
col=$(db_query "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='document_versions' AND column_name='review_status';")
check "DB: review_status 컬럼" "$([[ "$col" -ge 1 ]] && echo PASS || echo FAIL)"

# DB: APPROVED 버전 존재
approved=$(db_query "SELECT COUNT(*) FROM document_versions WHERE review_status='APPROVED';")
check "DB: APPROVED 버전 존재" "$([[ "$approved" -ge 1 ]] && echo PASS || echo FAIL)"

login "admin@eactive.co.kr" "Admin1234!"
check "GET /admin/documents/review → 200" \
  "$([[ "$(get_status /admin/documents/review)" == "200" ]] && echo PASS || echo FAIL)"

# 대시보드에 pendingReviewCount 카드
body=$(get_body /admin)
check "관리자 대시보드 '문서 검토 대기' 카드" \
  "$([[ "$body" == *"문서 검토 대기"* ]] && echo PASS || echo FAIL)"

# DB: APPROVE_DOCUMENT 감사 로그
audit=$(db_query "SELECT COUNT(*) FROM audit_logs WHERE action_type='APPROVE_DOCUMENT';")
check "DB: APPROVE_DOCUMENT 감사 로그" "$([[ "$audit" -ge 1 ]] && echo PASS || echo FAIL)"

# 내 폴더에 리뷰 배지 표시
login "test@eactive.co.kr" "Test1234!"
body=$(get_body /my/folder)
check "내 폴더에 검토 상태 배지 포함" \
  "$([[ "$body" == *"승인됨"* || "$body" == *"검토 대기"* || "$body" == *"반려됨"* ]] && echo PASS || echo FAIL)"

summary
