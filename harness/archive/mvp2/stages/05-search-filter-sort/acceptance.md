# MVP2 Stage 05 — Acceptance

## 자동 검증
- [ ] `templates/sales/profiles.html` 에 검색 input (`name="q"`)
- [ ] `templates/sales/profiles.html` 에 직급 select (`name="position"`)
- [ ] `templates/sales/profiles.html` 에 정렬 파라미터 사용 (`?sort=` 또는 동급)
- [ ] SalesProfileQuery DTO 또는 Specification 클래스 존재
- [ ] 컬럼 토글 패널 흔적 (예: input checkbox + name 14개)

## 수동 검증
- [ ] 이름 검색 → 결과 좁혀짐
- [ ] 직급 필터 → 해당 직급만
- [ ] 개발자 등급 필터 동작
- [ ] hasResume=true 필터 → 이력서 있는 사용자만
- [ ] 정렬 헤더 클릭 → asc/desc 토글
- [ ] 경력 정렬 → 정확한 순서
- [ ] 컬럼 토글 끄기 → 해당 컬럼 숨김
- [ ] 새로고침 → 토글 상태 유지 (cookie/localStorage)
- [ ] 초기화 → 기본 14컬럼 + 정렬 복귀

## NOT-DOING
- [ ] 컬럼 순서 드래그 안 함 (2차)
- [ ] 엑셀 내보내기 버튼 없음 (08)

## MVP1 회귀
```bash
bash mvp2/harness/scripts/verify.sh 05 --with-mvp1
```
