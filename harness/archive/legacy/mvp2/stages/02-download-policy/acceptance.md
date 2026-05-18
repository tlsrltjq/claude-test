# MVP2 Stage 02 — Acceptance

## 자동 검증
- [ ] `/documents/{id}/download` 매핑 존재 (GET)
- [ ] `/documents/{id}/download/reason` 매핑 부재 (소스에서 제거)
- [ ] `templates/document-download-reason.html` 부재
- [ ] AuditActionType 에 `DELETE_DOCUMENT` 존재
- [ ] 관리자 삭제 라우트 존재 (`/admin/documents/{id}` DELETE 또는 `/delete`)
- [ ] `DocumentDeleteService` 존재

## 수동 검증
- [ ] ADMIN 다운로드 클릭 → 사유 화면 없이 바로 받기
- [ ] SALES 다운로드 클릭 → 사유 화면 없이 바로 받기 (전사 가능)
- [ ] EMPLOYEE 본인 다운로드 OK
- [ ] EMPLOYEE 가 타인 문서 download URL 직접 접근 → 403
- [ ] 다운로드 후 audit_logs 에 reason=NULL 행 추가 (사용자/대상문서/IP/UA)
- [ ] 관리자가 문서 삭제 → 디스크 파일/썸네일/preview 모두 사라짐 + DB 행 사라짐
- [ ] 삭제 후 audit_logs 에 DELETE_DOCUMENT 행
- [ ] EMPLOYEE/SALES 가 삭제 시도 → 거부

## NOT-DOING
- [ ] 휴지통/복구 없음 (영구 제외)
- [ ] 회원가입 폼 변경 없음
- [ ] /sales/profiles 표 없음

## MVP1 회귀
```bash
bash mvp2/harness/scripts/verify.sh 02 --with-mvp1
```
mvp1 6단계 (preview/download)는 다운로드 흐름이 바뀌었으니 회귀 일부는 의도된 차이일 수 있음 — 그 경우 mvp1 6단계 verify 결과를 별도로 검토하고 진행.
