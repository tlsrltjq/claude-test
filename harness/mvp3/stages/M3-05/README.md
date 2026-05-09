# MVP3 M3-05 — DocumentType 정비 + 태그 제거 + ppt/pptx 허용

## 목적
- LICENSE 표시명 "정보처리기사" (enum 그대로)
- EMPLOYMENT_CERTIFICATE deprecated (신규 업로드 옵션에서 제외)
- PROFILE_PHOTO 신규 추가 (DB 체크 제약 갱신)
- 허용 확장자에 ppt, pptx 추가
- 태그 화면/필터/입력 모두 제거 (DB 보존)

## 진입 조건
- M3-04 verified

## NOT-DOING
- 태그 테이블 drop (다음 라운드)
- /myfolder 본인 삭제 (M3-06)
