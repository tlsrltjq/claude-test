# MVP3 M3-05 — Prompt

작업: DocumentType 정비, 태그 제거, ppt/pptx 허용.

요구사항:

## 1. DocumentType 변경

1. `DocumentType` enum:
   - `LICENSE` displayName "정보처리기사"로 변경 (Korean displayName 메서드/필드)
   - `EMPLOYMENT_CERTIFICATE` `@Deprecated` + 업로드 select 옵션에서 제외
   - `PROFILE_PHOTO("증명사진")` 신규 추가

2. Flyway `V202__update_document_type_check.sql`
   - DB 체크 제약 갱신: PROFILE_PHOTO 허용
   - 예: `ALTER TABLE documents DROP CONSTRAINT IF EXISTS documents_document_type_check; ALTER TABLE documents ADD CONSTRAINT documents_document_type_check CHECK (document_type IN ('RESUME','CAREER_DESCRIPTION','GRADUATION_CERTIFICATE','LICENSE','EMPLOYMENT_CERTIFICATE','HEALTH_INSURANCE_PROOF','PROFILE_PHOTO','ETC'));`
   - EMPLOYMENT_CERTIFICATE 값은 enum/제약에 남기되 신규 업로드 옵션에서만 제외

3. 화면
   - `/my/folder/documents/upload` 의 문서 종류 select: 활성 옵션만 (EMPLOYMENT_CERTIFICATE 제외)
   - PROFILE_PHOTO는 jpg/png 만 허용 (서버 검증)

## 2. 허용 확장자 ppt/pptx

- `application.yml` `resourcehub.upload.allowed-extensions` 에 `ppt,pptx` 추가
  - 결과: `pdf,jpg,jpeg,png,docx,hwp,hwpx,ppt,pptx`
- 업로드 검증 로직(이미 있음) 자동 적용
- 미리보기 정책: ppt/pptx는 자동 변환 안 함 (HWP/DOCX 처럼 preview PDF 별도 업로드 옵션 그대로)

## 3. 태그 표시 제거 (D-05 옵션 A — DB 보존)

- 모든 화면/필터/검색 폼에서 태그 입력·표시 제거:
  - `/myfolder` 문서 상세 — 태그 라벨 제거
  - `/admin/employees/{id}/documents` 문서 카드 — 태그 라벨 제거
  - `/sales/employees/{id}/documents` — 태그 라벨 제거
  - `/search` (M3-08에서도 보장) — 태그 필터 제거
- 컨트롤러 파라미터에서 `tag` 또는 `tags` 제거 (서비스 단에서 무시)
- 태그 관련 컨트롤러/엔드포인트가 따로 있다면 삭제 또는 410 Gone
- DB 테이블/컬럼은 그대로 — soft retire

## 4. NOT-DOING
- 태그 DB drop
- /myfolder 본인 삭제 (M3-06)
- 통합 검색 (M3-08)

## 검증
- 부팅 → V202 적용
- 업로드 화면에 PROFILE_PHOTO 옵션 노출, EMPLOYMENT_CERTIFICATE 옵션 미노출
- jpg, png 로 PROFILE_PHOTO 업로드 OK / pdf 시도 → 거부
- ppt 또는 pptx 파일 업로드 OK
- 모든 문서 화면에서 태그 노출 없음
- DB documents 의 EMPLOYMENT_CERTIFICATE 기존 데이터 그대로 보존
