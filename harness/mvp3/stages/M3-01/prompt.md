# MVP3 M3-01 — Prompt

MVP3 1단계.

이번 작업: **직급·권한 네이밍을 화면에서 한글로 통일하고, 직급 enum에 상무를 추가**한다.

중요:
- enum 영문 이름은 그대로 (UserRole, Position) — DB 영향 없음.
- 화면 표시는 enum의 displayName 또는 i18n 메시지로 한글 매핑.
- TEAM_LEADER는 mvp2에서 이미 deprecated — 그대로.

요구사항:

1. Flyway `V200__add_managing_director_to_position.sql`
   - `users.position` 체크 제약이 있다면 `MANAGING_DIRECTOR` 허용하도록 갱신.
   - Postgres 예: `ALTER TABLE users DROP CONSTRAINT users_position_check; ALTER TABLE users ADD CONSTRAINT users_position_check CHECK (position IN ('REPRESENTATIVE','EXECUTIVE_DIRECTOR','MANAGING_DIRECTOR','DIRECTOR','GENERAL_MANAGER','DEPUTY_GENERAL_MANAGER','MANAGER','ASSISTANT_MANAGER','STAFF'));`
   - 시드 데이터 변경 없음.

2. `Position` enum에 `MANAGING_DIRECTOR("상무")` 추가, **전무 다음, 이사 앞** 위치.
   - enum 순서가 정렬 기준일 가능성 있으니 위치 중요.
   - displayName 메서드/필드 그대로 활용.

3. `UserRole` enum에 한글 displayName 메서드 추가
   ```java
   public String getDisplayName() {
       return switch (this) {
           case ADMIN -> "관리자";
           case SALES -> "영업";
           case EMPLOYEE -> "사원";
           case TEAM_LEADER -> "팀장(사용중지)";
       };
   }
   ```

4. 모든 화면에서 한글 표시 — Thymeleaf 헬퍼 또는 직접 enum.displayName 호출:
   - `/dashboard` (사용자 권한)
   - `/admin/employees` 직원 목록의 권한 컬럼
   - `/admin/employees/{id}` 직원 상세
   - `/admin/users/{id}/role` 의 select option 라벨 — 영문 value + 한글 표시
   - `/admin/users/{id}/permissions` 권한 표시
   - `/sales/profiles` 직급 표시 (이미 displayName 쓰고 있을 가능성 — 확인)
   - 헤더 메뉴 사용자 배지 (권한 한글)

5. 직급 select 옵션에 상무 노출 확인 — 회원가입(`/signup`), 관리자 사용자 승인 시 직급 select 등.

6. NOT-DOING
   - DocumentType 변경 (M3-05)
   - 태그 제거 (M3-05에서 같이)
   - 다른 화면 변경

검증:
- `./gradlew bootRun` → V200 적용
- 회원가입 시 직급 select에 "상무" 노출
- 관리자 직원 목록의 직급/권한 컬럼이 한글로 표시
- 기존 사용자(EXECUTIVE_DIRECTOR 등) 데이터 영향 없음
- `/admin/users/{id}/role` 변경 select 에서 "관리자/영업/사원" 한글 노출
- `audit_logs` 변화 없음
