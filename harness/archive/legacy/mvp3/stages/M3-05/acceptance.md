# MVP3 M3-05 — Acceptance

## 자동 검증
- [ ] V202 마이그레이션 존재
- [ ] DocumentType.java 에 PROFILE_PHOTO
- [ ] DocumentType.java 의 EMPLOYMENT_CERTIFICATE 근처에 @Deprecated
- [ ] application.yml allowed-extensions 에 ppt, pptx
- [ ] templates 어디에도 "태그" 단어 미노출 (서버 응답 또는 직접 grep)

## 수동 검증
- [ ] 업로드 화면에서 PROFILE_PHOTO 옵션 노출, EMPLOYMENT_CERTIFICATE 옵션 미노출
- [ ] PROFILE_PHOTO + jpg 업로드 OK
- [ ] PROFILE_PHOTO + pdf 업로드 → 거부
- [ ] LICENSE 라벨이 "정보처리기사" 로 표시
- [ ] ppt/pptx 업로드 가능
- [ ] 문서 상세에 태그 영역 없음
- [ ] 기존 EMPLOYMENT_CERTIFICATE 업로드본 그대로 노출/다운로드 가능

## NOT-DOING
- [ ] 태그 테이블 drop X
- [ ] 본인 삭제 X (M3-06)
