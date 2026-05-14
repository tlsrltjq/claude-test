# Stage 08 — Context

## SSOT
PROJECT_SPEC §5.7 문서 썸네일, §11 파일 접근, §16 패키지.

## 이전 단계 결과
- 권한 분기 완성 (ADMIN/본인/팀장-같은팀/개별 권한자).
- DocumentVersion에 preview_storage_path가 채워질 수 있음.

## 이번 단계 핵심 제약
- PDF 첫 페이지 → 이미지: PDFBox(`org.apache.pdfbox`) 또는 동급 라이브러리. build.gradle에 의존성 추가.
- 이미지 리사이즈: 표준 자바 `java.awt.image` 또는 `Thumbnailator`. 둘 다 가능.
- 썸네일 자체도 `LocalFileStorage` 경로 정책 따름 (UUID + 연/월 분할 OR 별도 thumbnail/ 디렉토리).
- 썸네일 컨트롤러 → 권한 확인 → InputStreamResource 반환.
- 재생성 권한 분기는 7단계 ACL과 일치.

## 코드가 들어갈 위치
- `document/service/ThumbnailService.java`
- `document/service/thumbnail/{Pdf,Image,PreviewPdf,Default}ThumbnailGenerator.java` (선택)
- `document/controller/ThumbnailController.java`
- 카드 뷰 템플릿 다수 갱신.
