# MVP2 Stage 01 — 권한 단순화 (TEAM_LEADER → SALES)

## 목적
MVP1의 4개 역할(ADMIN/TEAM_LEADER/EMPLOYEE/CUSTOM_VIEWER)을 MVP2의 3개(ADMIN/SALES/EMPLOYEE)로 단순화하고, `/team/**` 라우팅을 `/sales/**`로 갈아탄다.

## 진입 조건
- MVP1 1~4단계 verified (현 상태 그대로 OK)
- 또는 MVP1 어디까지 됐든 SecurityConfig가 동작 중

## 절대 하지 말 것
- 다운로드 사유 화면 변경 (02단계)
- 새 회원가입 필드 추가 (03단계)
- 인력 프로필 표 (04단계)
- 양식 이력서 기능 (06단계)
