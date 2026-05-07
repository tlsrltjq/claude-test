# MVP3 M3-11 — Prompt

작업: /sales/profiles 행별 체크 후 엑셀 내보내기.

요구사항:

1. 표 행별 체크박스 (`name="selectedIds"` value=user.id)
2. 상단 체크박스 (전체 선택/해제)
3. "선택 인원 엑셀" 버튼 → POST `/sales/profiles/export?...현재검색파라미터...` body 에 selectedIds[] 다수
4. 0명 선택 시 "엑셀 내보내기" 버튼 disabled 또는 클릭 시 안내
5. 서버:
   - 권한: ADMIN / SALES (EMPLOYEE 403)
   - selectedIds 검증 → 검색 결과 + selectedIds intersect
   - 컬럼은 현재 적용된 프리셋(M3-10)의 columns_json 사용 / 없으면 기본 14컬럼
   - SXSSFWorkbook (streaming) 으로 .xlsx 생성
   - 문서 셀: 있으면 파일명, 없으면 "없음"
   - 파일명: `인력프로필_YYYYMMDD_HHmmss.xlsx`
6. audit_logs.action_type = `EXPORT_PROFILES`, reason = `"selected:N명 / cols:..."` 형태

7. NOT-DOING
   - 묶음 zip / 자리표시자 docx (mvp2 10)

검증:
- 0명 선택 → 비활성/안내
- 3명 체크 후 다운로드 → 그 3명만 들어간 xlsx
- 컬럼이 프리셋 따라감
- audit_logs EXPORT_PROFILES (selected count 로그)
