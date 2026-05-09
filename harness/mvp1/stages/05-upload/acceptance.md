# Stage 05 — Acceptance

## 자동 검증 (verify.sh)
- [ ] `FileStorage` interface + `LocalFileStorage` 구현 존재
- [ ] `MyFolderController` (또는 동급) 가 `/my/folder` 매핑
- [ ] `application.yml`에 multipart 20MB / 40MB, allowed-extensions 설정 존재
- [ ] DocumentType enum에 RESUME, CAREER_DESCRIPTION, GRADUATION_CERTIFICATE, LICENSE, EMPLOYMENT_CERTIFICATE, ETC 모두 존재

## 수동 검증
- [ ] 일반 사용자 로그인 → `/my/folder` 접속 → 본인 폴더 정보 표시
- [ ] `/my/folder/documents/upload` 화면 표시
- [ ] PDF 파일 업로드 성공 → `./storage/uploads/yyyy/MM/<uuid>.pdf` 존재
- [ ] DB `documents` + `document_versions` row 생성 확인 (storage_path, original_file_name, stored_file_name, file_size, content_type, checksum)
- [ ] DOCX 업로드 + preview PDF 함께 업로드 → 두 파일 모두 저장, document_versions에 preview_* 채워짐
- [ ] 미허용 확장자(.exe 등) 업로드 시 차단
- [ ] 빈 파일 업로드 차단
- [ ] 25MB 파일 업로드 차단
- [ ] 같은 (document_type, title)로 다시 업로드 → version_no 1 → 2, current_version_id 갱신
- [ ] 다른 사용자의 `/my/folder`에는 접근 불가 (본인 폴더만 보임)
- [ ] 관리자 계정으로 `/admin/employees/{userId}/documents` 접속 → 메타데이터 목록 표시
- [ ] `audit_logs` 또는 애플리케이션 로그에 UPLOAD/UPDATE_DOCUMENT 기록

## NOT-DOING 확인
- [ ] 미리보기/다운로드 버튼은 비활성 또는 미노출
- [ ] 썸네일 컬럼/생성 로직 없음
- [ ] DOCX/HWP 자동 PDF 변환 없음
