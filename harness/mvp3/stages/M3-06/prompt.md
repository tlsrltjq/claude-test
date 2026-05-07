# MVP3 M3-06 — Prompt

작업: /myfolder 에 본인 문서 삭제 기능.

요구사항:

1. 컨트롤러 — `DELETE /my/folder/documents/{documentId}` (또는 POST `/my/folder/documents/{documentId}/delete`).
2. 권한: 로그인 사용자 + 해당 문서의 folder.owner_user_id == currentUser.id 인 경우만.
3. 처리: mvp2 02 의 DocumentDeleteService 를 재사용 또는 동급 — 디스크 파일 + 썸네일 + preview + DB 메타 + audit_logs.
4. audit_logs.action_type = `DELETE_DOCUMENT`, reason = "self" 정도 또는 별도 액션 `DELETE_DOCUMENT_SELF` 추가 (둘 다 OK, 일관성 유지).
5. 화면: /myfolder 카드/상세에 [삭제] 버튼. 클릭 시 confirm.
6. ADMIN 도 동일 라우트 가능하면 좋지만, 기존 `/admin/documents/{id}` 삭제(mvp2 02)는 그대로 유지 — 두 라우트가 동일 서비스를 호출.

NOT-DOING:
- 휴지통/복구 (영구 제외)
- 다른 사용자의 문서 삭제 (ADMIN 전용 흐름은 mvp2 02 그대로)

검증:
- 본인 문서 [삭제] → 디스크 + DB 사라짐
- 다른 사용자 문서에 대해 본인 라우트 호출 → 403
- audit_logs 기록
