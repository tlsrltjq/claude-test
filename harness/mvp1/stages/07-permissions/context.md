# Stage 07 — Context

## SSOT
PROJECT_SPEC §4 사용자, §5.10 관리자 권한 부여, §18 권한 정책.

## 이전 단계 결과
- 6단계까지의 권한 규칙: ADMIN/본인만.
- audit_logs에 UPLOAD/VIEW/DOWNLOAD/UPDATE_DOCUMENT.

## 이번 단계 핵심 제약
- TEAM_LEADER는 같은 팀의 직원 문서만 read-only로.
- FOLDER_ACCESS 권한자는 허용된 폴더만 read-only.
- 권한 검사는 Service(`DocumentAccessService`, `FolderAccessService`)로 일원화.
- 같은 사용자에게 같은 폴더 권한 중복 부여 차단 — DB unique 제약 추가하거나 서비스에서 검사.

## 코드가 들어갈 위치
- `permission/service/`, `permission/controller/`
- `team/controller/TeamLeaderController.java` — `/team/members/**`
- `permission/controller/SharedFolderController.java` — `/shared/folders/**`
- `templates/team/`, `templates/shared/`, `templates/admin/user-permissions.html`
