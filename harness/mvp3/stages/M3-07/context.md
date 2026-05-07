# MVP3 M3-07 — Context

DECISIONS D-10 (옵션 A: folders.type 추가).

이전: folders.owner_user_id 가 NOT NULL 이고 사람당 1행이 자동 생성. 공용 폴더는 owner 가 없는 케이스 → owner_user_id 를 nullable로 바꿔야 함.

핵심 제약:
- 기존 PERSONAL 폴더 데이터 영향 없음 (default 'PERSONAL')
- 공용 폴더 1행만 — 추가 시드 없음
- 업로드된 문서의 uploader 기준 삭제 권한
- DocumentVersion.uploaded_by 가 본인 비교 기준

위치:
- `V203__add_folders_type_and_public.sql`
- `document/entity/Folder.java`, `FolderType.java`
- `shared/controller/PublicFolderController.java`
- `templates/shared/public-folder.html`
- `DocumentAccessService` / `FolderAccessService` 갱신
