# MVP2 Stage 04 — Acceptance

## 자동 검증
- [ ] V103 마이그레이션 존재
- [ ] EmployeeProfile에 developerGrade, careerMonths 필드
- [ ] DocumentType에 HEALTH_INSURANCE_PROOF 존재
- [ ] `/sales/profiles` 매핑
- [ ] `/sales/employees/{userId}/documents` 매핑
- [ ] templates/sales/profiles.html 존재
- [ ] SalesProfileQueryService 존재

## 수동 검증
- [ ] 부팅 + V103 적용
- [ ] ADMIN 로그인 → `/sales/profiles` 14컬럼 표
- [ ] SALES 로그인 → 같은 표
- [ ] 이름 클릭 → ADMIN: `/admin/employees/{id}/documents`, SALES: `/sales/employees/{id}/documents`
- [ ] 문서 있는 직원 → 보기/다운로드 버튼 동작
- [ ] 문서 없는 직원 → "없음" 표시
- [ ] 경력은 N년 N개월 표시
- [ ] 나이는 생년월일 기준 자동 계산
- [ ] EMPLOYEE → `/sales/profiles` 접근 시 403
- [ ] SALES 가 직원 폴더에서 다른 사람 문서 미리보기/다운로드 OK, 업로드/수정/삭제 버튼 미노출

## NOT-DOING
- [ ] 검색창 없음 (05에서 추가)
- [ ] 컬럼 토글 없음
- [ ] 양식 이력서 다운로드 버튼 없음 (06)

## MVP1 회귀
```bash
bash mvp2/harness/scripts/verify.sh 04 --with-mvp1
```
