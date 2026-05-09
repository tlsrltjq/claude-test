# MVP3 M3-01 — Context

## SSOT
DECISIONS D-06, D-09. PROJECT_SPEC_MVP3 §3.

## 이전 결과
- mvp2 01에서 SALES 추가, TEAM_LEADER deprecated
- Position enum: REPRESENTATIVE/EXECUTIVE_DIRECTOR/DIRECTOR/GENERAL_MANAGER/DEPUTY_GENERAL_MANAGER/MANAGER/ASSISTANT_MANAGER/STAFF (8개)
- 직급/권한 화면 표시는 영문 또는 displayName 혼재

## 핵심 제약
- enum 값 그대로 (DB 영향 X)
- 정렬 기준이 enum ordinal 인 곳 있으면 상무 위치 변경으로 영향 — 가능하면 sortOrder 별도 메서드로 추출

## 코드가 들어갈 위치
- `V200__add_managing_director_to_position.sql`
- `user/entity/Position.java` (enum 위치 + displayName)
- `user/entity/UserRole.java` (displayName 메서드 추가)
- 영향 받는 모든 templates 헬퍼 호출
