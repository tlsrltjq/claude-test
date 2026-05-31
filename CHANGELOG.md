# 변경 이력

> 형식: `YYYY-MM-DD | 단계 | feat/fix/chore/docs/refactor/test: 내용`
> 한 줄씩 누적. 옛 상세 이력은 `docs/archive/CHANGELOG-detailed.md` 참조.

---

2026-05-31 | fix | fix: 통계 페이지 LazyInitializationException — AuditLogRepository LEFT JOIN FETCH u.team 추가, findByActionTypeWithUser 반환 타입 Page→List 변경
2026-05-31 | fix | fix: 직원 문서 카드 레이아웃 통일 — admin·sales employee-documents.html 양쪽 doc-icon-area 래퍼 적용, 파일명 accent 색상, 날짜 yy.MM.dd, 관리자 미리보기·다운로드·삭제 버튼 정비
2026-05-31 | fix+chore | fix: GlobalModelAttributeAdvice 추가 — 사이드바 currentUser 분리로 직원 상세 페이지 프로필 깨짐 해결 / fix: 통계 이력 테이블 레이아웃(colgroup·members-table·팀 컬럼 추가) / fix: 직원 문서 목록 테이블 스타일 정비(members-table·썸네일 36px·colgroup) / chore: 사이드바 팀 관리·팀 프로젝트 설정 2항목 제거
2026-05-30 | feat | feat: 사이드바 관리자 메뉴 2항목 추가 — 팀 프로젝트 설정·만료 문서
2026-05-30 | refactor | refactor: dead code GC — CertificateService.getTemplates()·SalesProfileExporter byte[] 오버로드 2개·ProfileRow 미사용 메서드 2개 제거
2026-05-30 | test+docs | test: E2E 단일 클래스(E2ETest 24케이스)·단위 테스트 8클래스(93케이스) 추가 — 393개 전 통과 / docs: 전체 문서(HARNESS·testing·architecture·data-model·decisions·tasks) V227·393테스트 기준 최신화
2026-05-29 | test | test: 테스트 스위트 302개 구축 — SearchService·DocumentUpload·RouteSecurityTest 단위테스트, Testcontainers DocumentRepositoryIntegrationTest 11개, build.gradle Testcontainers 의존성 추가 / fix: 문서검색 500 에러(PostgreSQL null LocalDateTime sentinel 패턴 적용)
2026-05-29 | perf+feat | feat: 회원가입 개인정보 동의서 7섹션 모달·5체크박스 전체동의·내용보기 스크롤·폼 검증 / perf: ZIP·Excel 스트리밍·DeployStats COUNT·검색 DB 위임·PDF 썸네일 tempfile·V227 audit_logs 인덱스 / fix: 캘린더 CANCELLED cascade·memberCounts·dayMap·사이드바 순서
2026-05-29 | fix | fix: 대시보드 LazyInitializationException 수정 — findByUserId에 JOIN FETCH pa.project 추가, 사원 500·영업 빈화면 해결
2026-05-29 | feature | feat: 파일 크기 임계값 10MB→20MB(즉시승인) / 100MB 이하 허용, zip·xlsx·xls 확장자 추가
2026-05-29 | docs | docs: architecture·data-model·decisions·spec·frontend·tasks/current.md V226 기준 전체 최신화
2026-05-29 | feature | feat: 허용 이메일 일괄 등록 — 텍스트(쉼표·줄바꿈) + 엑셀(.xlsx/.xls) 업로드, 메모 제거, 2컬럼 레이아웃·검색 필터
2026-05-29 | fix | fix: 계정 삭제 FK 전면 수정(V226) — project_assignments SET NULL+user_name 스냅샷, 누락 FK 6개 CASCADE/SET NULL 추가
2026-05-29 | feature | feat: join_date(입사일) 필드 추가 — 회원가입 선택 입력, 설정 편집, 재직증명서 {{입사일}} 연동
2026-05-29 | ux | feat: 대시보드 전면 개선 — 내 프로젝트 현황, 영업 KPI 재배치, 관리자 메뉴 4종
2026-05-29 | ux | feat: 기술등급 산출 직원 선택 autocomplete 드롭다운 교체 — 스크롤 리스트 제거·키보드 탐색·DOM 빌드 XSS 방지
2026-05-29 | ux | fix: 인력표 개선 — 등급 통계 인라인화(초기화 옆)·투입 한 줄·프리셋 복원·미설정 등급·ZIP 옆 구분선
2026-05-29 | ux | feat: 전사인력조회 페이징(30명)·정렬 시각화·직원문서목록 카드/리스트 뷰 토글
2026-05-29 | ux | fix: 사이드바 접힘 로그아웃 텍스트 버그·검색 공유 폴더 유형 제거·대시보드 활동 기록 버튼 제거
2026-05-29 | ux | feat: 내 폴더·공용 폴더 카드/리스트 뷰 토글 + 검색바 정렬 수정 + 문서 상세 표 여백 개선
2026-05-29 | scheduler | feat: 프로젝트·배정 상태 자동 전환 스케줄러 — 매일 자정 ACTIVE→ENDED·PLANNED→ACTIVE
2026-05-29 | calendar | feat: 캘린더 전체 프로젝트 리스트(ACTIVE+PLANNED+ENDED 접기) + 배정 인원 수 + 인력현황 툴바 1줄(검색+통계 통합) + AJAX 월 이동 슬라이드 애니메이션
2026-05-29 | calendar | feat: 투입 캘린더 2모드 리디자인 — 캘린더 뷰(현재 진행중 프로젝트 목록·잔여일 배지) + 인력 현황 뷰(투입중/미투입 테이블·통계 카드), 모드 탭 localStorage 유지

## UI 디자인 개선 — 색상 통일·버그 수정·리디자인 계획

2026-05-28 | refactor | refactor: 리팩토링 1단계 — PasswordValidator 공통 유틸, DocumentPreviewResolver @Component, DocumentUploadService private helper 분리
2026-05-28 | refactor | refactor: 리팩토링 2단계 — ResourceNotFoundException 도입, GlobalExceptionHandler 개선 (IllegalStateException→500 버그 수정)
2026-05-28 | refactor | perf: 리팩토링 3단계 — MyFolderService N+1 제거 (findLatestVersionsByDocumentIds 배치 쿼리, findByFolderIdAndStatusWithVersion 사용)
2026-05-28 | refactor | refactor: 리팩토링 4단계 — DTO 검증 애너테이션 추가 (DocumentUploadRequest, Project 관련 DTO), MethodArgumentNotValidException 핸들러
2026-05-28 | test | test: 리팩토링 5단계 — PasswordValidatorTest(7케이스), DocumentPreviewResolverTest(10케이스) 추가
2026-05-28 | UI | plan: 전면 UI 리디자인 계획 수립 — 사이드바 쉘 구조·새 디자인 토큰·P0~P7 단계 정의 (미착수)
2026-05-28 | UI | feat: 전면 UI 리디자인 P3~P7 완료 — admin 17개·sales 7개·my/search/settings/shared 8개·error 3개 전체 app-shell+sidebar 마이그레이션 (topnav 제거, page-hd 패턴, 로딩 오버레이 통일)
2026-05-28 | UI | feat: Design System v3 — 모던 플랫 재개선 (테이블 헤더 라이트, 카드 shadow, 메뉴 스프링 애니메이션, dashboard welcome-hero, project-detail 멤버 아바타 테이블)
2026-05-28 | UI | fix: .page-wrap max-width:1200px 제거 — 전체 화면 너비 활용, 좌우 패딩 2rem
2026-05-28 | P0 | feat: app.css Design System v2 전면 재작성 + fragments/sidebar.html 신규 + admin/dashboard.html 쉘 구조 적용
2026-05-28 | P1 | feat: login.html·signup.html 2-column 레이아웃 리디자인 (auth-2col·auth-left·auth-right·auth-form-box)
2026-05-28 | P2 | feat: dashboard.html app-shell + sidebar fragment 적용 — topnav·container 제거, 쉘 구조 통일
2026-05-28 | UI | chore: app.css 전체 색상 #0d6efd으로 통일 (Bootstrap 기본 파란색, 기존 #1c2538·#2563eb 제거)
2026-05-28 | UI | fix: SecurityConfig CSP 확장 — *.daumcdn.net·*.kakao.com (script-src·frame-src·connect-src·img-src)
2026-05-28 | UI | fix: signup.html Bootstrap JS 추가 + 주소검색 script https:// 명시 (개인정보 모달·주소검색 팝업 수정)

## UI 디자인 개선 — 통합 디자인 시스템 도입

2026-05-28 | UI | feat: static/css/app.css 신규 생성 — 단일 디자인 시스템 (nav #1c2538·accent #2563eb·flat card)
2026-05-28 | UI | refactor: 전체 41개 템플릿 인라인 <style> 블록 제거 → app.css 단일 외부 CSS로 통합
2026-05-28 | UI | feat: 주소검색 Daum/Kakao Postcode API 위젯 추가 (signup.html·settings.html)

## 캘린더 개편 — 권한 확장·팀 필터·프로젝트 리스트·LazyInit 수정

2026-05-28 | 캘린더 | fix: ProjectAssignmentRepository — findActiveOn/findEndingSoon/findPlannedFrom/findActiveByUserId/findOverlapping에 JOIN FETCH pa.project 추가 (LazyInitializationException 방지)
2026-05-28 | 캘린더 | feat: 프로젝트 CRUD·멤버 관리 엔드포인트 /admin/projects/** → /sales/projects/** 이동, ADMIN·SALES 동일 권한 부여
2026-05-28 | 캘린더 | feat: 캘린더 하단 프로젝트 리스트 추가 (기간·최고직급 대표자·인원수), projectTeam=true 팀만 노출

## 전체 정리 — 미사용 코드 제거·문서 재편·Docker 버그 수정·테스트 보강

2026-05-27 | 정리 | chore: 전체 정리 커밋 — AuditLogService·Tag 제거, V217·V218, LazyInit 수정, docs/SECURITY_AND_PERMISSION.md, 테스트 212케이스 (48575e5)

## 프로젝트 정리 — 문서 최신화·미사용 코드 제거

2026-05-27 | 정리 | docs: HARNESS.md 전면 개편 — workspace 미구현 표시, V216 반영, 절차 구조화
2026-05-27 | 정리 | docs: spec.md V216 기준·project_assignments 섹션 추가, frontend.md 캘린더 화면 추가
2026-05-27 | 정리 | docs: architecture.md SalesMemberService·ProfileRow·audit/dto·DeployStats 위치 정확히 기재
2026-05-27 | 정리 | docs: testing.md 미기재 14개 테스트 파일(131 케이스) 목록 추가, 미커버 영역 현행화
2026-05-27 | 정리 | refactor: Document.java — Tag ManyToMany·addTag·removeTag·moveToTrash·restore 제거 (호출 없음)
2026-05-27 | 정리 | chore: HomeController Javadoc 스테일 주석 제거, package-info.java 9개 phase 번호 → 현행 설명으로 수정

## 기능 개편 — 회원가입·설정·폴더·인증서·통계

2026-05-27 | 기능 개편 | feat: 이메일 사전등록(allowed_emails) 방식 회원가입·관리자 허용 이메일 UI 추가 (V214·V215 마이그레이션 포함)
2026-05-27 | 기능 개편 | feat: 설정 화면 이름·주소·팀 편집, 개인정보 동의 모달, 비밀번호 찾기 전체 이메일 입력 전환
2026-05-27 | 기능 개편 | refactor: 공유 폴더(Permission 기반) 제거, SHARED_PUBLIC 공용 폴더만 유지
2026-05-27 | 기능 개편 | feat: 통합 워크스페이스(/workspace) 신설 — 내 폴더·공용 폴더 통합 뷰
2026-05-27 | 기능 개편 | feat: 통계 화면 업로드 통계·최근 업로드 이력 추가
2026-05-27 | 기능 개편 | fix: 재직증명서 파일명 truncation 수정·최신 파일 강조·템플릿 생성 버튼 제거
2026-05-27 | 기능 개편 | feat: 관리자 모든 화면 네비게이션에 '가입 허용' 링크 추가
2026-05-27 | 기능 개편 | docs: spec/frontend/data-model/decisions/qa-checklist/사용법 전 문서 V215 기준 최신화 (허용이메일·공용폴더 업로드·설정탭·통계·재직증명서 변경 반영)

## 하네스 개편

2026-05-19 | 정합성 감사 | chore: 소스 기준 하네스·문서 정합성 감사 — templates/team/ 빈 폴더 삭제, security-lint 15/15 PASS 확인, architecture.md 누락 클래스 식별(SalesMemberService·ProfileRow·audit/dto)
2026-05-18 | 하네스 개편 | chore: 범용 양식 도입 — CLAUDE.md / HARNESS.md / tasks/current.md / docs/architecture.md / docs/decisions.md / start_session.sh / end_session.sh 신설, 옛 자산은 harness/archive/legacy/ 와 docs/archive/ 로 이동

## 21단계 — 재직증명서 자동 발급

2026-05-18 | 운영 마감 | feat: 관리자 대시보드 빠른 메뉴에 파일 GC 카드 추가 (93cb444)
2026-05-18 | 운영 마감 | docs: 역할별 사용 가이드 업데이트 (f632159)
2026-05-18 | 운영 마감 | fix: 경력 계산기 직원 선택 시 학력 정보 초기화 (86592dc)
2026-05-18 | 운영 마감 | docs: 역할별 사용 가이드 추가 — 직원·영업·관리자 (fb3f8cf)
2026-05-18 | 운영 마감 | feat: 인력표 개선 및 문서 유형 확장 (d80e4be)
2026-05-15 | 21-certificate | chore: 재직증명서 시스템 하네스·문서 업데이트 + 전체 관리자 nav 링크 추가 (28129b5)
2026-05-15 | 21-certificate | docs: 관리자 사용가이드에 재직증명서 발급 섹션 추가 (cedacfb)
2026-05-15 | 21-certificate | fix: certificate 서비스 개발 포트 5000→5001 변경 — macOS Control Center 충돌 (73e3788)
2026-05-15 | 21-certificate | feat: 재직증명서 자동 발급 시스템 추가 — Python+Flask+LibreOffice 컨테이너 (73f758f)

## 20단계 — 운영 배포 준비

2026-05-15 | 20-ops-readiness | docs: 시놀로지 NAS 스토리지 전환 가이드 추가 — MinIO / NFS 두 가지 방법 (a89ad2a)
2026-05-15 | 20-ops-readiness | docs: 사용가이드 업데이트 — 업로드 취소·중복탐지·역할변경·파일GC (5eb1fce)
2026-05-15 | 20-ops-readiness | config: 운영 환경 앱 로그 레벨 WARN → INFO 상향 (6306f65)
2026-05-15 | 20-ops-readiness | docs: 하네스 스테이지 19·20 추가 + 문서 업데이트 (9f08867)
2026-05-15 | 20-ops-readiness | ops: 2단계 운영 준비 — 로그 파일 저장, 백업 크론 등록, 체크리스트 완료 (7c4bca3)
2026-05-15 | 20-ops-readiness | infra: 운영 배포 스크립트 추가 및 compose 파일 혼용 방지 — scripts/deploy.sh (0360807)
2026-05-15 | 20-ops-readiness | infra: Caddy 리버스 프록시 + HTTPS 설정 추가 — Let's Encrypt 자동 인증서 (2127594)
2026-05-15 | 20-ops-readiness | docs: 배포 체크리스트 작성 — 3단계 분류(필수/권장/선택) (78f98be)

## 19단계 — 업로드 안전성 강화

2026-05-15 | 19-upload-hardening | fix: 고아 파일 방지 및 주기적 orphan 스캔 GC 추가 — DocumentFileGcService.runOrphanScan (50ff30f)
2026-05-15 | 19-upload-hardening | ux: 업로드 중 취소 기능 추가 — XHR abort 기반 (c137014)
2026-05-14 | 19-upload-hardening | feat: 중복 파일 탐지 — SHA-256 체크섬 비교로 동일 파일 재업로드 차단 (15aaa05)
2026-05-14 | 19-upload-hardening | security: 파일 magic bytes 검증 + 동시 업로드 race condition 방지 (V211 partial unique index) (4dae09f)

## post-MVP3 마감 (UX / 보안 픽스)

2026-05-14 | post-MVP3 | ux: 경력계산기 2컬럼 레이아웃 — 결과·저장을 오른쪽 sticky 패널로 이동 (6b87acb)
2026-05-14 | post-MVP3 | fix: 경력 저장 시 실제 총 경력일수로 교정 (86b7b7b)
2026-05-14 | post-MVP3 | feat: 인력표 엑셀 문서 컬럼 파일명 → O/X 표시로 변경 (d6276bb)
2026-05-14 | post-MVP3 | fix: 업로드 XHR → JSON 응답 방식 전환으로 오류 메시지 오판 해소 (6f963ca)
2026-05-14 | post-MVP3 | fix: 업로드 XHR 오류 처리 개선 — 서버 실패 시 오류 메시지 직접 표시 (41948e5)
2026-05-14 | post-MVP3 | fix: 업로드 500·검색 403 버그 수정 (d5fc9ea)
2026-05-14 | post-MVP3 | docs: mvp3-spec·security-policy 문서 현행화 (d28878f)
2026-05-14 | post-MVP3 | feat: /sales/profiles ZIP 묶음 다운로드 — bundle-template 흡수 (a97ba6b)
2026-05-13 | post-MVP3 | ux: 업로드 진행률 바 추가 — XHR 방식 전환 (ed2c47e)
2026-05-13 | post-MVP3 | fix: 역할 변경 시 대상 사용자 세션 즉시 만료 (93bb648)
2026-05-12 | post-MVP3 | fix: 본인 업로드 파일 검토 상태 무관 접근 허용 (c006715)
2026-05-12 | post-MVP3 | fix: 전 사원 공용 폴더 EMPLOYEE 접근 시 403 수정 (26e6295)
2026-05-09 | post-MVP3 | ux: 업로드 종류별 제목 placeholder + 다운로드 이력 파일명 표시 (0fd83ad)
2026-05-09 | post-MVP3 | feat: 직급 변경 권한 관리자 전용으로 제한 (3a93907)
2026-05-09 | post-MVP3 | docs: 사용법 문서 3개 사실 오류 수정 (febde25)

## 17단계 — 파일 GC

2026-05-09 | 17-gc | feat: 파일 GC — 소프트 삭제 문서 물리 파일 자동 정리, @Scheduled(cron 0 0 2 * * *), V210, /admin/gc (4843234)

## 16단계 — 데이터 무결성 + 아키텍처 정리

2026-05-09 | 16-data-integrity | refactor: 컨트롤러 Repository 직접 주입 제거 + 문서·사용법 가이드 추가 (8da9eaa)
2026-05-08 | 16-data-integrity | perf: 직원 목록·영업 인력 조회 DB 레벨 필터링·페이지네이션 전환 (001e4cc)
2026-05-08 | 16-data-integrity | fix: SalesProfileQueryService toMap 중복 키 IllegalStateException 수정 (6d0d849)

## post-MVP3 / 설정 페이지 + UI 리디자인

2026-05-08 | post-MVP3-settings | feat: 설정 페이지 추가 + 자격증 종류 필드 제거 — /settings (9e7e54f)
2026-05-08 | post-MVP3-settings | feat: 경력계산기 자격증을 정보처리기사 단일로 고정 (d7afe13)
2026-05-08 | post-MVP3-ui | feat(ui): 미개선 화면 전체 UI 개선 — 19개 파일 (febc70d)
2026-05-08 | post-MVP3-ui | feat(ui): 관리자 패널·통계 UI 전면 개선 (f58f619)
2026-05-08 | post-MVP3-ui | feat(ui): 경력 계산기 UI 개선 (a62e684)
2026-05-08 | post-MVP3-ui | feat(ui): 전사 인력 목록·인력표 UI 개선 (91b3915)
2026-05-08 | post-MVP3-ui | feat(ui): 문서 검색·다운로드 이력 UI 개선 (c8ba3bc)
2026-05-08 | post-MVP3-ui | feat(ui): 공유 폴더·전사 공용 폴더 UI 개선 (a9c8764)
2026-05-08 | post-MVP3-ui | feat: 문서 메타 필드(certType·issuedDate·degreeType) + 경력계산기 자동채우기 + UI 개선 (8458637)
2026-05-08 | post-MVP3-ui | feat(dashboard): UI 전면 리디자인 (aef751d)
2026-05-08 | post-MVP3-ui | fix: 팀 자동 시딩 제거 + 로그인 UI 개선 (e0cf095)
2026-05-08 | post-MVP3-ui | feat(signup): UI 리디자인 + 전화번호·생년월일 자동 포맷 (762b67d)
2026-05-08 | post-MVP3 | fix: 경력저장 초기화·이메일 중복발송·인증시간·폴더자동생성·업로드 5건 (5456d40)
2026-05-08 | post-MVP3 | fix(career-calculator): 합계 일자 SpEL 오류 수정 → 저장 버튼 미표시 해결 (6f0bde9)
2026-05-08 | post-MVP3 | fix(V207): folders 테이블 컬럼명 오류 수정 — owner_id → owner_user_id (3b861ba)

## MVP3 M3-01 ~ M3-14 (보안·성능·UX 마감)

2026-05-08 | M3-14 | feat(ux): 업로드 버튼 중복 클릭 방지 + 경력 계산기 설명 문구 수정 (dd86e16)
2026-05-08 | M3-14 | feat: GlobalExceptionHandler — ResponseStatusException·제네릭 예외 처리 (90cd19a)
2026-05-08 | M3-14 | feat(perf): ThumbnailService @Async 비동기화 — 업로드 응답 지연 제거 (2d0e06e)
2026-05-08 | M3-14 | fix(security): 로그 보안 — 파일 절대 경로·비밀번호 재설정 코드 로그 제거 (e3264f4)
2026-05-08 | M3-14 | fix(security): 로그인 무한 리다이렉트 수정 + HTTP 보안 헤더 추가 (c7f3559)
2026-05-08 | M3-14 | chore(lint): 보안 린터 검사 13~15 추가 — HTTP 헤더·코드 로그·env 기본값 (e1ff807)
2026-05-08 | M3-14 | refactor: FileUtils.extension() 공통 유틸로 extension() 중복 제거 (362eb4a)
2026-05-08 | M3-14 | feat: S3/Cloudflare R2 스토리지 지원 + 환경변수 기본값 보안 강화 (5c4012d)
2026-05-08 | M3-14 | feat(mvp1-10): 배포 파일 + 운영 보안 체크리스트 추가 (ebb5655)
2026-05-08 | M3-14 | feat(mvp2-14): 문서 소프트 삭제 구현 — deletedAt/deletedBy 필드 (V206) (ef0db84)
2026-05-08 | M3-04 | feat(M3-04): dashboard 마이프로필에 연락처 행 추가 (0f2df0e)
2026-05-08 | M3-05 | fix(M3-05): EMPLOYMENT_CERTIFICATE @Deprecated 마킹 + V202 (9487689)
2026-05-08 | M3-07 | fix(M3-07): V207 SHARED_PUBLIC 마이그레이션 생성 + 검사 범위 확장 (554b1e2)
2026-05-08 | M3-02 | fix(M3-02): PasswordResetToken에 TOKEN_TTL 상수 추출 (a766c6a)
2026-05-07 | MVP3 | feat: MVP2 stages 08-14 + MVP3 stages M3-01~M3-13 구현 (3648b3f)
2026-05-07 | MVP3 | refactor(auth): 회원가입 관리자 승인 단계 제거 — 이메일 인증 후 즉시 활성화 (98c4bfc)

## MVP2 — TEAM_LEADER → SALES + 인력표 + 경력 계산기

2026-05-04 | MVP2 | feat: 이메일 발송 구현 — SmtpEmailSender 추가 (d3535ca)
2026-05-04 | MVP2-07 | feat: 경력 계산기 (124903d)
2026-05-04 | MVP2-06 | feat: 양식 이력서 기능 (1ab4eca)
2026-05-04 | MVP2-05 | feat: 인력 표 검색/필터/정렬/컬럼 토글 (c08ebf2)
2026-05-03 | MVP2-04 | feat: 영업부 인력 표 /sales/profiles + employee-documents (500de5e)
2026-05-03 | MVP2-03 | feat: 회원가입 프로필 필드 확장 + 비밀번호 복잡도 정책 (909b715)
2026-04-30 | MVP2-02 | feat: 다운로드 사유 제거 + 관리자 문서 삭제 기능 (7143886)
2026-04-30 | MVP2-01 | fix: Stage 01 완성도 보완 + 보안 린터 도입 — scripts/security-lint.sh (886e2a6)
2026-04-30 | MVP2-01 | feat: TEAM_LEADER → SALES 역할 재구조화 (V100) (243046f)

## MVP1 — 골격부터 13단계 + 배포 산출물

2026-04-29 | MVP1-13 | feat: tags + global document search — /search (9229e13)
2026-04-29 | MVP1-12 | feat: document expiry date management (defa6fc)
2026-04-29 | MVP1-deploy | feat: deployment artifacts + 13-stage test harness (ccd4a32)
2026-04-29 | MVP1-11 | feat: document approval/rejection email notifications (47bb6d4)
2026-04-29 | MVP1-10 | feat: download history + statistics (8a25c9d)
2026-04-29 | MVP1-09 | feat: document approval/review process (3a95b4c)
2026-04-29 | MVP1-08 | feat: thumbnail generation + card view (ae528cf)
2026-04-29 | MVP1-07 | feat: team leader permissions + individual folder access (3826f67)
2026-04-29 | MVP1-06 | feat: preview, download with reason, access control, audit VIEW/DOWNLOAD (38a60dd)
2026-04-28 | MVP1-05 | feat: file upload + FileStorage + versioning (8cfe409)
2026-04-28 | MVP1-04 | feat: admin approval flow + team CRUD + employee management + personal folder auto-creation (7b0e98c)
2026-04-28 | MVP1-03 | feat: signup + email verification + session login (1e0dec0)
2026-04-28 | MVP1-01·02 | feat: project skeleton + DB schema + JPA entities (21fc360)
