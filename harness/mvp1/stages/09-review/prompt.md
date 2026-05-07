# Stage 09 — Prompt

> PDF §22 9단계 프롬프트 원본

---

9단계 작업을 진행해줘.

이전 단계까지: 골격, DB, 인증, 관리자 승인, 팀/직원/폴더, 업로드, 미리보기/다운로드, 권한 분기, 썸네일+카드뷰.

이번 단계의 목표는 문서 승인/반려 프로세스를 구현하는 것이야.

중요:
- 직원이 업로드한 문서는 바로 팀장/개별 권한자에게 노출되면 안 돼.
- 업로드 직후 문서 버전 상태는 PENDING_REVIEW로 저장해.
- 관리자와 문서 소유자 본인은 검토 대기/승인/반려 문서를 모두 볼 수 있어.
- 팀장과 개별 권한 사용자는 APPROVED 문서만 볼 수 있어.
- 새 버전이 검토 대기 상태일 때 기존 승인 버전은 계속 유지되어야 해.
- documents.current_version_id는 최신 승인 버전을 가리키게 해.
- JWT는 사용하지 말고 기존 세션 기반 인증을 유지해.

요구사항:

1. `V4__add_review_columns_to_document_versions.sql` — review_status, reviewed_by, reviewed_at, reject_reason. review_status 기본 PENDING_REVIEW.

2. DocumentVersion Entity 필드 추가.

3. DocumentReviewStatus enum: PENDING_REVIEW, APPROVED, REJECTED, ARCHIVED.

4. 업로드 로직 수정: 새 버전 review_status=PENDING_REVIEW, current_version_id는 즉시 갱신하지 마. 첫 업로드라 승인된 버전이 없으면 current_version_id null 유지.

5. `/admin/documents/review` — ADMIN만, PENDING_REVIEW 목록(직원 이름/팀/종류/제목/원본 파일명/업로드일/상태/검토 버튼).

6. `/admin/documents/review/{documentVersionId}` — 검토 상세(직원/문서/버전/원본/업로드일/미리보기/승인/반려/반려 사유 입력). 6단계 미리보기 재사용.

7. 승인 — `POST /admin/documents/review/{documentVersionId}/approve`. PENDING_REVIEW만 승인. APPROVED+reviewed_by/at, current_version_id 갱신, APPROVE_DOCUMENT 로그.

8. 반려 — `POST /admin/documents/review/{documentVersionId}/reject`. 사유 필수. REJECTED+reviewed_by/at+reject_reason. current_version_id 변경 안 함. REJECT_DOCUMENT 로그.

9. 노출 정책:
   - 관리자: 모든 상태
   - 본인 `/my/folder`: 모든 상태
   - 팀장 화면: APPROVED만
   - 공유 폴더: APPROVED만
   - 팀장/개별 권한자가 PENDING/REJECTED URL 직접 접근 차단.

10. 카드 뷰 상태 배지: PENDING_REVIEW(검토 대기), APPROVED(승인됨), REJECTED(반려됨), ARCHIVED(보관됨).

11. 반려 사유 표시: 본인 상세 + 관리자 상세에서. 팀장/개별 권한자는 반려 문서 자체를 못 봄.

12. 미리보기/다운로드 권한 확장: ADMIN/본인=모든 상태, TEAM_LEADER/FOLDER_ACCESS=APPROVED만. 그 외 차단.

13. audit_logs: SUBMIT_REVIEW(업로드 시), APPROVE_DOCUMENT, REJECT_DOCUMENT. target_type=DOCUMENT_VERSION.

14. 관리자 대시보드 `/admin`에 PENDING_REVIEW 문서 수 추가.

15. README 9단계 검증 추가.

16. 아직 만들지 마: 운영 서버 배포, 백업 자동화, 워터마크, DOCX/HWP 자동 PDF 변환, 외부 공유 링크, 전자결재 연동.
