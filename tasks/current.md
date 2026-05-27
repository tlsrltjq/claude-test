# 현재 작업 컨텍스트

## 지금 단계: 투입 캘린더 개편 (미구현)

> 상세 기획: `docs/CALENDAR_REDESIGN.md`

## 다음 구현 단계 (순서대로)

- [x] **1단계** — V220: `projects` 테이블 생성 + `Project` 엔티티·Repository
- [x] **2단계** — V221·V222: `project_assignments.project_id` 추가 + 기존 데이터 마이그레이션
- [ ] **3단계** — `ProjectService` 구현 (생성·수정·삭제·인원 일괄 등록·추가·해제)
- [ ] **4단계** — `ProjectController` 구현 (CRUD API + 상세 페이지)
- [ ] **5단계** — V223: `project_assignments`에서 비정규화 컬럼 제거 (project_name·client_name·memo)
- [ ] **6단계** — `CalendarGridBuilder` 재설계 (Project 기반 dayMap)
- [ ] **7단계** — `sales/project-detail.html` 신규 화면 (투입 직원 테이블·수정 모달)
- [ ] **8단계** — `sales/calendar.html` 바(bar) 렌더링 적용
- [ ] **9단계** — 기존 `/sales/assignments/**` 핸들러 정리
- [ ] **10단계** — 테스트 작성 (Project 엔티티·서비스·컨트롤러)

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
→ 위 "다음 구현 단계" 참조 (투입 캘린더 개편)

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
2026-05-27: 전체 정리 커밋(48575e5) 완료.
- 코드: AuditLogService 제거, Tag 제거(V217), AuditActionType 4개 제거, @MockitoBean 전환
- 버그: V218(allocation_rate INTEGER), findByIdForDetailWithTags 제거, calendar LazyInit 수정(findForMonth + findAssignableUsers JOIN FETCH 추가)
- 문서: SECURITY_AND_PERMISSION.md 신설, 6개 파일 archive 이동
- 테스트: 투입 관련 5개 클래스 신규(212케이스), 캘린더 템플릿 렌더링 검증 추가
- Flyway 현재 V218, 다음 V219부터. security-lint 15/15 PASS. Docker /health OK.
