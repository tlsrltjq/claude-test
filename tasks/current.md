# 현재 작업 컨텍스트

## 지금 단계: 전면 UI 리디자인 (P0 미착수)

> 상세 기획: `docs/CALENDAR_REDESIGN.md`

## 다음 구현 단계 (순서대로)

- [x] **1단계** — V220: `projects` 테이블 생성 + `Project` 엔티티·Repository
- [x] **2단계** — V221·V222: `project_assignments.project_id` 추가 + 기존 데이터 마이그레이션
- [x] **3단계** — `ProjectService` 구현 (생성·수정·삭제·인원 일괄 등록·추가·해제)
- [x] **4단계** — `ProjectController` 구현 (CRUD API + 상세 페이지)
- [x] **5+9단계** — V223: 비정규화 컬럼 제거 + 구 배정 CRUD 엔드포인트 삭제
- [x] **6단계** — `CalendarGridBuilder` 재설계 (Project 기반 weekBars)
- [x] **7단계** — `sales/project-detail.html` 신규 화면 (투입 직원 테이블·수정 모달)
- [x] **8단계** — `sales/calendar.html` 바(bar) 렌더링 적용
- [x] **10단계** — 테스트 작성 (Project 엔티티·서비스·컨트롤러)

## 완료 기준
- `./gradlew build` BUILD SUCCESSFUL
- `bash scripts/security-lint.sh` 15/15 PASS
- 프로젝트 등록 → 캘린더 바 표시 → 상세 페이지 진입 흐름 정상 동작

---

## 완료된 항목 (이전 단계)
- [x] V214 SQL (users.address)
- [x] V215 SQL (allowed_emails)
- [x] AllowedEmail 엔티티·리포지토리·EmailAllowlistService
- [x] SignupRequest·SignupService 개편 (도메인 제거, 허용 이메일·동의 검증)
- [x] SignupController·ForgotPasswordController 개편
- [x] SettingsController·SettingsService 확장 (이름·주소·팀)
- [x] AdminInitializer companyEmailDomain 제거
- [x] signup.html (전체 이메일, 주소, 개인정보 동의 모달)
- [x] login-forgot.html (전체 이메일 입력)
- [x] settings.html (아이디·상태 제거, 이름·주소·팀 편집)
- [x] SignupValidationTest 업데이트
- [x] 관리자 허용 이메일 관리 UI (AdminController + allowed-emails.html)
- [x] 관리자 모든 화면 네비게이션에 '가입 허용' 링크 추가
- [x] 대시보드 간소화 (다운로드 이력 카드 제거, 공유 폴더 카드 제거, 공용 폴더 내 업무 섹션으로 통합)
- [x] 공유 폴더(Permission 기반) 제거 (SharedFolderController 2개 라우트 제거, SharedFolderService 정리, templates 삭제)
- [x] 재직증명서 레이아웃 개선 (파일명 truncation 수정, 최신 파일 강조, 템플릿 생성 버튼 제거)
- [x] 통계 화면 확장 (업로드 통계 카드 + 최근 업로드 이력 테이블 추가)
- [x] HARNESS.md·CHANGELOG.md·tasks/current.md 문서 최신화

## 다음 진행 항목
→ 전면 UI 리디자인 P0부터 순서대로 진행

### 리디자인 단계 계획
- [x] **P0** — `app.css` 전면 재작성 + `fragments/sidebar.html` 신규 + `admin/dashboard.html` 쉘 구조 적용
- [x] **P1** — 인증 화면: `login.html`, `signup.html` 2-column 리디자인 (forgot/verify 페이지는 기존 centered card 유지)
- [x] **P2** — 대시보드: `dashboard.html` app-shell + sidebar fragment 적용 (`admin/dashboard.html`은 P0에서 완료)
- [x] **P3** — 어드민 직원: `admin/employees.html`, `admin/employee-detail.html`, `admin/employee-documents.html`, `admin/employee-document-detail.html`
- [x] **P4** — 어드민 나머지: `admin/teams.html`, `admin/team-edit.html`, `admin/documents-*.html`, `admin/statistics.html` 외 6개
- [x] **P5** — 영업: `sales/calendar.html`, `sales/members.html`, `sales/profiles.html`, `sales/career-calculator.html` 외 3개
- [x] **P6** — 내 화면: `my/folder.html`, `my/upload.html`, `my/activity.html`, `search.html`, `settings.html` 외 2개
- [x] **P7** — 에러 페이지: `error/403.html`, `error/404.html`, `error/500.html`

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V216.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 15/15 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓

## 이전 세션에서 멈춘 곳
2026-05-28: Design System v3 모던 플랫 재개선 완료.
- app.css: accent #2563eb, sidebar #1e293b/240px, 테이블 헤더 전부 라이트(#f1f5f9+대문자), 카드 shadow-sm, 메뉴아이템 스프링 cubic-bezier 애니메이션
- dashboard.html: welcome-hero 그라디언트 + 아바타 + 전체너비 메뉴그리드(설명 텍스트 포함, 5개 메뉴)
- sales/project-detail.html: horizontal 정보카드 + assign-table + 아바타 멤버 테이블

**P3~P7 완료 (이번 세션):**
- P3: admin 직원 4개 (employees, employee-detail, employee-documents, employee-document-detail)
- P4: admin 나머지 13개 (teams, team-edit, statistics, documents-review, document-review-detail, documents-expiry, allowed-emails, certificate, gc, project-settings, user-permissions, user-role, resume-template)
- P5: sales 7개 (members, member-documents, employee-documents, calendar, project-detail, profiles, career-calculator)
- P6: my 4개 (folder, upload, activity, document-detail) + search, settings, shared/public-folder, dashboard(기완료)
- P7: error 3개 (403, 404, 500) — 독립 레이아웃 유지, Bootstrap JS 추가
- 빌드: BUILD SUCCESSFUL, security-lint 15/15 PASS

**다음 작업 없음 — UI 리디자인 완전 완료**
