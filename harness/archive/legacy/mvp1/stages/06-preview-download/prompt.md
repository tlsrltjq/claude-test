# Stage 06 — Prompt

> PDF §22 6단계 프롬프트 원본

---

6단계 작업을 진행해줘.

이전 단계까지: 골격, DB, 인증, 관리자 승인 처리, 팀/직원/폴더 자동 생성, 본인 폴더 파일 업로드 + 버전 관리.

이번 단계의 목표는 업로드된 문서를 웹에서 미리보기로 확인하고, 권한이 있는 사용자가 다운로드할 수 있게 하며, 열람/다운로드 로그를 저장하는 것이야.

중요:
- 파일 폴더를 정적 리소스로 직접 공개하지 마.
- 모든 미리보기와 다운로드는 Controller를 통해 처리해.
- 미리보기와 다운로드 전에 반드시 로그인과 권한을 확인해.
- 파일 경로가 URL에 직접 노출되지 않게 해.
- 다운로드 시 사유를 입력받고 audit_logs에 저장해.
- 팀장 권한과 개별 폴더 접근 권한은 아직 구현하지 마.

요구사항:

1. `document.service.DocumentAccessService` — 현재 단계 권한 규칙: ADMIN 모든 문서 / EMPLOYEE는 본인 문서만 / 그 외 차단(403).

2. 문서 상세 화면.
   - 본인: `/my/folder/documents/{documentId}`
   - 관리자: `/admin/employees/{userId}/documents/{documentId}`
   - 표시: 제목, 종류, 현재 버전, 원본 파일명, 업로드일, 미리보기 영역, 다운로드 버튼, 버전 목록.
   - 관리자 화면에는 직원 이름/팀 추가.

3. PDF 미리보기 `GET /documents/{documentVersionId}/preview` — 권한 확인 후 application/pdf로 스트리밍, iframe으로 표시.

4. 이미지 미리보기 — jpg/jpeg/png은 적절한 content type으로 스트리밍, img 태그로 표시.

5. DOCX/HWP/HWPX 미리보기 정책 — 원본 직접 미리보기 금지. preview_storage_path 있으면 preview PDF 반환. 없으면 미리보기 불가 안내.

6. 미리보기 로그(VIEW) audit_logs 저장. target_type=DOCUMENT_VERSION, reason=null 또는 "PREVIEW", IP/UA 저장.

7. 다운로드 사유 입력 화면 `GET /documents/{documentVersionId}/download/reason` — reason 최소 2자.

8. 다운로드 `POST /documents/{documentVersionId}/download` — 권한 확인, 원본 파일 스트리밍, content-disposition attachment with original_file_name.

9. 다운로드 로그(DOWNLOAD) audit_logs 저장. user_id, documentVersionId, reason, IP, UA 저장.

10. `audit/service/AuditLogService` — VIEW, DOWNLOAD 메서드. 5단계 UPLOAD 로그와 중복되지 않게 정리.

11. 문서 목록에 상세 보기 버튼 추가 (`/my/folder`, `/admin/employees/{userId}/documents`).

12. 오류 처리: 문서 없음 / 버전 없음 / 파일 디스크에 없음 / 미리보기 미지원 / 권한 없음 — 각각 메시지.

13. README.md에 6단계 검증 방법 추가.

14. 아직 만들지 마: 팀장 권한, 개별 폴더 접근, 썸네일, DOCX/HWP 자동 PDF 변환, 워터마크, 외부 공유 링크.
