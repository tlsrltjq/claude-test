# CHANGELOG

> 구현 완료된 변경 사항을 역순(최신 → 오래된 순)으로 기록.

---

## [Post-MVP3] 2026-05-15 (6차 — 재직증명서 자동 발급)

### 재직증명서 자동 발급 시스템 (하네스 21-certificate)

#### Python CLI + Flask HTTP 서비스 (`certificate/`)
- `generate.py`: `--name`, `--csv`, `--all`, `--create` 인수 지원
  - `_replace_in_doc()`: run 분할 대응 치환 (전체 run 텍스트 합산 후 run[0]에 기록)
  - `{{발급일자}}` PLACEHOLDER → `datetime.today().strftime("%Y년 %m월 %d일")` 치환
  - LibreOffice headless PDF 변환 (`libreoffice --headless --convert-to pdf`)
- `app.py`: Flask 라우트 6개 (`/health`, `/generate`, `/create`, `/templates`, `/files`, `/download/<filename>`)
  - path traversal 방어 (`os.path.basename` + `startswith` 검증)
- `Dockerfile`: `python:3.12-slim` + `libreoffice-writer` + `fonts-noto-cjk`
- `employees.csv`: 이름 컬럼 기반 일괄 발급 지원
- `requirements.txt`: `python-docx==1.1.2`, `flask==3.1.1`

#### Spring Boot 연동 (`certificate/`)
- `CertificateService.java`: Java `HttpClient` 기반 Flask 호출 (추가 의존성 없음)
  - `isAvailable()`, `getTemplates()`, `getFiles()`, `generate()`, `generateAll()`, `createTemplate()`, `download()`
  - `certificate.service.url` 프로퍼티 (기본: `http://certificate:5000`)
- `CertificateController.java`: `@RequestMapping("/admin/certificate")` — 기존 ADMIN 권한 자동 적용
  - `GET /` 목록, `POST /generate` 발급, `POST /create` 템플릿 생성, `GET /download/{filename}` 다운로드
- `templates/admin/certificate.html`: 단건/전체 발급, 템플릿 생성, 파일 목록·다운로드 UI

#### 인프라
- `docker-compose.prod.yml` + `docker-compose.yml`: `certificate` 서비스 추가
  - named volumes: `certificate_employees`, `certificate_output`
  - dev: 포트 `5001:5000` (macOS 5000 포트 충돌 회피)
- 관리자 전체 16개 템플릿 nav에 재직증명서 링크 추가

---

## [Post-MVP3] 2026-05-15 (5차 — 업로드 강화 + 운영 준비)

### 업로드 안전성 강화 (하네스 19-upload-hardening)

#### 1. Magic Bytes 검증 (`FileMagicValidator`)
- `common/util/FileMagicValidator.java` 신규 생성
- 지원 확장자 9개: pdf, jpg/jpeg, png, docx, pptx, hwpx, hwp, ppt
- 파일 서명(매직 바이트) 불일치 시 `IllegalArgumentException` — 확장자 위조 차단

#### 2. 경쟁 조건 방어
- `V211__add_unique_index_documents_folder_type_title.sql`: `(folder_id, document_type, title)` partial unique index (`WHERE status <> 'DELETED'`)
- `DocumentUploadService`: `saveAndFlush()` 후 `DataIntegrityViolationException` 캐치 → 의미 있는 오류 메시지 반환

#### 3. 중복 파일 사전 탐지
- `DocumentVersionRepository.findFirstByChecksumInFolder()`: 동일 폴더·SHA-256 체크섬 중복 조회
- 업로드 전 checksum 계산 → 동일 폴더 내 중복 파일 시 저장 차단

#### 4. 업로드 취소 (XHR abort)
- `my/upload.html`: `<a>` 취소 버튼 → `<button id="cancelBtn">` 전환
- `currentXhr` 변수로 진행 중 XHR 관리, 업로드 중 취소 시 `xhr.abort()` 호출
- `xhr.onabort`: "업로드가 취소됐습니다." 메시지 + 진행바 초기화

#### 5. 고아 파일 GC
- `FileStorage.listAll(Instant olderThan)` 기본 메서드 추가 (S3 no-op)
- `LocalFileStorage.listAll()`: `Files.walk()` 기반 구현 (1시간 이전 파일만)
- `DocumentVersionRepository`: `findAllStoragePaths()`, `findAllPreviewPaths()`, `findAllThumbnailPaths()` 추가
- `DocumentFileGcService.runOrphanScan()`: DB 경로 SET vs 파일시스템 diff → 고아 파일 삭제
- `scheduledGc()` 내 `runOrphanScan()` 자동 실행 (새벽 2시)

---

### 운영 배포 준비 (하네스 20-ops-readiness)

#### 1단계 배포 블로커 완료
- **HTTPS/SSL**: `Caddyfile` + `docker-compose.prod.yml` caddy 서비스 (Let's Encrypt 자동 인증서)
  - 도메인: `CADDY_DOMAIN` 환경변수 주입 (`{env.CADDY_DOMAIN}` 구문)
  - app 서비스 외부 포트 비노출 — Caddy 경유 전용
  - `application-prod.yml`: `server.forward-headers-strategy: native`
- **운영 compose 가드**: `scripts/deploy.sh` 신규 생성
  - 필수 env(`POSTGRES_PASSWORD`, `RESOURCEHUB_ADMIN_PASSWORD`, `CADDY_DOMAIN`) 검증
  - `RESOURCEHUB_SEED_TEST_PASSWORD` 설정 시 경고 + 확인 프롬프트
  - 120초 헬스체크 루프

#### 2단계 권장 사항 완료
- **로그 파일 저장**: `src/main/resources/logback-spring.xml` 신규 생성
  - prod 프로파일: 롤링 파일 appender (50MB 단위, 30일 보관, 1GB 상한)
  - `docker-compose.prod.yml`: `resourcehub_logs` 볼륨 + `LOG_DIR=/data/logs` 주입
- **DB 자동 백업**: `scripts/setup-cron.sh` 신규 생성
  - 매일 03:00 DB 백업 (`backup-db.sh`), 03:30 업로드 백업 (`backup-uploads.sh`)
  - 중복 등록 방지 (`grep -qF` 검사), 서버 1회 실행으로 완료
- **SMTP**: `@ConditionalOnMissingBean` 확인 — 미설정 시 콘솔 폴백 자동 동작
- **Caddy 문서화**: `Caddyfile` 레포 포함, README 배포 섹션 업데이트

---

## [Post-MVP3] 2026-05-08 (3차 — UX 마감)

### UX 개선

#### 관리자 네비바 active 상태 (14개 템플릿)
- `admin/*.html` 전체: `.btn-nav.active { background: rgba(255,255,255,.22); border-color: #fff; }` CSS 추가
- JS: `window.location.pathname` 기준으로 현재 페이지와 일치하는 nav 버튼에 `.active` 자동 적용
  - 루트(`/admin`) — 정확 일치, 그 외 — `startsWith` 매칭

#### 로딩 스피너 (14개 관리자 + 설정 페이지)
- `admin/*.html` 전체 + `settings.html`: 반투명 오버레이 + Bootstrap spinner 추가
- POST 폼 `submit` 이벤트 시 자동 표시 (GET 검색 폼 제외)
- HTML: `position:fixed; inset:0; background:rgba(0,0,0,.45); z-index:9999`

---

## [Post-MVP3] 2026-05-08 (2차 — 아키텍처 개선)

### 아키텍처 개선

#### 1순위 — DB 레벨 필터링 + 페이지네이션
- `EmployeeManagementService.findActiveFilteredPaged()`: Java 스트림 필터 → DB JPQL + `Page<User>` 반환
- `AdminController /employees`: `result.getContent()` + `totalPages` / `currentPage` 사용
- `SalesMemberService.findActiveMembers()`: 전체 로드 후 스트림 → DB `findActiveMembersFiltered()` 호출
- `UserRepository`: `findFilteredPage`, `findActiveMembersFiltered` 2개 JPQL 추가

#### 2순위 — 컨트롤러 Repository 직접 주입 제거
6개 컨트롤러 → 서비스 레이어 경유로 교체 (보안 lint [5] WARN 목록 5→3 감소):

| 컨트롤러 | 제거된 Repository | 사용 서비스 |
|---|---|---|
| `SalesController` | `TeamRepository` | `TeamService.findAll()` |
| `SalesProfileController` | `TeamRepository` | `TeamService.findAll()` |
| `SignupController` | `TeamRepository` | `TeamService.findAll()` |
| `DashboardController` | `UserRepository`, `EmployeeProfileRepository` | `SettingsService.getUser()`, `CareerSaveService.findProfile()` |
| `CareerCalculatorController` | `UserRepository`, `FolderRepository`, `DocumentRepository` | `SalesMemberService.findActiveMembersForCalculator()`, `getMemberAutofillData()` |
| `MyActivityController` | `AuditLogRepository` | `StatisticsService.findUserDownloads()` |

서비스 추가 메서드:
- `CareerSaveService.findProfile(Long userId)` — EmployeeProfile 조회
- `SalesMemberService.findActiveMembersForCalculator()` — 경력계산기용 멤버 목록
- `SalesMemberService.getMemberAutofillData(Long userId)` — 자동채우기 문서 데이터
- `StatisticsService.findUserDownloads(Long userId, int page, int size)` — 내 활동 다운로드 이력

잔여 WARN (기술 부채): `AdminController`, `SharedFolderController`, `MyFolderController`

---

## [Post-MVP3] 2026-05-08 (1차)

### 기능 추가

#### 설정 페이지 (`/settings`)
- 3탭 구성: 계정 정보 / 개인정보 수정 / 비밀번호 변경
- 수정 가능 항목: 연락처(`phone`), 생년월일(`birthDate`), 직급(`position`)
- 현재 비밀번호 확인 후 새 비밀번호 변경 (8자 이상 검증)
- 대시보드 "내 업무" 섹션에 설정 메뉴 버튼 추가
- 구현 파일: `SettingsController`, `SettingsService`, `templates/settings.html`, `User.updateProfile()`

#### 정보처리기사 자격증 종류 단일화
- 업로드 폼(`my/upload.html`): 자격증 종류 select 제거 → `certTypeMeta` hidden input ENGINEER 고정, 취득일 전체 너비
- 경력계산기(`sales/career-calculator.html`): 자격증 종류 select 제거 → "정보처리기사" 체크박스 단일 항목
- 등급 기준 참고표: 산업기사 열 삭제, 열 명칭 "기사" → "정보처리기사"
- autofill 자동입력도 체크박스 방식으로 변경

### 인프라 확인
- Cloudflare R2 외부 스토리지 활성 확인 (`RESOURCEHUB_STORAGE_TYPE=s3` 컨테이너 env 주입 정상)

---

## [Post-MVP3] 2026-05-07 — UI 전면 개선

### 25개 템플릿 전면 리디자인

**디자인 시스템**
- 사용자 페이지: 상단바 `linear-gradient(135deg, #0d6efd 0%, #0a58ca 100%)` 블루
- 관리자 페이지: 상단바 `linear-gradient(135deg, #1e3a5f 0%, #0d2137 100%)` 네이비 + ADMIN 배지
- 테이블 헤더: `#1e3a5f` / 카드: `border-radius:12px`, `box-shadow:0 2px 10px rgba(0,0,0,.07)`

**관리자 패널 (6개)**
- `admin/dashboard.html` — 통계 카드 + 빠른 메뉴 그리드
- `admin/employees.html` — 검색·필터·정렬, 인라인 뱃지
- `admin/teams.html` — 팀별 인원 표시
- `admin/documents-review.html` — 검토 대기 문서 목록
- `admin/documents-expiry.html` — 만료 임박 문서 목록
- `admin/statistics.html` — 시각화 카드

**기타 관리자 서브 페이지 (8개)**
- `admin/team-edit.html`, `admin/user-role.html`, `admin/user-permissions.html`
- `admin/resume-template.html`, `admin/document-review-detail.html`
- `admin/employee-documents.html`, `admin/employee-document-detail.html`, `admin/employee-detail.html`

**사용자·영업·팀 페이지 (5개)**
- `my/document-detail.html`, `sales/member-documents.html`, `sales/employee-documents.html`
- `team/members.html`, `team/member-documents.html`

**인증·에러 페이지 (6개)**
- `login-forgot.html`, `login-forgot-verify.html`, `signup-verify.html`
- `error/403.html`, `error/404.html`, `error/500.html`

**주요 기능 페이지 (별도 커밋)**
- `dashboard.html` — 환영 배너, 프로필 카드, 메뉴 그리드
- `login.html` — 로그인 UI
- `signup.html` — 전화번호·생년월일 자동 포맷
- `sales/members.html`, `sales/profiles.html` — 인력 목록·인력표
- `sales/career-calculator.html` — 경력 계산기 (자동채우기 포함)
- `my/folder.html`, `shared/folders.html`, `shared/folder-documents.html` — 폴더류
- `search.html`, `my/activity.html` — 검색·이력

---

## [MVP3] 2026-05-06 이전

### M3-01 ~ M3-14 전 단계 구현 완료

| 단계 | 내용 |
|------|------|
| M3-01 | Position 직급 네이밍 (상무 추가, 9개 직급), UserRole 한글 displayName |
| M3-02 | /login 이메일 기억하기 체크박스 (cookie 30일) |
| M3-03 | /login/forgot 비밀번호 찾기 (이메일 → 코드 → 임시 비밀번호) |
| M3-04 | /signup/resend 5분 타이머, 이메일 인증 유효시간 5분 |
| M3-05 | Dashboard 내 정보 보강 (생년월일, 연락처, 개발자 등급) |
| M3-06 | DocumentType 정비: EMPLOYMENT_CERTIFICATE @Deprecated, PROFILE_PHOTO 추가, LICENSE → 정보처리기사 |
| M3-07 | 태그 기능 전면 제거 |
| M3-08 | /search 통합 검색 (진입 시 전체 문서 표시 + 필터) |
| M3-09 | /shared/folders/public 전 사원 공용 폴더 |
| M3-10 | /sales/members 정렬 (직급/팀/역할) |
| M3-11 | /sales/profiles 컬럼 정리, 등급 위젯, 경력 표시 3가지, 사용자 프리셋, 체크 선택 엑셀 |
| M3-12 | /sales/career-calculator 검색 동작 복구 |
| M3-13 | 관리자 직원 목록 검색·필터, 계정 활성/비활성 토글 |
| M3-14 | 문서 메타 필드 (certType, issuedDate, degreeType), 경력계산기 자동채우기 |

### 보안 개선 (모두 security-lint.sh 15/15 PASS)

| 항목 | 내용 |
|------|------|
| [13] HTTP 보안 헤더 | SecurityConfig에 X-Frame-Options, X-Content-Type-Options, Referrer-Policy, CSP 추가 |
| [14] 비밀번호 재설정 코드 로그 제거 | PasswordResetService에서 `code={}` 로그 패턴 제거 |
| [15] 환경변수 기본값 제거 | application.yml에서 `:Admin1234!`, `:resourcehub` 등 하드코딩 제거 |

### 스토리지

- Cloudflare R2 (S3 호환): `RESOURCEHUB_STORAGE_TYPE=s3`, AWS SDK v2 기반 `S3FileStorage`
- 로컬 폴백: `LocalFileStorage` (`RESOURCEHUB_STORAGE_TYPE=local` 또는 미설정 시)

---

## [MVP2] ~2026-05-05

- SALES 역할, /sales/** 라우팅
- 인력표, 경력 계산기, 엑셀 내보내기
- 공유 폴더, 폴더 권한, 문서 삭제(관리자)
- Flyway V100~V207 마이그레이션

## [MVP1] ~2026-05-01

- 회원가입/이메일 인증/로그인
- 내 폴더, 문서 업로드/다운로드/미리보기/썸네일
- 팀 관리, 권한 관리
- 감사 로그
- Flyway V1~V6 마이그레이션
