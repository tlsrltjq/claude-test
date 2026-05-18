# MVP3 M3-07 — Acceptance

## 자동 검증
- [ ] V203 마이그레이션 존재 + folders.type 추가 + INSERT SHARED_PUBLIC
- [ ] FolderType enum 존재 (PERSONAL, SHARED_PUBLIC)
- [ ] Folder entity 에 type 필드
- [ ] /shared/folders/public 매핑
- [ ] templates/shared/public-folder.html 존재

## 수동 검증
- [ ] 부팅 후 folders 테이블에 SHARED_PUBLIC type 1행 자동 생성
- [ ] 모든 로그인 사용자 → /shared/folders/public 접근 가능
- [ ] 업로드 가능
- [ ] 본인 카드에 삭제 버튼, 타인 카드에 미노출
- [ ] ADMIN 은 모든 카드 삭제
- [ ] 다운로드는 모두
- [ ] 비로그인 → /login 리다이렉트

## NOT-DOING
- [ ] 통합 검색 X (M3-08)
- [ ] 카테고리 분류 X
