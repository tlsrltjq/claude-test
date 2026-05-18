# MVP3 M3-11 — Acceptance

## 자동 검증
- [ ] /sales/profiles/export route (POST)
- [ ] AuditActionType EXPORT_PROFILES
- [ ] templates/sales/profiles.html 에 selectedIds 체크박스
- [ ] templates 에 "선택 엑셀" 또는 export 버튼

## 수동 검증
- [ ] 0명 선택 → 버튼 disabled or 안내
- [ ] 3명 체크 → xlsx 다운로드, 그 3명만 포함
- [ ] 프리셋 컬럼이 헤더에 반영
- [ ] audit_logs EXPORT_PROFILES (count 포함)

## NOT-DOING
- [ ] 자리표시자 docx, 묶음 zip 안 함
