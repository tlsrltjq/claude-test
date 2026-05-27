# 시스템 아키텍처

> 본 문서는 "무엇이 어디 있는지" 만 다룬다. "왜" 는 `docs/decisions.md` 참조.

---

## 전체 구성

```
        Browser
          │ HTTPS
          ▼
    ┌────────────┐
    │   Caddy    │  (운영 전용, Let's Encrypt 자동 인증서)
    └─────┬──────┘
          │  http://app:8080
          ▼
  ┌────────────────┐      ┌────────────────────────┐
  │  Spring Boot   │◀────▶│  PostgreSQL 18         │
  │  (Java 21)     │      │  + Flyway V1~V215      │
  └───┬────────┬───┘      └────────────────────────┘
      │        │
      │        └───────▶  Local FS (storage/) or S3/R2 (S3FileStorage)
      │
      └─ HTTP 5001 ──▶ certificate (Python+Flask+LibreOffice, 별도 컨테이너)
```

- 인증: Spring Security 세션, 쿠키 `RESOURCEHUB_SESSION` (HttpOnly + SameSite=Strict, 운영 `Secure=true`)
- 비동기: `@EnableAsync` — 썸네일 생성, GC 등
- 스케줄링: `@EnableScheduling` — 파일 GC `@Scheduled(cron="0 0 2 * * *")`
- 로그: 운영 프로파일에서 logback 롤링 파일 (`/data/logs/resourcehub.log`, 50MB/30일/1GB 상한)

---

## Java 패키지 구조

```
com.eactive.resourcehub
├─ ResourceHubApplication            # @SpringBootApplication + @EnableScheduling + @EnableAsync
├─ HomeController                    # GET / → /dashboard 리다이렉트 등
│
├─ user/                             # 회원·로그인·관리자
│  ├─ controller/                    # AdminController, DashboardController, SignupController,
│  │                                 # SalesController, SalesProfileController, SettingsController,
│  │                                 # ForgotPasswordController, CareerCalculatorController
│  ├─ service/                       # SignupService, PasswordResetService, EmployeeManagementService,
│  │                                 # SettingsService, SalesProfileQueryService, SalesProfileExporter,
│  │                                 # BundleDownloadService, CareerCalculator, ColumnViewPreferenceService,
│  │                                 # AdminInitializer, UserRoleService
│  ├─ service/                       # ...(생략)..., EmailAllowlistService
│  ├─ entity/                        # User, UserRole(ADMIN/SALES/EMPLOYEE/@Deprecated TEAM_LEADER),
│  │                                 # UserStatus, Position(9개 직급 — 사원~상무), PasswordResetToken,
│  │                                 # EmailVerificationToken, ColumnViewPreference, AllowedEmail
│  ├─ repository/                    # *Repository, AllowedEmailRepository
│  └─ dto/                           # SignupRequest, VerifyCodeRequest, SalesProfileQuery
│
├─ document/                         # 문서·폴더·검토·GC·검색·공용폴더
│  ├─ controller/                    # DocumentController, MyFolderController, MyActivityController,
│  │                                 # SearchController, SharedFolderController
│  ├─ service/                       # DocumentUploadService, DocumentDeleteService,
│  │                                 # DocumentAccessService, FolderAccessService,
│  │                                 # DocumentReviewService, DocumentExpiryService,
│  │                                 # DocumentFileGcService (cron 0 0 2 * * *),
│  │                                 # FolderService, MyFolderService, SearchService,
│  │                                 # SharedFolderService, ThumbnailService (@Async)
│  ├─ entity/                        # Document, DocumentVersion, Folder, FolderType,
│  │                                 # DocumentType (RESUME/CAREER_DESCRIPTION/GRADUATION_CERTIFICATE/
│  │                                 # LICENSE/HEALTH_INSURANCE_PROOF/PROFILE_PHOTO/ETC,
│  │                                 # @Deprecated EMPLOYMENT_CERTIFICATE),
│  │                                 # DocumentStatus, DocumentReviewStatus, Tag(미사용)
│  └─ repository/                    # *Repository
│
├─ team/                             # 팀(부서) 관리
│  ├─ controller/                    # AdminTeamController, TeamController
│  ├─ service/                       # TeamService, TeamInitializer
│  ├─ entity/                        # Team (projectTeam 플래그 — V212)
│  └─ repository/                    # TeamRepository
│
├─ permission/                       # 폴더/문서 별 권한
│  ├─ entity/                        # Permission, PermissionTargetType, PermissionType
│  ├─ service/                       # FolderPermissionService
│  └─ repository/                    # PermissionRepository
│
├─ audit/                            # 감사 로그·통계
│  ├─ entity/                        # AuditLog, AuditActionType (LOGIN, UPLOAD, DOWNLOAD,
│  │                                 # DELETE_DOCUMENT, DELETE_DOCUMENT_SELF, RESET_PASSWORD,
│  │                                 # CHANGE_USER_STATUS, EXPORT_PROFILES, ENABLE/DISABLE_USER 등),
│  │                                 # AuditTargetType
│  ├─ service/                       # AuditLogService (REQUIRES_NEW), StatisticsService
│  └─ repository/                    # AuditLogRepository
│
├─ employee/                         # 직원 프로필·경력
│  ├─ entity/                        # EmployeeProfile, AvailableStatus
│  ├─ service/                       # CareerSaveService
│  └─ repository/                    # EmployeeProfileRepository
│
├─ template/                         # 이력서 템플릿
│  ├─ controller/                    # AdminResumeTemplateController, ResumeTemplateDownloadController
│  ├─ entity/                        # ResumeTemplate, ResumeTemplateStatus
│  ├─ service/                       # ResumeTemplateService
│  └─ repository/                    # ResumeTemplateRepository
│
├─ certificate/                      # 재직증명서 (Java → Python Flask 호출)
│  ├─ CertificateController          # /admin/certificate
│  └─ CertificateService             # HttpClient → http://certificate:5001
│
└─ common/                           # 공통 인프라
   ├─ config/JpaAuditingConfig
   ├─ security/                      # SecurityConfig, CustomUserDetails(Service),
   │                                 # LoginSuccessHandler
   ├─ email/                         # EmailSender (Smtp/Console), EmailSenderConfig
   ├─ file/                          # FileStorage, LocalFileStorage, S3FileStorage,
   │                                 # FileStorageConfig
   ├─ entity/BaseEntity              # createdAt, updatedAt, version (JPA Auditing)
   ├─ exception/GlobalExceptionHandler
   ├─ service/AuditService
   └─ util/                          # FileMagicValidator (9개 확장자 magic bytes), FileUtils
```

> 외부 컨테이너 `certificate/` (저장소 루트) — Python 3, Flask 5001, LibreOffice headless. `generate.py` (python-docx + run 분할 대응 치환), `app.py`, `employees.csv`, `Dockerfile`, `requirements.txt`.

---

## 라우트 맵 (도메인별 발췌)

| 영역 | 메서드 | 경로 | 비고 |
|------|--------|------|------|
| 인증 | GET/POST | `/login`, `/login/forgot`, `/login/forgot/verify` | 비번 찾기 5분 TTL |
| 인증 | GET/POST | `/signup`, `/signup/verify`, `/signup/resend` | 이메일 인증 5분 TTL |
| 대시보드 | GET | `/dashboard` | 본인 정보 + 메뉴 그리드 |
| 설정 | GET/POST | `/settings?tab=info|profile|password` | 모든 역할 접근 가능 |
| 내 폴더 | GET/POST | `/my/folder`, `/my/folder/documents/**`, `/my/activity` | 본인 문서 CRUD |
| 공용 폴더 | GET/POST | `/shared/folders/public`, `/shared/folders/public/documents/**` | 전 사원 read, ADMIN write/delete |
| 검색 | GET | `/search` | 본인 권한 모든 문서 + 필터 |
| 영업 | GET/POST | `/sales/members`, `/sales/profiles`, `/sales/profiles/export`, `/sales/profiles/bundle-download`, `/sales/profiles/preset`, `/sales/career-calculator`, `/sales/career-calculator/save`, `/sales/career-calculator/autofill` | SALES + ADMIN |
| 관리자 | GET/POST | `/admin/employees`, `/admin/employees/{id}/toggle-status\|change-team\|change-position`, `/admin/teams`, `/admin/teams/project-settings`, `/admin/user-role`, `/admin/user-permissions`, `/admin/documents/review/**`, `/admin/documents/expiry`, `/admin/statistics`, `/admin/resume-template`, `/admin/gc`, `/admin/gc/run`, `/admin/certificate`, `/admin/allowed-emails`, `/admin/allowed-emails/{id}/delete` | ADMIN |
| 문서 | GET/POST | `/documents/upload`, `/documents/{id}`, `/documents/{id}/delete`, `/documents/{v}/download`, `/documents/{v}/preview`, `/documents/{v}/thumbnail`, `/documents/{v}/thumbnail/regenerate` | 컨트롤러 경유 다운로드만 허용 |

---

## DB 스키마 (Flyway)

| 번호 | 시기 | 핵심 변경 |
|------|------|----------|
| V1 | MVP1 | users, teams, folders, documents, document_versions, permissions, audit_logs |
| V2 | MVP1 | email_verification_tokens |
| V3 | MVP1 | document_versions thumbnail 컬럼 |
| V4 | MVP1 | document_versions review 컬럼 |
| V5 | MVP1 | documents expires_at |
| V6 | MVP1 | tags |
| V100 | MVP2 | TEAM_LEADER → SALES (UserRole) |
| V102 | MVP2 | users 프로필 필드 |
| V103 | MVP2 | EmployeeProfile developer_grade, career |
| V104 | MVP2 | resume_templates |
| V106 | MVP2 | EmployeeProfile career_total_days |
| V200 | MVP3 | Position MANAGING_DIRECTOR(상무) 추가 |
| V201 | MVP3 | password_reset_tokens |
| V202 | MVP3 | DocumentType PROFILE_PHOTO |
| V203 | MVP3 | folders.type + SHARED_PUBLIC enum |
| V204 | MVP3 | career_total_days 백필 |
| V205 | MVP3 | column_view_preferences |
| V206 | MVP3 | documents soft delete |
| V207 | MVP3 | 공용 폴더 시드 |
| V208 | MVP3 | document 메타 (certType, issuedDate, degreeType) |
| V209 | MVP3 | 학위·자격증 시드 |
| V210 | post-MVP3 | documents files_purged_at (GC) |
| V211 | 19-upload | partial unique index (folder_id, document_type, title) |
| V212 | post-21 | teams.project_team |
| V213 | post-21 | 의료보험증명 문서 일괄 삭제 |
| V214 | 기능 개편 | users.address 컬럼 추가 |
| V215 | 기능 개편 | allowed_emails 테이블 신설 (이메일 사전등록 방식) |

> 새 마이그레이션은 V216부터.

---

## 파일 스토리지

- 추상화: `FileStorage` 인터페이스 — `store`, `load`, `delete`, `listAll(Instant)`
- 구현: `LocalFileStorage` (운영 기본 + 환경변수 `RESOURCEHUB_UPLOAD_BASE_DIR`) / `S3FileStorage` (Cloudflare R2, MinIO — `RESOURCEHUB_STORAGE_TYPE=s3`)
- 업로드 검증: `DocumentUploadService.validateFile()` + `FileMagicValidator.validate()` 이중 검사 (9개 확장자 magic bytes)
- 중복 차단: V211 partial unique index + SHA-256 체크섬 (`DocumentVersionRepository.findFirstByChecksumInFolder`)
- 고아 파일 GC: `DocumentFileGcService.runOrphanScan()` — 1시간 이상 된 파일만 (진행 중 업로드 보호), S3 사용 시 no-op

---

## 보안 / 권한 흐름

- 진입: `SecurityConfig.securityFilterChain` — CSRF on, 세션 30분, sessionFixation `changeSessionId`, maximumSessions(-1) + SessionRegistry
- 인증: `CustomUserDetailsService` → `CustomUserDetails` (Spring Security)
- 권한: 모든 파일 접근은 `DocumentAccessService.getVersionWithAccessCheck()` 경유
- 폴더 접근: `FolderAccessService` (역할별 read/write 판정)
- HTTP 헤더: `X-Frame-Options=DENY`, `X-Content-Type-Options=nosniff`, `Referrer-Policy=strict-origin-when-cross-origin`, CSP(`script-src 'self' cdn.jsdelivr.net 'unsafe-inline'` 등)
- 감사: `AuditLogService` (`REQUIRES_NEW`) — UPLOAD/DOWNLOAD/DELETE/RESET_PASSWORD/EXPORT_PROFILES 등

---

## 운영 인프라

- 개발: `docker-compose.yml` (포트 노출, dev override)
- 운영: `docker-compose.prod.yml` — postgres, app(포트 미노출), caddy(HTTPS), certificate
- 배포: `scripts/deploy.sh` (env 검증 + SEED 경고 + 헬스체크), `scripts/setup-cron.sh` (DB 03:00, uploads 03:30)
- 백업: `scripts/backup-db.sh` (pg_dump.gz, 30일 보관), `scripts/backup-uploads.sh` (tar.gz, 30일 보관)
- 도메인: `CADDY_DOMAIN` 환경변수 → `Caddyfile` `{env.CADDY_DOMAIN}`
- forward-headers: `application-prod.yml` `server.forward-headers-strategy: native`
- NAS 전환: `docs/archive/storage-nas-migration.md` (MinIO 또는 NFS)

---

## 정적 분석 도구

- `scripts/security-lint.sh` — 15개 항목 (JWT/Remember-me/CSRF/파일 노출/SQL 인젝션/XSS/UUID/role 직접비교/이메일 트랜잭션/TEAM_LEADER 잔존/ddl-auto/HTTP 헤더/리셋코드 로그/env 하드코딩 등). 0 FAIL 유지.
- `./gradlew build` — 컴파일 + Spring Boot 단위 테스트(`CareerCalculatorTest`).
