# mvp3/harness

mvp1·mvp2 와 동일한 하네스. **차이점**:

- 회귀 검사 옵션 두 개: `--with-mvp1`, `--with-mvp2`. 큰 변경 후엔 둘 다 켜고 돌리기 권장.
- start.sh 헤더가 MVP3 SSOT(PROJECT_SPEC_MVP3 / DECISIONS / STAGE_PLAN / MIGRATION_FROM_MVP2)를 참조.
- DB 마이그레이션은 **V200~** 만 사용.

## 단계 목록 (전체 완료 — 2026-05-14 기준)

| 단계 | 제목 | 상태 |
|------|------|------|
| M3-01 | 직급·권한 네이밍 + 한글화 | ✅ verified |
| M3-02 | 로그인 UX (ID 저장·비번 찾기·5분 타이머) | ✅ verified |
| M3-03 | /signup 폼 정비 | ✅ verified |
| M3-04 | /dashboard 내 정보 보강 | ✅ verified |
| M3-05 | DocumentType 정비 + 태그 제거 + ppt/pptx | ✅ verified |
| M3-06 | /myfolder 본인 문서 삭제 | ✅ verified |
| M3-07 | /shared/folders/public 전 사원 공용 폴더 | ✅ verified |
| M3-08 | /search 통합 검색 | ✅ verified |
| M3-09 | /sales/members 정렬 + /admin/employees 검색 | ✅ verified |
| M3-10 | /sales/profiles 등급 위젯·프리셋·경력 표시 | ✅ verified |
| M3-11 | /sales/profiles 체크 → 엑셀 (mvp2-08 흡수) | ✅ verified |
| M3-12 | /sales/career-calculator 검색 동작 복구 | ✅ verified |
| M3-13 | 계정 활성/비활성 토글 + 세션 즉시 만료 | ✅ verified |
| M3-14 | 보안·성능·UX 최종 품질 | ✅ verified |
| 16-data-integrity | 데이터 무결성 + 런타임 안전성 | ✅ verified |
| 17-gc | 파일 GC (소프트 삭제 문서 물리 파일 정리) | ✅ verified |
| post-mvp3-* | 설정 페이지·아키텍처·UX·보안 버그픽스 (4개 묶음) | ✅ verified |
| 18-git-hygiene | Git 위생 (.gitignore·민감파일·하드코딩 시크릿) | ✅ verified |

## 사용 흐름

```bash
bash harness/mvp3/scripts/status.sh                      # 전체 진행 현황
bash harness/mvp3/scripts/verify.sh M3-01                # 단일 단계 검증
bash harness/mvp3/stages/18-git-hygiene/verify.sh        # Git 위생 검증
bash harness/mvp3/stages/17-gc/verify.sh                 # 파일 GC 검증
bash harness/mvp3/stages/16-data-integrity/verify.sh     # 데이터 무결성 검증
```
