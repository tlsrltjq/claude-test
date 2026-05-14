# Stage 05 — Prompt

> PDF §22 5단계 프롬프트 원본

---

5단계 작업을 진행해줘.

현재 프로젝트는 Java 21, Spring Boot 3.5.x, Gradle 기반의 eActive Resource Hub야.
패키지명은 `com.eactive.resourcehub`야.

이전 단계까지: 골격, DB, 회원가입/인증/로그인, 관리자 승인 처리, 팀 관리, 직원 목록, 개인 폴더 자동 생성.

이번 단계의 목표는 사용자가 본인 개인 폴더에 문서를 업로드하고, 실제 파일은 로컬 디스크에 저장하며, DB에는 문서 정보와 파일 메타데이터를 저장하는 것이야.

중요:
- 실제 파일은 DB에 저장하지 마.
- DB에는 파일 메타데이터만 저장해.
- 파일은 로컬 디스크에 저장해.
- 파일명은 원본 파일명을 그대로 쓰지 말고 UUID 기반으로 저장해.
- 나중에 NAS, MinIO, S3로 바꿀 수 있도록 `FileStorage` 인터페이스를 먼저 만들어줘.

요구사항:

1. FileStorage 구조.
   - `common.file.FileStorage` 인터페이스 (`store`, `load`, `delete`).
   - `common.file.LocalFileStorage` 구현체.
   - 업로드 경로는 `application.yml`의 `resourcehub.upload.base-dir`. 기본값 `./storage/uploads`. 운영은 `RESOURCEHUB_UPLOAD_BASE_DIR`.

2. 파일 업로드 설정.
   - 최대 파일 크기 20MB, multipart max-file-size 20MB, max-request-size 40MB.
   - 허용 확장자: pdf, jpg, jpeg, png, docx, hwp, hwpx (application.yml에서 관리).

3. 파일 저장 규칙.
   - 원본 파일명은 DB에.
   - 실제 저장 파일명은 UUID 기반.
   - 원본 확장자 유지.
   - 저장 경로 연/월 분할: 2026/04/{uuid}.pdf
   - 파일 크기, content type, checksum(SHA-256) 저장.

4. 내 개인 폴더 화면 `/my/folder`. 본인 폴더만 표시. 본인 문서 목록 표시. 다른 사용자 폴더는 볼 수 없음.

5. 문서 업로드 화면 `/my/folder/documents/upload`.
   - 입력값: 문서 종류, 문서 제목, 원본 파일, 미리보기 PDF(선택).
   - 문서 종류 enum: RESUME, CAREER_DESCRIPTION, GRADUATION_CERTIFICATE, LICENSE, EMPLOYMENT_CERTIFICATE, ETC.

6. 문서 업로드 처리 `POST /my/folder/documents`.
   - 본인 폴더에만 업로드.
   - document_type+title 기준 기존 문서 있으면 새 document_versions 생성, 없으면 documents 새로 만들고 v1 생성.
   - 새 버전 시 version_no +1, current_version_id 갱신.

7. 미리보기 PDF 업로드(선택). preview는 PDF만. preview_file_name/preview_storage_path 저장. **자동 변환은 구현하지 마.**

8. 업로드 검증.
   - 빈 파일 차단.
   - 미허용 확장자 차단.
   - 20MB 초과 차단.
   - 파일 저장 실패 시 DB 롤백, DB 저장 실패 시 파일 삭제.

9. 문서 목록 화면 `/my/folder` — 문서 종류, 제목, 원본 파일명, 현재 버전, 업로드일. 미리보기/다운로드 버튼은 비활성 표시(아직).

10. 관리자용 직원 문서 목록 `/admin/employees/{userId}/documents` (ADMIN만, 다운로드/미리보기 없이 메타만).

11. 감사 로그.
    - 업로드 성공 → UPLOAD.
    - 새 버전 → UPDATE_DOCUMENT.
    - target은 document 또는 document_version.
    - IP, User-Agent 가능하면 저장.

12. README.md에 5단계 검증 방법 추가.

13. 아직 만들지 마: PDF 웹 미리보기, 이미지 웹 미리보기, 문서 다운로드, 다운로드 사유, 다운로드 로그, 팀장 권한 부여, 개별 폴더 접근 권한, 문서 썸네일 자동 생성, DOCX/HWP 자동 PDF 변환.
