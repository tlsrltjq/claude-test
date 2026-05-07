# Stage 07 — Acceptance

## 자동 검증 (verify.sh)
- [ ] `FolderAccessService` 존재
- [ ] `/admin/users/{userId}/role` 매핑
- [ ] `/admin/users/{userId}/permissions` 매핑
- [ ] `/team/members` 매핑, `/team/members/{userId}/documents` 매핑
- [ ] `/shared/folders` 매핑, `/shared/folders/{folderId}/documents` 매핑
- [ ] AuditActionType: CHANGE_ROLE, GRANT_PERMISSION, REVOKE_PERMISSION 존재

## 수동 검증
- [ ] 관리자가 사용자 A를 TEAM_LEADER로 변경
- [ ] A 로그인 → `/team/members`에서 본인 팀 직원만 노출
- [ ] A가 본인 팀 직원 문서 미리보기/다운로드 가능
- [ ] A가 다른 팀 직원 문서 URL 직접 접근 → 403
- [ ] 관리자가 사용자 B에게 직원 C의 폴더 FOLDER_ACCESS 부여
- [ ] B 로그인 → `/shared/folders`에 C 폴더 표시
- [ ] B가 C 폴더의 문서 미리보기/다운로드 가능
- [ ] B가 D(권한 없는 사용자) 폴더 URL 접근 → 403
- [ ] TEAM_LEADER/B가 다른 사람 문서 업로드/수정/삭제 시도 → 거부 (UI 미노출 + 서버 차단)
- [ ] audit_logs: CHANGE_ROLE / GRANT_PERMISSION / REVOKE_PERMISSION / VIEW / DOWNLOAD 모두 기록
- [ ] 같은 (user, folder) 조합으로 두 번 부여 시도 → 거부

## NOT-DOING 확인
- [ ] 썸네일 컬럼/이미지 없음
- [ ] DOCX/HWP 자동 PDF 변환 없음
- [ ] 권한 만료일 컬럼 없음
