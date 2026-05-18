# MVP3 M3-01 — 직급·권한 네이밍 + 상무 추가

## 목적
- 권한 화면 표시를 한글(관리자/영업/사원)로 통일 (enum 그대로)
- Position enum에 상무(`MANAGING_DIRECTOR`) 추가, 전무 ↓ 이사 ↑ 위치
- 모든 직원/상세/직급 select에서 displayName(한글) 사용

## 진입 조건
- mvp1·mvp2 1차 7단계 verified

## NOT-DOING
- enum 자체 한글화 X (DB 영향)
- 그 외 화면 변경 X
