# MVP3 M3-06 — Context

mvp2 02 의 DocumentDeleteService 를 재사용. 본인 권한 체크만 추가.

위치:
- `document/controller/MyFolderDeleteController.java` (또는 MyFolderController에 메서드 추가)
- 기존 `DocumentDeleteService` 활용
- `templates/my/folder.html`, `templates/my/document-detail.html` — 삭제 버튼 + confirm
