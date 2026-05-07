# MVP2 Stage 09 — Prompt (STUB)

> 1차 7단계 + M2-08 verified 후 본격 다듬는다.

목표: 경력 계산기에서 산출한 totalMonths 를 `employee_profiles.career_months` 에 저장. 본인 또는 ADMIN.

요구사항(개요):
1. 경력 계산기 화면에 "내 프로필에 저장" 버튼 (본인 EMPLOYEE 일 때) — POST `/my/profile/career-months`
2. ADMIN 화면 (직원 상세) 에서 "이 직원의 경력 보정" 입력 — POST `/admin/employees/{id}/career-months`
3. 저장 후 audit_logs 에 `UPDATE_CAREER_MONTHS` 행
4. (선택) Flyway V105 — `employee_profiles` 에 `career_months_updated_at TIMESTAMPTZ` 추가
5. SALES 는 본인 프로필 X (영업부는 SALES 역할이지 EMPLOYEE 가 아님) — 본인 입력은 EMPLOYEE 만

상세 prompt는 진입 시 보강.
