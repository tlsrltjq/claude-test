# MVP3 M3-08 — Context

이전: 본인/공용/공유 폴더 흐름 모두 마련됨 (M3-07 까지). 통합 검색 화면이 비어 있음.

핵심:
- 권한 union 쿼리 — N+1 회피
- ADMIN/SALES 의 전사 read-only 가 검색 결과에 어떻게 반영되는지 결정 (PROJECT_SPEC: SALES read-only 전사) — 검색 결과에 모든 사용자의 APPROVED 문서를 포함하는 게 일관됨

위치:
- `search/controller/SearchController`
- `search/service/SearchService`
- `search/dto/SearchQuery`, `SearchResultRow`
- `templates/search.html`
