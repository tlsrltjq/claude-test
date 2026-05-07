# MVP3 M3-09 — Prompt

작업 묶음 2개:

## A. /sales/members 정렬

1. 기본 정렬: 직급(Position) 순 — REPRESENTATIVE → STAFF
2. 컬럼 헤더 클릭:
   - "팀" 컬럼 → 팀 이름 가나다 정렬
   - "역할" 컬럼 → UserRole enum 순서 (ADMIN → SALES → EMPLOYEE)
3. 쿼리 파라미터 `sort=position|team|role`, `direction=asc|desc`
4. 정렬 토글 (asc → desc → 기본 복귀)
5. mvp2 05 검색·필터·정렬 패턴과 일관

## B. /admin/employees 검색 + 화면 정리

1. 검색 input 4개:
   - 이름 (`q`) — contains
   - 직급 (`position`) — Position enum select
   - 권한 (`role`) — UserRole select
   - 팀 (`teamId`) — Team select
2. 모두 optional, AND 조건
3. 직급 / 권한 컬럼 한글 표시 (M3-01 displayName 활용)
4. **"개인폴더" 체크 컬럼 삭제** — 이제 모두 가짐
5. 정렬: 기본 이름 가나다, 컬럼 헤더 클릭 동작 (이름/직급/팀/권한)

## 검증
- /sales/members 직급 기본 정렬 — 대표가 위
- 팀 컬럼 클릭 → 팀 정렬
- /admin/employees 4축 검색 동작
- 개인폴더 컬럼 미노출
- 직급/권한 한글 표시
