# MVP2 Stage 01 — Context

## SSOT
- `mvp2/docs/PROJECT_SPEC_MVP2.md` §2 권한 구조
- `mvp2/docs/MIGRATION_FROM_MVP1.md` §1~§3 (권한 모델, 라우팅, SecurityConfig)

## 이전 단계 결과 (MVP1)
- UserRole = {ADMIN, TEAM_LEADER, EMPLOYEE}
- `/team/members`, `/team/members/{id}/documents` 가 TEAM_LEADER에게 본인 팀만 노출
- DocumentAccessService / FolderAccessService 가 TEAM_LEADER 분기를 가지고 있음
- /admin/users/{userId}/role 에서 역할 변경 가능

## 이번 단계 핵심 제약
- enum 값 TEAM_LEADER는 즉시 삭제하지 않음 (DB 제약/기존 데이터 보호) — `@Deprecated` 표시 + 신규 부여 차단
- SALES는 **전사 read-only** — MVP1 TEAM_LEADER의 "같은 팀만" 제한이 SALES에서는 사라짐 (PROJECT_SPEC_MVP2 §2)
- 라우트 `/team/**` 는 사용자 북마크 호환을 위해 `/sales/**` 로 리다이렉트

## 코드가 들어갈 위치
- 마이그레이션: `eactive-resource-hub/src/main/resources/db/migration/V100__rename_team_leader_to_sales.sql`
- 컨트롤러: `team/controller/*` → `sales/controller/*` (패키지도 이동) 또는 기존 `team/` 패키지를 그대로 두고 `@RequestMapping`만 `/sales/**`로 변경 (보수적)
- 권한 서비스: `document/service/DocumentAccessService`, `permission/service/FolderAccessService`
- 템플릿: `templates/team/` → `templates/sales/`

## 권장 접근법
1. enum + 마이그레이션부터 (DB 일치)
2. SecurityConfig 수정
3. 컨트롤러/서비스 권한 분기 수정
4. 템플릿 이동
5. 리다이렉트 추가
6. 수동 테스트
