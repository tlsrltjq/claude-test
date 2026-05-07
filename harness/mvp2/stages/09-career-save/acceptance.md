# MVP2 Stage 09 — Acceptance (STUB)

## 자동 검증
- [ ] `/my/profile/career-months` 매핑
- [ ] `/admin/employees/{id}/career-months` 매핑
- [ ] AuditActionType 에 `UPDATE_CAREER_MONTHS`

## 수동 검증
- [ ] EMPLOYEE 가 경력 계산기 → "내 프로필에 저장" → /sales/profiles 표에서 본인 경력 갱신
- [ ] ADMIN 이 직원 상세에서 임의 직원 경력 보정 → 표 반영
- [ ] SALES 본인은 입력 화면 안 보임 (해당 안 됨)
- [ ] audit_logs UPDATE_CAREER_MONTHS 기록

(상세는 진입 시 보강)
