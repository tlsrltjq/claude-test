# MVP2 Stage 08 — Acceptance (STUB)

## 자동 검증
- [ ] build.gradle 에 `poi-ooxml` 의존성
- [ ] `/sales/profiles/export` 매핑
- [ ] AuditActionType 에 `EXPORT_PROFILES`

## 수동 검증
- [ ] ADMIN/SALES 가 표 화면에서 "엑셀 다운로드" 버튼 클릭
- [ ] 검색/필터 적용 상태에서 다운받은 xlsx 가 그 결과만 포함
- [ ] 선택 컬럼만 포함
- [ ] 문서 셀이 "있음/없음" 또는 파일명만
- [ ] audit_logs 에 EXPORT_PROFILES 행

(상세 acceptance는 1차 종료 후 보강)
