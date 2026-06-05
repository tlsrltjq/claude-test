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
- 번호 규칙: V1–V6=MVP1, V100~=MVP2, V200~=MVP3·post-MVP3. 새 변경은 V230부터.
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

## ADR-037: 투입 중복은 경고만, 차단 안 함
- 결정: 같은 직원이 겹치는 기간에 여러 프로젝트에 배정되는 것을 DB 레벨에서 차단하지 않는다. 겹치는 배정이 감지되면 `overlapWarning` flash 속성으로 경고 메시지만 표시하고 저장은 허용한다.
- 이유: 분할 투입(A 프로젝트 50% + B 프로젝트 50%) 이 현실에서 발생함. 100% 투입률 합계 초과를 DB 제약으로 막으면 정상 업무 흐름을 차단하게 됨. 책임은 등록자(ADMIN)에게.
- 트레이드오프: 투입률 합계 검증 없음 — 200% 투입도 저장 가능. 향후 합계 경고 추가 가능.
- 재검토 조건: 인력 자원 관리(HR) 시스템 연동 또는 투입률 합계 100% 초과가 운영 이슈가 될 때.

## ADR-038: CalendarGridBuilder는 package-private 클래스로 분리
- 결정: `ProjectAssignmentController` 의 캘린더 그리드 빌드 로직(`buildCalendarWeeks`, `buildDayMap`)을 같은 패키지 내 별도 클래스 `CalendarGridBuilder`(package-private) 로 추출한다.
- 이유: 정적 private 메서드는 컨트롤러를 `@WebMvcTest` 로 올려야만 테스트 가능. 순수 로직(`buildWeeks`, `buildDayMap`)은 Spring 컨텍스트 없이 검증돼야 한다. package-private 분리로 `src/test/java` 동일 패키지에서 직접 접근 가능.
- 트레이드오프: 컨트롤러 파일이 하나 늘어남. public 도우미 클래스가 아니므로 외부 패키지 접근 불가.
- 적용 범위: 주 1회 빌드 실패 방지보다 테스트 가능성(testability) 이 높은 가치임. 향후 캘린더 로직 변경 시 `CalendarGridBuilderTest` 먼저 수정.

## ADR-044: SampleDataFixRunner @Profile("!prod") 격리
- 결정: `SampleDataFixRunner` 에 `@Profile("!prod")` 를 추가해 prod 프로파일에서는 빈 자체가 등록되지 않도록 한다.
- 이유: 앱 재시작 시마다 `demo/*` 경로 DB 쿼리·파일 업로드가 실행됨. 운영 환경에는 demo 데이터가 없어 즉시 종료되더라도 불필요한 코드가 매번 실행되는 것은 안전하지 않음.
- 트레이드오프: local/dev/test 환경에서는 여전히 실행됨. prod 배포 시 `spring.profiles.active=prod` 가 반드시 설정되어야 함.

## ADR-043: 엑셀 업로드 확장자·매직바이트·행수 삼중 검증
- 결정: `/admin/allowed-emails/bulk-excel` 의 `addBulkFromExcel()` 에서 (1) 확장자 xlsx/xls 검사, (2) `FileMagicValidator` 매직바이트 검증, (3) 1,000행 초과 시 파싱 중단.
- 이유: Apache POI `WorkbookFactory.create()` 는 파일 내용을 검증 없이 파싱하려 시도. ZIP Bomb(압축 폭탄) XLSX 파일이 올라오면 메모리 고갈 가능. 관리자 전용 기능이지만 내부 위협도 대비해야 함.
- 트레이드오프: xlsx/xls 만 허용. csv 방식은 텍스트 일괄 입력(`addBulk`)으로 대체.

## ADR-042: CSP script-src nonce 방식 도입, unsafe-inline 제거
- 결정: `CspNonceFilter`(요청마다 16바이트 nonce 생성) + `CspNonceHeaderWriter`(동적 CSP 헤더) + `CspNonceInterceptor`(Thymeleaf 모델 주입) 3-레이어 구조. 모든 인라인 `<script>` 태그에 `th:attr="nonce=${cspNonce}"` 추가. style-src `unsafe-inline` 은 인라인 style 속성 558개로 인해 유지.
- 이유: XSS 취약점이 발생하더라도 nonce를 모르는 공격자의 인라인 스크립트가 실행되지 않음. 기존 `unsafe-inline`은 이 2차 방어선을 완전히 무력화.
- 트레이드오프: 새 인라인 `<script>` 추가 시 nonce 속성 누락하면 스크립트가 차단됨. security-lint [19]가 이를 감지.

## ADR-041: 로그인 브루트포스 — 잠금 없이 비밀번호 재설정 유도
- 결정: 10회 실패 시 계정 잠금(15분 대기) 대신 카운터를 리셋하고 `/login/forgot?toomany` 로 리다이렉트. `LoginAttemptService` 인메모리, `LoginFailureHandler` + `LoginSuccessHandler` 연동. 성공 로그인 시 카운터 즉시 초기화.
- 이유: 계정 잠금은 공격자가 의도적으로 타인 계정을 잠글 수 있는 DoS 공격 벡터가 됨. 비밀번호 재설정 유도는 합법적 사용자 접근성을 유지하면서 브루트포스 실효성을 낮춤. 인메모리 저장이라 서버 재시작 시 카운터 리셋되지만 사내 포털 규모에서는 수용 가능.
- 트레이드오프: 서버 재시작 시 카운터 초기화. 다중 서버 환경에서는 Redis 기반으로 교체 필요.

## ADR-040: 인증코드 5회 실패 시 즉시 무효화
- 결정: 비밀번호 재설정(`POST /login/forgot/verify`) 및 회원가입 이메일 인증(`POST /signup/verify`) 모두 5회 코드 불일치 시 토큰/세션을 즉시 무효화하고 처음부터 재시작 요구. 남은 시도 횟수 안내.
- 이유: 6자리 숫자 코드(100만 가지)를 5분/10분 내에 브루트포스 가능. 시도 횟수 제한 없이 자동화 스크립트로 계정 탈취 가능.
- 트레이드오프: 정당한 사용자가 실수로 5회 틀리면 재발송 필요. 회원가입의 경우 `?toomany` 리다이렉트로 UX 안내.

## ADR-039: 계정 삭제 시 프로젝트 배정 이름 보존 (SET NULL + 스냅샷)
- 결정: 직원 계정 삭제 시 `project_assignments.user_id` 는 CASCADE DELETE 가 아닌 SET NULL 처리. 삭제 전 `user_name` 컬럼에 이름 스냅샷을 저장. `ProjectAssignment.getDisplayName()` 은 `user != null` 이면 `user.getName()`, null 이면 `userName` 스냅샷, 그것도 없으면 "(삭제된 계정)" 반환.
- 이유: 프로젝트 인력 투입 이력은 계정 삭제 후에도 보존 가치가 있음. 고객사·경영진이 과거 프로젝트 인력 구성을 조회할 때 "(삭제된 계정)" 만 표시되면 의미 없음. 이름 스냅샷으로 이력 가독성 유지. CASCADE 삭제 시 캘린더·상세 페이지의 배정 행이 사라져 데이터 손실이 발생.
- 구현: V226 마이그레이션 (`ADD COLUMN user_name`, `UPDATE … SET user_name = name`, `ALTER COLUMN user_id DROP NOT NULL`, FK ON DELETE SET NULL). `ProjectAssignment.createForProject()` 에서 생성 시 `pa.userName = user.getName()` 로 즉시 스냅샷.
- 영향: V224 에서 먼저 정비한 나머지 FK 6개(email_verification_tokens·password_reset_tokens·column_view_preferences CASCADE, resume_templates·document_versions.reviewed_by·documents.deleted_by SET NULL) 도 같이 수정.
- 트레이드오프: 이름 변경 후 삭제 시 스냅샷이 구 이름을 유지 (최근 업데이트된 이름이 아닐 수 있음). 스냅샷 재동기화는 미구현.

## ADR-045: 문서 만료 알림은 "임박 1회 + 만료 1회"만 발송 (발송 이력 컬럼)
- 결정: 매일 09:00 cron(`DocumentExpiryService.sendExpiryNotifications`)이 문서당 만료 임박 알림 1회(만료 30일 전)와 만료 알림 1회만 발송한다. `documents.expiry_warn_sent_at`/`expired_notice_sent_at`(V230) 에 발송 시각을 기록하고, 알림 전용 쿼리(`findExpiringSoonNeedingWarn`/`findExpiredNeedingNotice`)가 이력 NULL인 문서만 조회한다.
- 이유: 기존 구현은 30일 임박 구간·만료 후 내내 매일 동일 메일을 재발송해 사용자 받은편지함을 채웠다. 발송 이력 컬럼으로 정확히 2회만 보내 알림 피로를 제거.
- 구현: 발송 성공 직후에만 `markExpiryWarnSent`/`markExpiredNoticeSent` 호출(실패 시 미기록 → 다음 실행 재시도). 작업 트랜잭션을 `@Transactional`(쓰기)로 변경해 dirty-checking 으로 플러시. 만료일 변경(`updateExpiresAt`) 시 두 이력을 NULL로 초기화해 새 만료 주기에 재알림.
- 관리자 만료 현황 화면용 `findExpired`/`findExpiringSoon` 은 이력과 무관하게 전체를 보여줘야 하므로 그대로 두고, 알림 전용 쿼리를 별도 추가했다.
- 배포 백필: V230에서 이미 임박 구간(만료 30일 이내)·만료된 ACTIVE 문서의 이력을 `now()`로 채워 배포 직후 대량 재발송을 방지(구버전에서 이미 매일 받던 문서들).
- 트레이드오프: 임박 알림이 단 1회뿐이라 사용자가 놓치면 만료 시점까지 추가 리마인더가 없음(만료 알림이 보조). 향후 D-7 추가 알림이 필요하면 별도 컬럼·쿼리로 확장.

## ADR-046: 파일 GC 정기 실행을 별도 스케줄러 빈으로 분리 (self-invocation 트랜잭션 우회 해소)
- 결정: `DocumentFileGcService.scheduledGc()`(`@Scheduled`, 비트랜잭션)가 같은 빈의 `runGc()`/`runOrphanScan()`(`@Transactional`)을 직접 호출하던 구조를 폐기하고, 정기 실행 진입점을 새 빈 `DocumentFileGcScheduler`로 분리한다. 스케줄러는 `DocumentFileGcService`를 주입받아 호출한다.
- 이유: 같은 빈 내부의 self-invocation 은 Spring AOP 프록시를 우회하므로 cron 경로에서 `@Transactional` 어드바이스가 적용되지 않았다. 관리자 수동 실행(`AdminController` → 프록시 경유)과 cron 경로의 트랜잭션 경계가 달라지는 비일관성이 있었다. 별도 빈에서 호출하면 프록시를 타 트랜잭션이 정상 적용된다.
- 구현: `runGc`/`runOrphanScan` 시그니처·로직은 그대로 유지(AdminController·테스트 호출부 무변경). 스케줄러에 GC·고아스캔 독립 try-catch + `log.error` 가드를 둬 한쪽 실패가 다른 쪽을 막지 않게 했다. 기존 스케줄러 관례(`ProjectStatusScheduler`)와 동일한 형태.
- 트레이드오프: 클래스가 하나 늘어남. 대신 트랜잭션 경계가 경로와 무관하게 일관되고, 스케줄링 책임과 GC 로직 책임이 분리된다.

## ADR-035: 옛 stage별 verify.sh 하네스 폐기 → 범용 양식
- 결정: MVP1~MVP3·post-MVP3·19~21 단계별 `harness/*/verify.sh` + `progress.json` 방식을 폐기. 대신 `CLAUDE.md` + `HARNESS.md` + `tasks/current.md` + `docs/architecture.md` + `docs/decisions.md` + `CHANGELOG.md` 범용 양식 사용. 옛 자산은 `harness/archive/legacy/` 보존.
- 이유: 121개 커밋 누적 후 단계별 verify 가 코드 변경을 따라가지 못해 false positive·outdated 메타데이터(progress.json) 가 누적. 검증은 `scripts/security-lint.sh` + `./gradlew build` 로 충분.
- 트레이드오프: 회귀 검사를 자동으로 돌릴 수 없음 — 옛 verify.sh 는 `harness/archive/legacy/` 에서 꺼내 쓰면 됨.
- 재검토 조건: 자동 회귀 검사가 다시 필요할 때(예: CI 도입).
