# MVP2 Stage 01 — Acceptance

## 자동 검증 (verify.sh)
- [ ] `V100__rename_team_leader_to_sales.sql` 존재 + UPDATE 문 포함
- [ ] `UserRole.java` 에 `SALES` 존재
- [ ] `UserRole.java` 의 `TEAM_LEADER` 라인 근처에 `@Deprecated` 표시
- [ ] `SecurityConfig.java` 에 `/sales/**` 매처 존재
- [ ] `SecurityConfig.java` 에 `/team/**` 매처 부재 (또는 redirect 전용)
- [ ] `DocumentAccessService` / `FolderAccessService` 에 `SALES` 토큰 존재
- [ ] `templates/sales/` 디렉토리 존재 (옛 `templates/team/` 은 비어있어도 됨)
- [ ] `/sales/members` 매핑 컨트롤러 존재

## 수동 검증
- [ ] `./gradlew bootRun` → 부팅 성공 + Flyway V100 적용 (`flyway_schema_history` 확인)
- [ ] DB: 기존 TEAM_LEADER 사용자가 SALES 로 변경됨
- [ ] 기존 ADMIN 계정 로그인 → 모든 흐름 정상
- [ ] 기존 EMPLOYEE 계정 로그인 → `/my/folder` 정상, `/sales/**` 시도 시 403
- [ ] 새 SALES 계정으로 로그인 → `/sales/members` 접근 가능
- [ ] SALES 계정으로 다른 사람 문서 미리보기/다운로드 가능 (전사 read-only)
- [ ] SALES 계정으로 다른 사람 문서 업로드/수정/삭제 시도 → 거부
- [ ] `/admin/users/{id}/role` 화면에 TEAM_LEADER 옵션 없음
- [ ] 강제로 `role=TEAM_LEADER` POST 시 거부 메시지
- [ ] `/team/members` URL 접속 → `/sales/members` 로 리다이렉트
- [ ] `audit_logs` 에 `CHANGE_ROLE` 기록 정상

## NOT-DOING 확인
- [ ] 다운로드 사유 화면은 그대로 (M2-02에서 제거)
- [ ] 회원가입 폼은 그대로 (M2-03)
- [ ] `/sales/profiles` 표 없음 (M2-04)
- [ ] 양식 이력서 / 경력 계산기 없음

## MVP1 회귀 검사
```bash
bash mvp2/harness/scripts/verify.sh 01 --with-mvp1
```
mvp1 1~10단계 verify 가 추가로 통과해야 함.
