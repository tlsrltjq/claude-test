#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 12: Document Expiry Date Management ==="

wait_for_app

# DB: expires_at 컬럼 존재
col=$(db_query "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='documents' AND column_name='expires_at';")
check "DB: documents.expires_at 컬럼" "$([[ "$col" -ge 1 ]] && echo PASS || echo FAIL)"

login "test@eactive.co.kr" "Test1234!"

# 업로드 폼에 expiresAt 필드 포함
body=$(get_body /my/folder/documents/upload)
check "업로드 폼에 expiresAt 입력 필드 포함" \
  "$([[ "$body" == *"expiresAt"* || "$body" == *"expires_at"* || "$body" == *"만료"* ]] && echo PASS || echo FAIL)"

# 문서 상세에 만료일 섹션
doc_id=$(db_query "SELECT d.id FROM documents d JOIN folders f ON f.id=d.folder_id JOIN users u ON u.id=f.owner_id WHERE u.email='test@eactive.co.kr' LIMIT 1;")
if [[ -n "$doc_id" ]]; then
  body=$(get_body /my/folder/documents/$doc_id)
  check "문서 상세에 만료일 섹션 포함" \
    "$([[ "$body" == *"만료"* || "$body" == *"expires"* ]] && echo PASS || echo FAIL)"
fi

# 관리자 만료 문서 목록
login "admin@eactive.co.kr" "Admin1234!"
check "GET /admin/documents/expiry → 200" \
  "$([[ "$(get_status /admin/documents/expiry)" == "200" ]] && echo PASS || echo FAIL)"

body=$(get_body /admin/documents/expiry)
check "만료 관리 페이지 로드 성공" \
  "$([[ "$body" == *"만료"* || "$body" == *"expir"* ]] && echo PASS || echo FAIL)"

# 만료일 수동 설정 엔드포인트 (직원 소유 문서)
if [[ -n "$doc_id" ]]; then
  login "test@eactive.co.kr" "Test1234!"
  status=$(post_form_status "/my/folder/documents/$doc_id/expiry" "expiresAt=2099-12-31")
  check "POST /my/folder/documents/$doc_id/expiry → 3xx" \
    "$([[ "$status" =~ ^3 ]] && echo PASS || echo FAIL)"
fi

summary
