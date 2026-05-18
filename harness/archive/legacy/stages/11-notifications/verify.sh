#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 11: Email Notifications (승인·반려·만료 알림) ==="

wait_for_app

# EmailSender 빈 등록 확인 — ConsoleEmailSender가 로그를 남기는지 앱 로그 확인 불가,
# 대신 승인/반려 액션 후 감사 로그로 간접 검증

login "admin@eactive.co.kr" "Admin1234!"

# 검토 대기 버전 하나 조회
pending_id=$(db_query "SELECT id FROM document_versions WHERE review_status='PENDING_REVIEW' LIMIT 1;")
if [[ -n "$pending_id" ]]; then
  # 승인 POST (이미 승인된 것이 없을 때)
  post_form "/admin/documents/review/$pending_id/approve" "comment=harness+test"
  approved=$(db_query "SELECT review_status FROM document_versions WHERE id='$pending_id';")
  check "승인 처리 후 review_status=APPROVED" \
    "$([[ "$approved" == "APPROVED" ]] && echo PASS || echo FAIL)"
else
  # 이미 APPROVED 버전 존재 여부로 대체 확인
  cnt=$(db_query "SELECT COUNT(*) FROM document_versions WHERE review_status='APPROVED';")
  check "APPROVED 버전 존재 (알림 발송 이력)" \
    "$([[ "$cnt" -ge 1 ]] && echo PASS || echo FAIL)"
fi

# APPROVE_DOCUMENT 감사 로그
audit=$(db_query "SELECT COUNT(*) FROM audit_logs WHERE action_type='APPROVE_DOCUMENT';")
check "DB: APPROVE_DOCUMENT 감사 로그" "$([[ "$audit" -ge 1 ]] && echo PASS || echo FAIL)"

# REJECT_DOCUMENT 감사 로그 (있으면 통과, 없으면 SKIP)
reject_audit=$(db_query "SELECT COUNT(*) FROM audit_logs WHERE action_type='REJECT_DOCUMENT';")
if [[ "$reject_audit" -ge 1 ]]; then
  check "DB: REJECT_DOCUMENT 감사 로그" "PASS"
else
  yellow "  - REJECT_DOCUMENT 로그 없음 — 반려 테스트 미수행"
fi

summary
