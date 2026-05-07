# MVP3 M3-13 — Acceptance

## 자동 검증
- [ ] /admin/employees/{...}/disable, /activate route 존재
- [ ] AuditActionType CHANGE_USER_STATUS
- [ ] AdminUserStatusService 존재
- [ ] SessionRegistry Bean 또는 expireNow 호출 흔적

## 수동 검증
- [ ] 활성 사용자 비활성 → status=DISABLED, 즉시 다음 요청 /login redirect
- [ ] 비활성 사용자 다시 로그인 시도 → 거부
- [ ] 활성 복귀 후 로그인 가능
- [ ] 본인 비활성 시도 → 거부
- [ ] permissions 보존
- [ ] 직원 목록에 "비활성" 배지

## 회귀
```bash
bash mvp3/harness/scripts/verify.sh M3-13 --with-mvp1 --with-mvp2
```
