# MVP2 Stage 08 — Context (STUB)

## SSOT
`mvp2/docs/PROJECT_SPEC_MVP2.md` §10 엑셀 내보내기.

## 진입 전제
1차 7단계가 모두 verified 되어 있어야 한다.

## 핵심 제약 (요약)
- Apache POI 의존성 추가
- 검색/필터/정렬 파라미터를 `/sales/profiles` 와 똑같이 받아 같은 데이터 셋을 다시 조회
- 메모리 폭발 방지 — SXSSFWorkbook (streaming) 사용
- audit_logs 에 `EXPORT_PROFILES` 행 기록
