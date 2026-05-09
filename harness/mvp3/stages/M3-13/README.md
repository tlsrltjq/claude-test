# MVP3 M3-13 — 계정 활성/비활성 토글

## 목적
- 직원 상세에 [활성/비활성] 토글
- 비활성 시 `UserStatus.DISABLED` + 즉시 세션 무효화 + 로그인 거부
- 활성 복귀 시 `UserStatus.ACTIVE`
- permissions 보존 (D-11)
