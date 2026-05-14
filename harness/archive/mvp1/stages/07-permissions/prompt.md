# Stage 07 — Prompt

> PDF §22 7단계 프롬프트 원본

---

7단계 작업을 진행해줘.

이전 단계까지: 골격, DB, 인증, 관리자 승인, 팀/직원/폴더, 업로드+버전, 미리보기/다운로드+VIEW/DOWNLOAD 로그.

이번 단계의 목표는 팀장 권한과 개별 폴더 접근 권한을 구현하는 것이야.

중요:
- JWT는 사용하지 말고 기존 세션 기반 인증을 유지해.
- 팀장은 본인 팀 직원 폴더와 문서만 볼 수 있어야 해.
- 개별 권한 사용자는 관리자가 허용한 특정 직원 폴더만 볼 수 있어야 해.
- 팀장과 개별 권한 사용자는 다른 사람의 문서를 업로드/수정/삭제할 수 없어야 해.
- 다른 사람 문서에 대해서는 미리보기와 다운로드만 가능하게 해.
- 권한 판단 로직을 Controller에 흩뿌리지 말고 Service로 분리해.

요구사항:

1. 사용자 역할 변경 — `/admin/users/{userId}/role` (ADMIN만). ADMIN/TEAM_LEADER/EMPLOYEE 변경 가능. CHANGE_ROLE 로그. ADMIN 부여 시 확인 메시지.

2. `/admin/users/{userId}/permissions` — 사용자 권한 관리 화면 (이름/이메일/팀/역할/현재 권한 목록/부여 폼/회수 버튼).

3. 개별 폴더 접근 권한 부여 — `permissions` 테이블 사용. permission_type=FOLDER_ACCESS, target_type=FOLDER, target_id=folders.id. 중복 부여 차단. GRANT_PERMISSION 로그.

4. 권한 회수 — 1차는 삭제 방식. REVOKE_PERMISSION 로그.

5. 팀장용 팀원 목록 `/team/members` (TEAM_LEADER만, 본인 팀 ACTIVE 사용자만 표시). 다른 팀 직원 절대 노출 금지.

6. 팀장용 직원 문서 목록 `/team/members/{userId}/documents` (대상 사용자가 같은 팀일 때만, 아니면 403).

7. 개별 권한자용 공유 폴더 목록 `/shared/folders` — 본인이 FOLDER_ACCESS 받은 폴더 목록(소유자 이름/팀, 폴더명).

8. 개별 권한자용 공유 폴더 문서 목록 `/shared/folders/{folderId}/documents` — 권한 보유 시에만.

9. `DocumentAccessService` 확장:
   - ADMIN → 모두
   - 본인 → 모두
   - TEAM_LEADER + 같은 팀 → 허용
   - permissions FOLDER_ACCESS → 허용
   - 그 외 → 차단

10. `FolderAccessService` 신설(또는 같은 클래스에 추가):
    - 접근 허용: ADMIN/본인/같은 팀 팀장/FOLDER_ACCESS
    - 수정 허용: ADMIN/본인만 (TEAM_LEADER/개별 권한자는 수정 불가)
    - 문서 업로드/수정 흐름에 반영.

11. 문서 상세/미리보기/다운로드에 권한 확장 반영. 다운로드 사유 + DOWNLOAD 로그, 미리보기 + VIEW 로그 유지.

12. 본인 아닌 문서엔 업로드/수정/삭제 버튼 미노출, 서버에서도 차단.

13. 감사 로그 정리: CHANGE_ROLE, GRANT_PERMISSION, REVOKE_PERMISSION + 기존 VIEW/DOWNLOAD/UPLOAD 유지. IP/UA.

14. README.md에 7단계 검증 방법 추가.

15. 아직 만들지 마: 썸네일, DOCX/HWP 자동 PDF 변환, 워터마크, 외부 공유 링크, 권한 만료일, 결재.
