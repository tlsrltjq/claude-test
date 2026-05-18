# Stage 08 — Prompt

> PDF §22 8단계 프롬프트 원본

---

8단계 작업을 진행해줘.

이전 단계까지: 골격, DB, 인증, 관리자 승인, 팀/직원/폴더, 업로드+버전, 미리보기/다운로드+로그, 팀장/개별 폴더 권한.

이번 단계의 목표는 문서 목록 화면을 카드 뷰로 개선하고, PDF/이미지/preview PDF 기반 썸네일을 생성해 폴더 안의 문서를 한눈에 볼 수 있게 만드는 것이야.

중요:
- 파일 폴더를 정적 리소스로 직접 공개하지 마.
- 썸네일도 반드시 Controller를 통해 제공해.
- 썸네일 요청 시에도 문서 접근 권한을 확인해.
- 권한 없는 사용자는 썸네일도 볼 수 없어야 해.
- DOCX/HWP/HWPX 자동 PDF 변환은 아직 구현하지 마.
- preview PDF가 있을 때만 DOCX/HWP/HWPX 문서의 썸네일을 생성해.
- 팀장과 개별 권한 사용자는 다른 사람 문서를 업로드/수정/삭제/썸네일 재생성할 수 없어야 해.

요구사항:

1. `V3__add_thumbnail_columns_to_document_versions.sql` — `thumbnail_file_name`, `thumbnail_storage_path`, `thumbnail_content_type`, `thumbnail_generated_at`.

2. DocumentVersion Entity 필드 추가.

3. `document.service.ThumbnailService` — 생성/저장/경로 갱신/재생성/조회. 가능하면 PdfThumbnailGenerator / ImageThumbnailGenerator / PreviewPdfThumbnailGenerator / DefaultThumbnailProvider 분리. 시작은 단일 Service로도 OK.

4. PDF 썸네일: 첫 페이지 → PNG. 적절한 크기. 저장 파일명 UUID. 경로 저장.

5. 이미지 썸네일: jpg/jpeg/png 리사이즈해서 PNG/JPG로. 원본 노출 금지.

6. DOCX/HWP/HWPX: 원본 변환 안 함. preview PDF가 있으면 첫 페이지로 썸네일. 없으면 기본 아이콘.

7. 업로드 후 썸네일 생성 — 실패가 업로드 실패로 이어지지 않게. 실패 시 로그만, 화면은 기본 아이콘.

8. `GET /documents/{documentVersionId}/thumbnail` — 권한 확인 + 스트리밍. 없으면 기본 아이콘 또는 종류별 아이콘.

9. `POST /documents/{documentVersionId}/thumbnail/regenerate` — ADMIN/본인만. TEAM_LEADER/개별 권한자 차단. REGENERATE_THUMBNAIL 로그.

10. 카드 뷰 적용: `/my/folder`, `/admin/employees/{userId}/documents`, `/team/members/{userId}/documents`, `/shared/folders/{folderId}/documents`. 카드 항목: 썸네일/종류/제목/현재 버전/원본 파일명/업로드일/보기/다운로드. 본인+ADMIN에만 업로드/수정/썸네일 재생성.

11. 문서 종류 필터(전체/이력서/경력기술서/졸업증명서/자격증/재직증명서/기타).

12. 검색(제목 또는 원본 파일명).

13. 정렬(최근 업로드/종류/제목). 기본 최근 업로드.

14. 관리자 직원 폴더 화면 상단 보강(이름/팀/직급/문서 개수/최근 업데이트일).

15. 내 폴더 화면 상단 보강(이름/팀/개인 폴더명/문서 개수/업로드 버튼).

16. 오류 처리: 썸네일 생성 실패/없음/버전 없음/권한 없음/지원 안 함 → 메시지/기본 아이콘.

17. README.md 8단계 검증 추가.

18. 아직 만들지 마: DOCX/HWP 자동 PDF 변환, 워터마크, 외부 공유 링크, 문서 승인/반려, 권한 만료일, 모바일 앱.
