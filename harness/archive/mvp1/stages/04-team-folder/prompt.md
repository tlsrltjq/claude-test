# Stage 04 — Prompt

> PDF §22 4단계 프롬프트 원본

---

4단계 작업을 진행해줘.

현재 프로젝트는 Java 21, Spring Boot 3.5.x, Gradle 기반의 eActive Resource Hub야.
패키지명은 `com.eactive.resourcehub`야.

이전 단계에서 골격, DB 스키마, 회사 이메일 회원가입+이메일 인증+세션 로그인까지 완성했어.

이번 단계의 목표는 관리자 승인 처리, 팀 관리, 직원 목록, 개인 폴더 자동 생성 기능을 구현하는 것이야.

요구사항:

1. 관리자 전용 페이지 `/admin` (ADMIN만, 일반은 403). 대시보드에는 승인 대기 사용자 수, 전체 사용자 수, 전체 팀 수.

2. `/admin/users/pending`에서 PENDING_ADMIN_APPROVAL 사용자 목록(이름, 이메일, 가입일, 상태, 승인/반려 버튼).

3. 사용자 승인.
   - 팀과 직급 입력.
   - 상태 ACTIVE로 변경.
   - role은 EMPLOYEE 유지.
   - email_verified=true인 사용자만 승인 가능.
   - 이미 ACTIVE인 사용자는 다시 승인 불가.

4. 승인 시 개인 폴더 자동 생성.
   - `folders` 테이블에 사용자별 1개.
   - `folder_name`은 "{사용자 이름} 개인 폴더".
   - 중복 생성 금지.

5. 사용자 반려: 상태 REJECTED. 로그인 불가. 반려 사유는 선택사항.

6. `/admin/teams` 팀 CRUD (이름 unique, 사용자가 소속된 팀은 삭제 불가). 초기 데이터: 개발팀, 영업팀, 기술지원팀, 경영지원팀.

7. `/admin/employees` 직원 목록 (ACTIVE 사용자: 이름, 이메일, 팀, 직급, 역할, 상태, 개인 폴더 존재 여부).

8. `/admin/employees/{userId}` 직원 상세 (이름, 이메일, 팀, 직급, 역할, 상태, 이메일 인증 여부, 개인 폴더 정보). 문서 목록은 표시하지 마.

9. 사용자 팀 변경 (ADMIN만, 직원 상세에서 가능).

10. Service 분리: `AdminUserApprovalService`, `TeamService`, `EmployeeManagementService`, `FolderService`.

11. 보안 설정: `/admin/**`는 ADMIN, `/dashboard`는 로그인 필요, 공개 경로는 기존 유지. JWT 사용 금지.

12. 감사 로그: 사용자 승인/반려, 팀 CRUD, 사용자 팀 변경, 개인 폴더 생성. `audit_logs`에 기록 우선, 어려우면 애플리케이션 로그.

13. README.md에 4단계 검증 방법 추가.

14. 아직 만들지 마: 파일 업로드, 미리보기, 다운로드, 팀장 권한 부여, 개별 폴더 접근 권한, 문서 썸네일, 열람/다운로드 로그.
