# MVP2 Stage 06 — Acceptance

## 자동 검증
- [ ] V104 마이그레이션 존재 + `resume_templates` 테이블 정의
- [ ] `ResumeTemplate` entity 존재
- [ ] `ResumeTemplateService` 존재
- [ ] `/admin/resume-template` 매핑 존재
- [ ] `/my/folder/resume-template/download` 또는 동급 매핑 존재
- [ ] `templates/admin/resume-template.html` 존재
- [ ] `templates/my/folder.html` 에 양식 다운로드 링크/버튼

## 수동 검증
- [ ] ADMIN/SALES 가 `/admin/resume-template` 접속 → 업로드 폼
- [ ] PDF 또는 DOCX 업로드 → row 1개 ACTIVE
- [ ] 다시 업로드 → 기존 ACTIVE 행은 ARCHIVED, 새 행 ACTIVE (DB에서 status 컬럼 확인)
- [ ] EMPLOYEE 로그인 → `/my/folder` 에 "양식 다운로드" 버튼 + 클릭 시 다운로드
- [ ] 직원이 받은 파일을 RESUME 유형으로 본인 폴더 업로드 → 영업부 표 이력서 칸에 보기/다운로드
- [ ] EMPLOYEE 가 `/admin/resume-template` 접근 시 403
- [ ] audit_logs 에 RESUME_TEMPLATE 관련 행 기록

## NOT-DOING
- [ ] 양식 다중 버전 관리 안 함 (단일 active)
- [ ] 양식 삭제 화면 없음
- [ ] 경력 계산기 없음

## MVP1 회귀
```bash
bash mvp2/harness/scripts/verify.sh 06 --with-mvp1
```
