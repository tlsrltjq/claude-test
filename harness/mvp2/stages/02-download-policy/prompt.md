# MVP2 Stage 02 — Prompt

MVP2 2단계 작업을 진행해줘.

이전 단계에서 권한이 ADMIN/SALES/EMPLOYEE 로 단순화됐어.

이번 단계 목표는 **다운로드 사유 입력 화면을 제거**하고 ADMIN/SALES는 바로 다운로드, **관리자 파일 삭제 기능**을 추가하는 것이야.

중요:
- 다운로드 로그(audit_logs DOWNLOAD)는 그대로 저장. reason은 nullable.
- 정적 리소스 노출 금지(MVP1 6단계 그대로). 다운로드는 컨트롤러 경유.
- 디스크 파일과 DB 메타를 함께 삭제. 트랜잭션 정합성.
- MVP1 acceptance 회귀 없음.

요구사항:

1. Flyway 마이그레이션 (필요할 경우): `V101__make_audit_reason_nullable.sql`
   - `audit_logs.reason`이 NOT NULL이라면 NULL 허용으로 변경. 이미 nullable이면 빈 마이그레이션이라도 추가하지 말고 README에 메모.

2. `/documents/{documentVersionId}/download/reason` 엔드포인트 제거.
   - GET 메서드도 제거하고 사유 입력 템플릿(예: `templates/document-download-reason.html`)도 제거.

3. `/documents/{documentVersionId}/download` 변경.
   - 메서드: `GET` 만 (POST 제거).
   - 권한 검사: ADMIN → 모두, SALES → 모두 (read-only), EMPLOYEE → 본인 문서만 (`folder.ownerUserId == currentUser.id`).
   - 권한 없으면 403.
   - 권한 통과 시 audit_logs에 DOWNLOAD 기록 (reason=null), Content-Disposition: attachment + 원본 파일명.

4. 화면 갱신.
   - 모든 "다운로드 사유 입력" 단계가 있던 곳에서 다이렉트 다운로드 버튼/링크로 변경.
   - 문서 상세, /admin/employees/{id}/documents, 카드 뷰 등 영향 받는 곳 전부.

5. 관리자 파일 삭제 추가.
   - URL: `DELETE /admin/documents/{documentId}` 또는 `POST /admin/documents/{documentId}/delete`.
   - ADMIN만 접근 가능.
   - 처리: 모든 DocumentVersion의 디스크 파일 + 썸네일 + preview PDF 삭제 → DB의 document_versions 삭제 → documents 행 삭제. 트랜잭션 단위로.
   - audit_logs.action_type=DELETE_DOCUMENT 기록.
   - 관리자 문서 목록(예: /admin/employees/{id}/documents)에 삭제 버튼.
   - 삭제 확인 모달 또는 confirm() 1회 필요.

6. AuditActionType 에 `DELETE_DOCUMENT` 추가 (없으면).

7. SALES 다운로드도 audit에 기록. user_id, target document/version, file name, IP, UA, time.

8. README.md에 다운로드 정책 변경 섹션 추가.

9. NOT-DOING:
   - 회원가입 필드 추가 (03)
   - /sales/profiles 표 (04)
   - 양식 이력서 (06)
   - 경력 계산기 (07)
   - 휴지통/복구 (영구 제외 — 그냥 즉시 hard delete)

검증:
- ADMIN/SALES 로그인 → 문서 다운로드 버튼 클릭 → 사유 화면 없이 바로 받기
- EMPLOYEE 로그인 → 본인 문서 OK, 타인 문서 URL 직접 접근 → 403
- 관리자 삭제 → 디스크 + DB 모두 정리, audit_logs DELETE_DOCUMENT 행 생성
- 다운로드 로그 reason = NULL/빈 값
