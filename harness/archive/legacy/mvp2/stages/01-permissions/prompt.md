# MVP2 Stage 01 — Prompt

MVP2 1단계 작업을 진행해줘.

현재 프로젝트는 Java 21 + Spring Boot 3.5.x + Gradle + PostgreSQL 기반의 eActive Resource Hub.
패키지는 `com.eactive.resourcehub`. MVP1까지 회원가입/이메일 인증/세션 로그인/관리자 승인/팀·폴더/문서 업로드·미리보기·다운로드·권한·썸네일·승인 흐름이 동작 중이야.

이번 단계 목표는 **권한 모델을 ADMIN/SALES/EMPLOYEE 3개로 단순화**하고, `/team/**` 라우팅을 `/sales/**` 로 갈아타는 것이야.

중요:
- JWT 사용 금지, 세션 기반 인증 유지.
- 기존 MVP1 acceptance(특히 ADMIN/EMPLOYEE 흐름)는 깨지지 않아야 함.
- TEAM_LEADER enum 값은 **enum에서 즉시 제거하지 말고** `@Deprecated`로 유지해서 DB 제약 위반을 방지.
- 데이터 마이그레이션으로 기존 TEAM_LEADER 사용자는 SALES로 변경.
- `permissions.permission_type=FOLDER_ACCESS` 흐름(MVP1 7단계)은 일단 그대로 유지.

요구사항:

1. `UserRole` enum에 `SALES` 값 추가. `TEAM_LEADER`는 `@Deprecated` 어노테이션 + Javadoc으로 사용 중단 명시 (enum 값은 남김).

2. Flyway 마이그레이션 추가 — `src/main/resources/db/migration/V100__rename_team_leader_to_sales.sql`
   - `UPDATE users SET role = 'SALES' WHERE role = 'TEAM_LEADER';`
   - 기존 enum 체크 제약(있을 경우)은 SALES를 허용하도록 갱신.

3. `SecurityConfig` 수정.
   - `.requestMatchers("/team/**")` 라인 제거.
   - `.requestMatchers("/sales/**").hasAnyRole("ADMIN", "SALES")` 추가.
   - `/admin/**`, 공개 경로(`/login`, `/signup`, `/signup/**`, `/health`, 정적 리소스), `/dashboard` 등 기존 규칙은 유지.

4. 라우팅 이전.
   - 기존 `/team/members`, `/team/members/{userId}/documents` 컨트롤러를 `/sales/members`, `/sales/members/{userId}/documents` 로 이동 (메서드/뷰 모두).
   - `templates/team/*` → `templates/sales/*` 로 이동, 내부 링크/폼 액션 갱신.
   - 기존 `/team/**` 경로로 들어온 GET 은 `/sales/**` 로 301 또는 302 리다이렉트하는 단일 컨트롤러 1개 추가 (운영 중이라면 북마크 호환).

5. 사용자 역할 변경 화면(MVP1 7단계 `/admin/users/{id}/role`)에서:
   - 선택 가능 역할에서 TEAM_LEADER 제거. ADMIN/SALES/EMPLOYEE만.
   - 서버에서도 TEAM_LEADER 역할로 변경 요청 들어오면 거부 (BusinessException 또는 IllegalArgumentException + 한글 메시지).

6. 권한 체크 서비스 갱신.
   - `DocumentAccessService`, `FolderAccessService`(MVP1 7단계)에서 TEAM_LEADER 분기를 SALES 분기로 변환.
   - SALES 사용자는 **모든 직원의 문서/폴더에 read-only로 접근 가능** (MVP2 PROJECT_SPEC §2 참고).
   - 기존 "본인 팀 직원만" 제약은 SALES에서는 사라진다 — SALES는 전사 조회 가능.

7. `audit_logs` 의 `CHANGE_ROLE` 이벤트 그대로 사용 (변경 사유나 코드 추가 없음).

8. 기존 회원가입 흐름은 건드리지 마. 회원가입 직후 사용자는 EMPLOYEE 그대로.

9. Thymeleaf 템플릿에서 `${#authentication.principal}` 또는 `sec:authorize="hasRole('TEAM_LEADER')"` 같은 표현이 있으면 SALES로 변경 또는 ADMIN으로 강등 검토. 헤더 메뉴에 "영업부 화면" 링크가 SALES/ADMIN에게 보이게 해줘 (지금은 임시로 `/sales/members` 로 연결).

10. README.md (프로젝트 루트) 에 다음 한 줄 추가:
    ```
    ## 역할 (MVP2)
    - ADMIN  : 전체 관리자
    - SALES  : 영업부 — 전사 인력/문서 read-only + 양식 이력서 활용 + 경력 계산기
    - EMPLOYEE : 일반 직원 — 본인 폴더만
    ```

11. NOT-DOING (이번 단계에서 만들지 마):
    - `/sales/profiles` 표 (M2-04)
    - 다운로드 사유 화면 제거 (M2-02)
    - 회원가입 필드 추가 (M2-03)
    - 양식 이력서 (M2-06)
    - 경력 계산기 (M2-07)

검증 방법(README 또는 수동):
- 기존 MVP1 admin/employee 로그인 그대로 동작
- 관리자가 사용자 역할을 SALES 로 변경 가능
- TEAM_LEADER 부여 시도 → 거부
- SALES 로그인 → `/sales/members` 접근 가능 (MVP1 7단계의 팀원 목록 화면이 그대로 떠야 함)
- EMPLOYEE 로그인 → `/sales/**` 접근 시 403
- DB: 기존 TEAM_LEADER 사용자가 모두 SALES로 갱신됨 (V100 적용 확인)
