# MVP2 Stage 08 — Prompt (STUB)

> 이 프롬프트는 1차 7단계가 모두 verified 된 뒤 본격 다듬는다. 지금은 윤곽만.

MVP2 8단계 (2차 1번째) 작업.

목표: `/sales/profiles` 의 현재 검색/필터/정렬 결과 + 체크된 컬럼만을 **엑셀(.xlsx)** 로 내보낸다. 문서 파일 자체는 포함하지 않고 "있음/없음" 또는 "파일명"만 표시. 내보내기 audit 로그.

요구사항(개요):

1. build.gradle — Apache POI 의존성 추가 (`org.apache.poi:poi-ooxml:5.x`)
2. 컨트롤러 — `GET /sales/profiles/export?...` (현재 화면 검색 파라미터 그대로 받음)
3. 서비스 — `SalesProfileExporter` (XSSFWorkbook 생성, streaming 권장 SXSSFWorkbook)
4. 컬럼 매핑 — 14컬럼 중 `cols=` 파라미터로 선택된 것만
5. 문서 셀 — 있으면 파일명, 없으면 "없음"
6. audit_logs — `EXPORT_PROFILES` 행 기록 (사용자, 필터/정렬, IP, UA, 시간)
7. 파일명 — `인력프로필_YYYYMMDD_HHmmss.xlsx`
8. 권한 — ADMIN/SALES만, EMPLOYEE 403

자세한 prompt는 1차 종료 후 작성한다.
