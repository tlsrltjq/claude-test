# Stage 04 — Acceptance

## 자동 검증 (verify.sh)
- [ ] 4개 Service 클래스 존재
- [ ] `/admin`, `/admin/users/pending`, `/admin/teams`, `/admin/employees`, `/admin/employees/{userId}` 매핑 존재
- [ ] `templates/admin/` 하위 5개 이상 템플릿 존재

## 수동 검증
- [ ] 관리자 계정으로 로그인
- [ ] `/admin` 접속 → 대시보드 카운트 표시
- [ ] `/admin/users/pending` 목록 표시
- [ ] 사용자 승인 → 상태 ACTIVE + `folders` 테이블에 1개 자동 생성
- [ ] 동일 사용자 재승인 시도 시 거부
- [ ] 사용자 반려 → 상태 REJECTED, 해당 계정 로그인 불가
- [ ] `/admin/teams` 팀 생성/수정/삭제 동작 (소속 사용자 있는 팀 삭제 거부)
- [ ] 초기 팀 4개(개발/영업/기술지원/경영지원) 표시
- [ ] `/admin/employees` 목록 정상
- [ ] `/admin/employees/{userId}` 상세에서 팀 변경 가능
- [ ] 일반 EMPLOYEE로 로그인 후 `/admin/**` 접근 시 차단(403 또는 거부 화면)
- [ ] `audit_logs`에 승인/반려/팀 CRUD/팀 변경/폴더 생성 로그가 남는지 (또는 애플리케이션 로그)

## NOT-DOING 확인
- [ ] 파일 업로드 화면 없음
- [ ] 팀장/개별 권한 화면 없음
- [ ] 문서 썸네일/열람 로그 없음
