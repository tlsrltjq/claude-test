# MVP3 M3-08 — Acceptance

## 자동 검증
- [ ] /search 매핑 존재
- [ ] templates/search.html 존재
- [ ] SearchService 존재
- [ ] templates/search.html 에 q/type/uploader/from/to/folderType 중 절반 이상 input

## 수동 검증
- [ ] 빈 검색 → 본인 권한 문서들이 모두 노출
- [ ] q 입력 → 좁혀짐
- [ ] 권한 없는 문서는 결과에 없음
- [ ] 카드에 출처 (개인/공용/공유) 표기
- [ ] 태그 input/태그 라벨 없음
- [ ] ADMIN/SALES 는 SALES read-only 정책에 맞게 전사 결과

## NOT-DOING
- [ ] 자동완성 X
- [ ] Elasticsearch X
