# MVP2 Stage 09 — Context (STUB)

`mvp2/docs/PROJECT_SPEC_MVP2.md` §9 (M2-09).

## 진입 전제
- M2-07 단독 계산기 verified
- M2-08 엑셀 verified

## 핵심 제약
- 본인은 EMPLOYEE만 자기 값 수정 가능 (SALES/ADMIN 본인은 EMPLOYEE가 아니므로 본인 입력 화면 미노출)
- ADMIN 은 임의 직원 값 보정 가능
- 음수/비정상 값 거부
- 변경 시 audit_logs 기록
