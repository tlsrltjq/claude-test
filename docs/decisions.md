# 기술 결정 (ADR)

> "왜 이렇게 했는지" 를 기록한다. 에이전트가 나중에 같은 결정을 뒤집지 않도록.
> 추가 시 번호 증가. 폐기된 결정은 삭제하지 말고 `상태: 폐기` 로 표시.

---

## ADR-001: 인증은 Spring Security 세션, JWT 금지
- 결정: Spring Security 세션 기반 인증, `RESOURCEHUB_SESSION` 쿠키(HttpOnly + SameSite=Strict, 운영 Secure=true), 세션 타임아웃 30분.
- 이유: 사내 포털이라 도메인 단일, 모바일 앱 없음. 토큰 관리 비용·블랙리스트 인프라 불필요. 세션 즉시 만료(역할 변경·비활성화) 가 핵심 요구사항.
- 트레이드오프: 외부 API/모바일 클라이언트 도입 시 재설계 필요.
- 재검토 조건: 외부 시스템 SSO/OAuth 연동 또는 모바일 클라이언트 추가.
- 정적 검사: `scripts/security-lint.sh [1] JWT 미사용`.

## ADR-002: Remember-me 금지
- 결정: Spring Security `rememberMe` 사용 금지. 단, 로그인 화면 "이메일 기억하기" 는 별도 쿠키(`RESOURCEHUB_LAST_EMAIL`) 로 평문 이메일만 저장 — 비밀번호·토큰 미저장.
- 이유: 30일 토큰을 평문 쿠키에 박는 방식의 위험 회피. UX 만 위해 이메일 자동 채움이면 충분.
- 트레이드오프: 비번 자동 로그인 불가.
- 정적 검사: `scripts/security-lint.sh [2] Remember-Me 미사용`.

## ADR-003: CSRF 항상 활성화
- 결정: `.csrf().disable()` 금지. 모든 POST 폼에 `_csrf` 히든 필드.
- 이유: 사내 포털도 세션 탈취·CSRF 표적이 됨. 비활성화의 편의보다 위험이 큼.
- 정적 검사: `scripts/security-lint.sh [3] CSRF 활성화`.

## ADR-004: 파일은 컨트롤러 경유, 정적 노출 금지
- 결정: 업로드된 파일은 `resources/static/` 또는 `addResourceHandlers` 로 직접 노출 금지. 모든 다운로드·미리보기·썸네일은 `DocumentController` + `DocumentAccessService` 경유.
- 이유: URL 추측만으로 권한 없는 사용자가 접근하는 사고 방지. 모든 접근에 감사 로그 기록 가능.
- 트레이드오프: 정적 캐시 효율 낮음. 트래픽 증가 시 캐시·CDN 별도 설계 필요.
- 정적 검사: `scripts/security-lint.sh [4]`.

## ADR-005: 저장 파일명은 UUID, 원본명은 DB
- 결정: 디스크/S3 저장 경로는 `UUID.ext`. 원본 파일명·MIME·체크섬은 `document_versions` 에 저장.
- 이유: 경로 추측 방지, 파일명 충돌 방지, 한글 파일명·특수문자 안전성.
- 정적 검사: `scripts/security-lint.sh [8]`.

## ADR-006: 권한 검사는 Service 레이어
- 결정: 컨트롤러에서 `user.getRole()` 직접 비교 금지. `DocumentAccessService` / `FolderAccessService` / `FolderPermissionService` 경유.
- 이유: 권한 규칙이 컨트롤러 곳곳에 분산되면 한 군데 누락으로 우회 가능. Service 한 곳에서 enforce.
- 정적 검사: `scripts/security-lint.sh [9]`. 컨트롤러 직접 Repository 주입도 `[5]` 에서 차단.

## ADR-007: 스키마 변경은 Flyway 만, `ddl-auto: validate`
- 결정: JPA 엔티티 변경 시 무조건 Flyway 마이그레이션 추가. `ddl-auto` 는 항상 `validate` (운영도 동일).
- 이유: Hibernate auto-DDL 의 실수로 인한 운영 데이터 손실 방지. 마이그레이션은 코드 리뷰 대상.
- 번호 규칙: V1–V6=MVP1, V100~=MVP2, V200~=MVP3·post-MVP3. 새 변경은 V216 이후.
- 정적 검사: `scripts/security-lint.sh [12]`.

## ADR-008: 감사 로그는 REQUIRES_NEW
- 결정: `AuditLogService` 의 기록 메서드는 `@Transactional(propagation = REQUIRES_NEW)`.
- 이유: 본 트랜잭션이 롤백되더라도 감사 로그는 남아야 함(인증 실패·삭제 시도 등 보안 이벤트 추적).

## ADR-009: 역할은 ADMIN / SALES / EMPLOYEE, TEAM_LEADER 폐기
- 결정: `UserRole` enum 3개. `TEAM_LEADER` 는 `@Deprecated`, 신규 부여 차단(V100 에서 SALES 로 일괄 이관). 화면 표기는 한글(관리자/영업/사원), enum 은 영문.
- 이유: 영업부가 전사 인력 표를 봐야 하는 비즈니스 요구. 팀장 권한은 모호해서 제거.
- 트레이드오프: 팀 단위 권한이 필요해지면 다시 도입 검토.

## ADR-010: 이메일 발송은 try/catch 로 트랜잭션 격리
- 결정: `@Transactional` 메서드(또는 클래스 레벨 `@Transactional`) 내 `EmailSender` 호출은 try/catch 필수. SMTP 미설정 시 `ConsoleEmailSender` 폴백.
- 이유: 메일 서버 일시 장애로 회원가입·비번 재설정 전체 롤백되는 사고 방지.
- 정적 검사: `scripts/security-lint.sh [10]` + `scripts/lint/check_email_transactional.py` — 메서드 단위 정밀 검사. enclosing method (또는 클래스) 가 `@Transactional` 이고 `emailSender.xxx()` 호출이 같은 메서드 안의 `try { ... }` 블록 밖에 있으면 FAIL.

## ADR-011: 환경변수 하드코딩 기본값 금지(시크릿 한정)
- 결정: `application.yml` 의 `*PASSWORD`, `*SECRET`, `*KEY`, `*TOKEN` 변수에 기본값 `:something` 금지. 미설정 시 앱 기동 실패(명시적 오류 의도).
- 이유: 운영에 기본 비밀번호로 떴다가 사고 난 사례 회피.
- 정적 검사: `scripts/security-lint.sh [15]`.

## ADR-012: 비밀번호 재설정 코드 로그 출력 금지
- 결정: `log.info("... code={}", code)` 패턴 금지. 이메일 또는 콘솔(`ConsoleEmailSender`) 만으로 전달.
- 이유: 운영 로그 유출 시 임시 비밀번호 누출.
- 정적 검사: `scripts/security-lint.sh [14]`.

## ADR-013: HTTP 보안 헤더 기본 적용
- 결정: `SecurityConfig.headers` 에 `X-Frame-Options=DENY`, `X-Content-Type-Options=nosniff`, `Referrer-Policy=strict-origin-when-cross-origin`, CSP 적용. HSTS 는 운영(`server.servlet.session.cookie.secure=true`) 전환 시 함께 켠다.
- 이유: 기본값으로 흔한 클릭재킹·MIME 스니핑·정보 누출 방어.
- 정적 검사: `scripts/security-lint.sh [13]`.

## ADR-014: XSS — Thymeleaf `th:utext` / `th:inline=javascript` 금지
- 결정: 모든 동적 값은 `th:text` 또는 `data-*` 속성으로. 마크다운 등 HTML 출력이 필요하면 별도 sanitizer 도입 후 검토.
- 정적 검사: `scripts/security-lint.sh [7]`.

## ADR-015: 업로드 파일 이중 검증 (확장자 + magic bytes)
- 결정: `DocumentUploadService.validateFile()` (확장자 화이트리스트) + `FileMagicValidator.validate()` (9개 확장자 magic bytes) 두 단계.
- 이유: 확장자 위조(`.jpg` 가 실제로 PHP)를 차단. ppt/pptx 추가 시에도 동일 정책.
- 허용 확장자: `pdf, jpg, jpeg, png, docx, hwp, hwpx, ppt, pptx`.

## ADR-016: 활성 문서 중복 차단 — partial unique index + SHA-256
- 결정: V211 `(folder_id, document_type, title) WHERE status <> 'DELETED'` partial unique index + 업로드 전 SHA-256 체크섬 사전 탐색.
- 이유: 동일 폴더에 같은 이름·타입의 활성 문서 중복 방지. 경쟁 조건은 DB 레벨 `DataIntegrityViolationException` 으로 방어.

## ADR-017: 소프트 삭제 + 지연 GC
- 결정: 문서 삭제는 `deleted_at` / `deleted_by` (V206). 물리 파일은 `DocumentFileGcService.@Scheduled(cron="0 0 2 * * *")` 가 보존일(`RESOURCEHUB_GC_RETENTION_DAYS`, 기본 7) 경과 후 삭제. 고아 파일은 `runOrphanScan()` 으로 별도 정리.
- 이유: 사용자 실수 복구·감사 추적 + 운영 디스크 누적 방지.

## ADR-018: 썸네일은 `@Async`, `@EnableAsync`
- 결정: `ThumbnailService.generateAndSave()` 는 `@Async` 로 분리, 업로드 응답 지연 없음. 트랜잭션 전파 주의(`REQUIRES_NEW` 필요 시 명시).
- 이유: PDFBox 렌더링이 수 초 걸리는 경우가 있어 업로드 UX 저하.

## ADR-019: 운영 HTTPS 는 Caddy, app 포트 비노출
- 결정: Caddy 2 (`caddy:2-alpine`, Let's Encrypt) 를 리버스 프록시로. `docker-compose.prod.yml` 에서 app 외부 포트 미노출, 내부 backend 네트워크만. `application-prod.yml: server.forward-headers-strategy=native`.
- 이유: HTTPS 인증서 자동 갱신·서비스 분리.
- 도메인: `CADDY_DOMAIN` 환경변수.

## ADR-020: 재직증명서는 별도 Python 서비스
- 결정: 재직증명서 생성은 별도 Docker 컨테이너 `certificate/` (Python 3 + Flask 5001 + LibreOffice headless + python-docx). Spring 측은 `CertificateService` 가 HttpClient 로 호출.
- 이유: Java 진영 docx → pdf 변환 라이브러리(Apache POI + Aspose 등) 가 폐쇄·고비용. LibreOffice headless 가 가장 안정.
- 트레이드오프: 운영 컨테이너 1개 추가, 빌드 시간 증가.

## ADR-021: 스토리지 추상화 — Local / S3 호환
- 결정: `FileStorage` 인터페이스 + `LocalFileStorage` / `S3FileStorage` (AWS SDK v2). 환경변수 `RESOURCEHUB_STORAGE_TYPE` 으로 전환.
- 이유: 시놀로지 NAS(MinIO) 또는 Cloudflare R2 모두 같은 코드로 대응(`docs/archive/storage-nas-migration.md`).

## ADR-022: 컨트롤러는 얇게, 로직은 Service 로
- 결정: 모든 컨트롤러(`*/controller/*.java`)에서 `private final XxxRepository` 직접 주입 금지. 8da9eaa 커밋(`refactor: 컨트롤러 Repository 직접 주입 제거`) 이후 면제 목록 폐기, 전 컨트롤러 균등 FAIL.
- 이유: 권한 검사·트랜잭션 경계·테스트 가능성. Service 레이어에서만 DB 접근 일원화.
- 정적 검사: `scripts/security-lint.sh [5]`.

## ADR-023: 정적 분석 0 FAIL 유지
- 결정: 모든 커밋 전 `bash scripts/security-lint.sh` 통과. 15개 항목 0 FAIL.
- 이유: CI 미구축 단계에서도 최소한의 안전망.

## ADR-024: 회원가입은 이메일 허용 목록 확인 + 이메일 인증 후 즉시 활성화 (관리자 승인 제거)
- 결정: MVP1 의 "관리자 승인 후 활성화" 단계를 폐기. 대신 `allowed_emails` 테이블에 사전 등록된 이메일만 가입 허용. 이메일 인증 통과 시 `UserStatus.ACTIVE` 즉시 전환. (commit `98c4bfc`, 기능 개편)
- 이유: 이메일 인증만으로는 임의 주소 가입 차단 불가. 허용 목록을 관리자가 관리함으로써 신원 확인 책임을 명확히 함. 승인 대기열 운영 부담 제거.
- 트레이드오프: 관리자가 이메일을 사전 등록해야 하는 절차 추가.
- 재검토 조건: 사외 사용자 가입을 받게 될 때.

## ADR-025: 비밀번호 복잡도 정책
- 결정: 영문 + 숫자 + 특수문자 포함 8자 이상. 비밀번호 변경·재설정·가입 폼 모두 동일 검증. (commit `909b715`)
- 이유: 평문 노출이 없어도 사전 공격·brute force 대비 최소선.
- 정적 검사: 별도 검사기는 없음 (서버측 `@Pattern` + 클라이언트측 JS).

## ADR-026: 다운로드 사유 입력 폐기
- 결정: MVP1 의 다운로드 시 사유 입력 모달 제거. 사유 컬럼은 `audit_logs` 에 남기되 nullable. (commit `7143886`)
- 이유: 실제 영업·관리자 사용 흐름에서 매번 입력하는 마찰이 크고, 결국 "확인" 같은 무의미 사유만 누적됨. 본 로그만으로 누가·언제·무엇을 받았는지 추적 충분.

## ADR-027: 인력표 엑셀 — 문서 보유 컬럼은 파일명이 아닌 O/X
- 결정: `/sales/profiles` 의 엑셀 내보내기에서 각 문서 유형(이력서·경력기술서·졸업·자격·증명사진 등) 컬럼은 파일명 대신 O/X 마크. (commit `d6276bb`)
- 이유: 파일명 자체에 개인 식별 정보(이름/생년월일)가 들어있어 엑셀 공유 시 노출 위험. O/X 만으로 영업 의사결정에 충분.
- 트레이드오프: 어떤 버전의 파일인지 추적 불가 — 필요 시 ZIP 묶음 다운로드(`/sales/profiles/bundle-download`) 사용.

## ADR-028: 직급(`position`) 변경은 ADMIN 전용
- 결정: `/settings/profile` 의 개인정보 수정에서 직급은 본인 변경 불가. ADMIN 만 `/admin/employees/{id}/change-position` 으로 변경 가능. (commit `3a93907`)
- 이유: 직급은 인사 권한이고 본인 자가 변경 시 권한 우회·이력 왜곡 가능.
- 화면: 본인 화면에서는 직급 select 자체 비표시 또는 disabled.

## ADR-029: 역할/상태 변경 시 대상 사용자 세션 즉시 만료
- 결정: 관리자가 다른 사용자의 `role` 또는 `status` 를 변경하면 `SessionRegistry` 로 해당 사용자의 모든 세션을 즉시 만료. (commit `93bb648`)
- 이유: 권한 강등 후에도 기존 세션이 살아있으면 다음 요청까지 옛 권한 유지 — 보안 사고.
- 구현: `SecurityConfig.maximumSessions(-1).sessionRegistry(sessionRegistry())` + `EmployeeManagementService.toggleStatus()` 가 SessionRegistry 에서 expireNow 호출.

## ADR-030: 공용 폴더(/shared/folders/public) 권한 — 전 사원 read·업로드, 본인·ADMIN 삭제
- 결정: `FolderType.SHARED_PUBLIC` (V203) 의 공용 폴더는 EMPLOYEE 포함 모든 인증 사용자가 read 및 업로드 가능. 삭제는 업로더 본인 또는 ADMIN만 가능. (기능 개편)
- 이유: 사내 공통 양식·안내문 배포뿐 아니라 직원 간 자료 공유 용도로도 활용하기 위함. ADMIN 전용 업로드는 불필요한 제약.

## ADR-031: 자격증 종류는 "정보처리기사" 단일 고정
- 결정: `LICENSE` 문서 타입의 `certTypeMeta` 는 `ENGINEER` 한 값만 사용. 산업기사(`INDUSTRIAL_ENGINEER`)는 폐기. (commit `d7afe13`)
- 이유: 실제 영업 의사결정에 산업기사 구분이 사용되지 않음. UI 마찰 감소.
- 영향: 업로드 폼은 hidden input 으로 `ENGINEER` 고정, 경력 계산기는 체크박스 "정보처리기사" 단일 항목, 등급표 컬럼도 단일화.

## ADR-032: 영업부(SALES) 는 전사 인력 read-only
- 결정: SALES 역할은 `/sales/profiles`, `/sales/members`, `/sales/career-calculator` 에서 전사 직원의 메타데이터·문서를 조회·다운로드 가능. 수정·삭제 불가.
- 이유: 영업 인력 매칭·외부 제안서 발송이 핵심 업무라 전사 인력 가시성이 필요.
- 분기 위치: `DocumentAccessService` / `FolderAccessService` 의 SALES 분기.

## ADR-033: 이력서 양식 다운로드는 미리 등록한 단일 활성 템플릿
- 결정: 관리자가 `/admin/resume-template` 에서 다중 등록 가능, 그러나 `ResumeTemplateStatus.ACTIVE` 한 건만 사용자에게 노출. (mvp2 `1ab4eca`)
- 이유: 화면에 여러 양식이 노출되면 사용자가 무엇을 써야 할지 혼선.

## ADR-034: 만료(expiration) 관리는 documents.expires_at 단일 컬럼
- 결정: 문서 만료일은 `documents.expires_at` (V5) 한 컬럼. 만료 알림은 `/admin/documents/expiry` 대시보드에서만 표시(이메일 알림 X).
- 이유: 비건강보험·자격증 갱신 추적 용도 — 이메일 푸시까지는 운영 비용 대비 이득 없음.
- 재검토 조건: 대상 사용자 수가 100명을 넘고 만료 임박 알림이 운영 이슈가 될 때.

## ADR-036: 가입 허용 이메일 사전등록 방식 도입
- 결정: `allowed_emails` 테이블(V215)을 신설하고, 회원가입 시 입력된 이메일이 목록에 없으면 가입 차단. 관리자가 `/admin/allowed-emails`에서 허용 이메일 추가·삭제.
- 이유: 도메인 고정 방식(`RESOURCEHUB_COMPANY_EMAIL_DOMAIN`) 은 같은 도메인을 쓰는 계약직·외부 인원 제어가 불가. 허용 목록 방식은 개인 단위로 정밀 제어 가능.
- 트레이드오프: 직원 추가 시 이메일 사전등록 절차 추가. 기존 환경변수(`RESOURCEHUB_COMPANY_EMAIL_DOMAIN`) 제거.
- 정적 검사: 없음 (DB 레벨 제어).

## ADR-035: 옛 stage별 verify.sh 하네스 폐기 → 범용 양식
- 결정: MVP1~MVP3·post-MVP3·19~21 단계별 `harness/*/verify.sh` + `progress.json` 방식을 폐기. 대신 `CLAUDE.md` + `HARNESS.md` + `tasks/current.md` + `docs/architecture.md` + `docs/decisions.md` + `CHANGELOG.md` 범용 양식 사용. 옛 자산은 `harness/archive/legacy/` 보존.
- 이유: 121개 커밋 누적 후 단계별 verify 가 코드 변경을 따라가지 못해 false positive·outdated 메타데이터(progress.json) 가 누적. 검증은 `scripts/security-lint.sh` + `./gradlew build` 로 충분.
- 트레이드오프: 회귀 검사를 자동으로 돌릴 수 없음 — 옛 verify.sh 는 `harness/archive/legacy/` 에서 꺼내 쓰면 됨.
- 재검토 조건: 자동 회귀 검사가 다시 필요할 때(예: CI 도입).
