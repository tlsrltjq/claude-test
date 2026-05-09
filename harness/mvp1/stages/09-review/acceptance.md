# Stage 09 — Acceptance

## 자동 검증 (verify.sh)
- [ ] V4 마이그레이션 존재
- [ ] DocumentVersion에 reviewStatus/reviewedBy/reviewedAt/rejectReason 필드
- [ ] DocumentReviewStatus enum 4개 값
- [ ] `/admin/documents/review`, `/admin/documents/review/{id}`, `/approve`, `/reject` 매핑
- [ ] AuditActionType에 SUBMIT_REVIEW/APPROVE_DOCUMENT/REJECT_DOCUMENT 존재

## 수동 검증
- [ ] 직원이 새 문서 업로드 → review_status=PENDING_REVIEW, current_version_id 미변경
- [ ] `/admin/documents/review` 목록에 표시
- [ ] 관리자가 승인 → review_status=APPROVED, reviewed_by/at 채워짐, current_version_id 해당 버전으로 갱신
- [ ] 팀장 / 공유 폴더 권한자가 새로 승인된 버전을 볼 수 있음
- [ ] 새 버전 업로드(검토 대기) → 기존 승인 버전이 계속 노출 유지
- [ ] 관리자가 반려 → review_status=REJECTED, reject_reason 저장, current_version_id 변경 없음
- [ ] 반려 문서: 본인+ADMIN만 보임, 팀장/개별 권한자에는 미노출
- [ ] 팀장이 PENDING_REVIEW 또는 REJECTED 버전 URL 직접 접근 → 차단
- [ ] 본인 상세 화면에서 반려 사유 표시
- [ ] 카드 뷰에 상태 배지 표시 (PENDING_REVIEW/APPROVED/REJECTED 구분)
- [ ] `/admin` 대시보드에 PENDING_REVIEW 개수 표시
- [ ] audit_logs에 SUBMIT_REVIEW/APPROVE_DOCUMENT/REJECT_DOCUMENT 기록

## NOT-DOING 확인
- [ ] 운영 배포 산출물 없음 (Docker prod yml, .env.example 운영 보강 등은 10단계)
- [ ] 백업 스크립트 없음
- [ ] 워터마크/외부 공유 링크 없음
