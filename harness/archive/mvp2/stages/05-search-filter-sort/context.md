# MVP2 Stage 05 — Context

## SSOT
`mvp2/docs/PROJECT_SPEC_MVP2.md` §7 검색/필터/정렬.

## 이전 단계 결과
- /sales/profiles 14컬럼 표 동작
- SalesProfileQueryService가 모든 ACTIVE 사용자 + 프로필 + 최신 APPROVED 문서를 가져옴

## 핵심 제약
- 가능하면 N+1 회피 (검색 결과에 대해서만 그룹 조회)
- 컬럼 토글은 서버 무시 가능 (CSS hidden + JS) — 선택값은 cookie 또는 localStorage
- 정렬은 SQL 단에서. 직급 정렬은 enum ordinal 또는 CASE WHEN

## 코드가 들어갈 위치
- `sales/service/SalesProfileSpecification.java` (또는 동적 JPQL builder)
- `sales/dto/SalesProfileQuery.java` (검색 조건 record)
- `templates/sales/profiles.html` 갱신 — 검색 폼 + 정렬 헤더 + 토글 패널
- 정적 JS (선택): `static/js/sales-profiles-cols.js`
