#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 13: Tags & Global Search ==="

wait_for_app

# DB: tags 테이블 존재
tbl=$(db_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='tags';")
check "DB: tags 테이블 존재" "$([[ "$tbl" -ge 1 ]] && echo PASS || echo FAIL)"

# DB: document_tags 조인 테이블
dt=$(db_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='document_tags';")
check "DB: document_tags 테이블 존재" "$([[ "$dt" -ge 1 ]] && echo PASS || echo FAIL)"

login "test@eactive.co.kr" "Test1234!"

# /search 페이지
check "GET /search → 200" "$([[ "$(get_status /search)" == "200" ]] && echo PASS || echo FAIL)"

body=$(get_body /search)
check "검색 페이지에 키워드 입력 필드 포함" \
  "$([[ "$body" == *"name=\"q\""* || "$body" == *"검색"* ]] && echo PASS || echo FAIL)"
check "검색 페이지에 타입 필터 포함" \
  "$([[ "$body" == *"filterType"* || "$body" == *"type"* ]] && echo PASS || echo FAIL)"
check "검색 페이지에 태그 필터 포함" \
  "$([[ "$body" == *"tag"* || "$body" == *"태그"* ]] && echo PASS || echo FAIL)"

# 키워드 검색 (빈 결과도 200)
check "GET /search?q=test → 200" \
  "$([[ "$(get_status "/search?q=test")" == "200" ]] && echo PASS || echo FAIL)"

# 문서 상세에서 태그 관리 UI
doc_id=$(db_query "SELECT d.id FROM documents d JOIN folders f ON f.id=d.folder_id JOIN users u ON u.id=f.owner_id WHERE u.email='test@eactive.co.kr' LIMIT 1;")
if [[ -n "$doc_id" ]]; then
  body=$(get_body /my/folder/documents/$doc_id)
  check "문서 상세에 태그 섹션 포함" \
    "$([[ "$body" == *"태그"* || "$body" == *"tag"* ]] && echo PASS || echo FAIL)"

  # 태그 추가 POST
  status=$(post_form_status "/my/folder/documents/$doc_id/tags" "tagName=harness")
  check "POST /documents/$doc_id/tags → 3xx" \
    "$([[ "$status" =~ ^3 ]] && echo PASS || echo FAIL)"

  # 태그가 DB에 저장됐는지
  tag_cnt=$(db_query "SELECT COUNT(*) FROM tags WHERE name='harness';")
  check "DB: 태그 'harness' 생성됨" "$([[ "$tag_cnt" -ge 1 ]] && echo PASS || echo FAIL)"

  # 태그로 검색
  check "GET /search?tag=harness → 200" \
    "$([[ "$(get_status "/search?tag=harness")" == "200" ]] && echo PASS || echo FAIL)"
fi

summary
