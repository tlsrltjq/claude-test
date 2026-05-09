# MVP3 M3-08 — Prompt

작업: 통합 문서 검색 `/search`.

요구사항:

1. `GET /search` 진입 시:
   - 검색 조건 없음이어도 본인 권한 문서를 카드/리스트로 즉시 표시
   - 본인 권한 = 다음 union:
     1. 내 개인 폴더(folders.owner_user_id = me) 의 모든 문서 (모든 검토 상태 포함)
     2. SHARED_PUBLIC 폴더의 모든 문서
     3. permissions FOLDER_ACCESS 로 받은 폴더의 APPROVED 문서
     4. ADMIN/SALES 면 전사 모든 APPROVED 문서 (선택)
   - 정렬 기본: 최근 업로드 순

2. 검색 파라미터 (모두 optional, AND):
   - `q` — 제목 또는 원본 파일명 contains
   - `type` — DocumentType
   - `uploader` — 업로더 이름 contains
   - `from`, `to` — 업로드일 범위
   - `folderType` — PERSONAL / SHARED_PUBLIC / SHARED_GRANTED (보유 형태)
   - `sort`, `direction`

3. 태그 필터·표시 모두 제거 (M3-05 일관)

4. SearchService — 위 union 쿼리를 효율적으로 (UNION ALL + 페이징, 또는 별도 service 메서드 3개를 합쳐서)

5. 화면 — `templates/search.html`
   - 상단 검색 폼 (q + type + uploader + from/to + folderType + sort)
   - 결과 카드: 썸네일/제목/종류/업로더/업로드일/소속폴더 (개인/공용/공유)
   - 폴더 표기로 어디 출처인지 시각적으로 구분
   - 카드 클릭 → 문서 상세
   - 페이지당 30~50건

6. SecurityConfig — `/search` 는 로그인 사용자만 (이미 anyRequest().authenticated() 라면 자동)

7. NOT-DOING
   - 검색 자동완성
   - 검색 인덱스(Elasticsearch 등) — DB LIKE 로 충분
   - 태그 관련 어떤 것도

검증:
- 본인 폴더 문서 / 공용 폴더 / 받은 권한 폴더 모두 표시
- 빈 검색 — 모든 문서
- q 입력 → 좁혀짐
- 종류 필터 동작
- 기간 필터 동작
- 카드에 출처 표기
- 태그 입력/표시 없음
- 권한 없는 문서는 결과에 절대 포함되지 않음
