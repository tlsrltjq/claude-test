# Stage 08 — Acceptance

## 자동 검증 (verify.sh)
- [ ] V3 마이그레이션 존재
- [ ] DocumentVersion에 thumbnail* 필드 존재
- [ ] `ThumbnailService` 존재
- [ ] `/documents/{documentVersionId}/thumbnail` 매핑
- [ ] `/documents/{documentVersionId}/thumbnail/regenerate` 매핑
- [ ] AuditActionType에 REGENERATE_THUMBNAIL 존재
- [ ] build.gradle에 PDFBox 또는 thumbnail 라이브러리 의존성 추가 흔적

## 수동 검증
- [ ] PDF 업로드 → DB thumbnail_storage_path 채워짐 + 디스크에 썸네일 파일 존재
- [ ] 이미지 업로드 → 썸네일 자동 생성
- [ ] DOCX + preview PDF 업로드 → preview 첫 페이지로 썸네일 생성
- [ ] preview 없는 DOCX → 기본 아이콘 표시 (오류 없이)
- [ ] `/documents/{id}/thumbnail` 권한 없으면 403/거부
- [ ] 카드 뷰 4개 화면(my, admin, team, shared) 정상
- [ ] 종류 필터/검색/정렬 동작
- [ ] 본인+ADMIN만 썸네일 재생성 버튼 노출 + 동작 (TEAM_LEADER/개별 권한자는 차단)
- [ ] audit_logs에 REGENERATE_THUMBNAIL 기록

## NOT-DOING 확인
- [ ] DOCX/HWP 자동 PDF 변환 코드 없음
- [ ] 워터마크 없음
- [ ] 외부 공유 링크 없음
