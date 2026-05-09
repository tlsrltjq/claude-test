# MVP3 M3-05 — Context

DECISIONS D-04, D-05.

이전: mvp2 04 — DocumentType 7개. 태그 컬럼/테이블이 어딘가 존재 (mvp2 어느 단계에서 추가됐을 가능성 있음 — 코드에서 확인).

핵심 제약:
- 기존 EMPLOYMENT_CERTIFICATE 데이터 보존
- 태그 DB는 drop 안 함 (다음 라운드 cleanup)
- PROFILE_PHOTO 확장자 jpg/png만

위치:
- `V202__update_document_type_check.sql`
- `document/entity/DocumentType.java`
- `application.yml` allowed-extensions
- `document/service/DocumentUploadService.java` (PROFILE_PHOTO 확장자 검증)
- 모든 templates 태그 표시 제거
