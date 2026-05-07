# MVP2 Stage 01 — Deliverables

## SQL
- `src/main/resources/db/migration/V100__rename_team_leader_to_sales.sql`

## Enum 갱신
- `user/entity/UserRole.java` — `SALES` 추가, `TEAM_LEADER` `@Deprecated`

## SecurityConfig
- `common/security/SecurityConfig.java` — `/team/**` → `/sales/**`

## Controller / Templates 이동
- 기존 `team/controller/TeamLeaderController` → `sales/controller/SalesMembersController` (또는 동급)
  - 매핑: `/sales/members`, `/sales/members/{userId}/documents`
- 기존 `templates/team/*` → `templates/sales/*`
- 헤더 메뉴: ADMIN/SALES 만 보이는 "영업부" 링크 (`/sales/members`)

## 리다이렉트
- `LegacyTeamRedirectController` (또는 동급) — `GET /team/**` → 302 `/sales/...`

## 권한 서비스
- `DocumentAccessService` — TEAM_LEADER 분기를 SALES로 변환 + "전사 read-only" 로 확장
- `FolderAccessService` — 동일

## 사용자 역할 관리 화면
- `/admin/users/{id}/role` 의 select 옵션에서 TEAM_LEADER 제거
- 서버에서 TEAM_LEADER 입력 거부

## README
- 프로젝트 루트 README에 MVP2 역할 표 추가
