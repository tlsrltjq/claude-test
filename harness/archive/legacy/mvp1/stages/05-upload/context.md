# Stage 05 — Context

## SSOT
PROJECT_SPEC §10~§12 (파일 저장/접근/저장소 확장성), §16 패키지, §25 절대 원칙 (특히 5,6,7번).

## 이전 단계 결과
- 사용자 승인 시 개인 폴더가 자동 생성되어 있음 (folders 1개/사람).
- ADMIN/EMPLOYEE 권한 분리 동작.
- audit_logs 테이블 존재 (4단계까지 가능하면 사용중).

## 이번 단계 핵심 제약
- DB에 BLOB 절대 금지. 메타데이터만.
- UUID 파일명, 연/월 폴더 분할.
- `FileStorage` 인터페이스 — 구현 1개 (`LocalFileStorage`). 추후 교체 가능 구조.
- 트랜잭션: 디스크 저장 → DB 메타 저장. 어느 한쪽 실패 시 다른 쪽 롤백 처리.
- preview는 선택 + PDF만. 자동 변환 안 함.
- 업로드 권한: 본인 폴더만. 본인이 아닌 폴더에 업로드 시도 시 거부.

## 코드가 들어갈 위치
- `common/file/` — FileStorage, LocalFileStorage, StoredFile, FileUploadCommand
- `document/service/DocumentUploadService` 등
- `document/controller/MyFolderController` — `/my/folder/**`
- `document/controller/AdminDocumentController` — `/admin/employees/{userId}/documents`
- `templates/my/folder.html`, `upload.html` 등
