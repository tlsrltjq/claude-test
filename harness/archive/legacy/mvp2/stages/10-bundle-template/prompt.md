# MVP2 Stage 10 — Prompt (STUB)

> 1차 7단계 + M2-08, M2-09 verified 후 본격 다듬는다. 단계 규모가 클 가능성이 있어 진입 시 잘게 쪼갠다.

목표: 영업부가 `/sales/profiles` 에서 N명을 체크해 묶음으로 받기.

후보 산출 형식:
- (a) zip — 각 직원 폴더 + meta.json
- (b) 통합 xlsx — 직원당 1행, 첨부 파일은 외부 zip
- (c) docx 템플릿 채우기 — 인력 카드 N개를 단일 문서로

요구사항(개요):
1. `/sales/profiles` 표에 행별 체크박스 + 상단 "선택 묶음 다운로드" 버튼
2. 묶음 다운로드 컨트롤러 + 서비스 (스트리밍)
3. 동적 컬럼/템플릿 — 회사가 사용하는 표준 양식을 등록하고 자리표시자(`{이름}`, `{경력}`)를 치환
4. audit_logs 에 `EXPORT_BUNDLE` 행 + 포함 직원 ID 목록(reason 또는 별도 컬럼)
5. 권한 — ADMIN/SALES, EMPLOYEE 403

상세는 진입 시 보강. 잘게 쪼갤 것을 추천.
