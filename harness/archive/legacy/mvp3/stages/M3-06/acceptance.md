# MVP3 M3-06 — Acceptance

## 자동 검증
- [ ] /my/folder/documents/{...} delete 라우트 존재
- [ ] templates/my/folder.html 또는 document-detail.html 에 삭제 버튼

## 수동 검증
- [ ] 본인 문서 삭제 → 디스크 + DB 사라짐
- [ ] 타인 문서 URL 직접 호출 → 403
- [ ] audit_logs DELETE_DOCUMENT (또는 SELF) 행
- [ ] mvp2 02 ADMIN 삭제 그대로 동작
