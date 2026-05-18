# MVP3 M3-01 — Acceptance

## 자동 검증
- [ ] `V200__add_managing_director_to_position.sql` 존재
- [ ] `Position.java` 에 `MANAGING_DIRECTOR` 존재 + displayName "상무"
- [ ] `UserRole.java` 에 `getDisplayName` 존재 + ADMIN→관리자, SALES→영업, EMPLOYEE→사원 매핑
- [ ] templates에 "관리자", "영업", "사원" 한글 출현 (헤더 또는 직원 목록)
- [ ] templates에 "상무" 한글 출현 (직급 select 또는 직원 목록)

## 수동 검증
- [ ] 부팅 후 V200 적용 확인 (`flyway_schema_history`)
- [ ] 회원가입 시 직급 select에 "상무" 옵션 노출
- [ ] 관리자 직원 목록 권한 컬럼이 "관리자/영업/사원" 으로 표시 (영문 X)
- [ ] 직급 컬럼이 "대표/전무/상무/이사/..." 한글
- [ ] `/admin/users/{id}/role` select 에 한글 라벨
- [ ] 기존 데이터 그대로 (Position enum 변경에도 ordinal 영향 없는지 — 정렬 기준이 ordinal인 코드 있으면 sortOrder 메서드로 분리됐는지)

## NOT-DOING
- [ ] DocumentType 변화 없음 (M3-05)
- [ ] /signup 폼 순서 변경 없음 (M3-03)
- [ ] /sales/profiles 컬럼 변경 없음 (M3-10)

## MVP1·MVP2 회귀
```bash
bash mvp3/harness/scripts/verify.sh M3-01 --with-mvp1 --with-mvp2
```
