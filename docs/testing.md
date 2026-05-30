# 테스트 전략

> 현재 테스트 범위, 도구, 실행 방법, 테스트하기 어려운 영역 기록.
> 참고 소스: `src/test/`, `build.gradle`, `scripts/security-lint.sh`

---

## 현재 테스트 범위

### 자동화 (현재) — Java 테스트 393개, security-lint 15항목

**공통 유틸·서비스**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `FileMagicValidatorTest.java` | 12 | 9개 확장자 magic bytes 정상·불일치 |
| `FileUtilsTest.java` | 9 | 확장자 추출, 경로 처리 |
| `PasswordValidatorTest.java` | 7 | 비밀번호 복잡도 규칙 5가지 |
| `AuditServiceTest.java` | 5 | user not found 스킵, 감사 저장, X-Forwarded-For, save 예외 전파 방지 |

**사용자·인증**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `SignupValidationTest.java` | 14 | SignupRequest 유효성 검증 5가지 규칙 |
| `SignupServiceTest.java` | 16 | 이메일 중복·허용 목록 차단·인증 코드 발급·완료·미래 생년월일 차단 |
| `PasswordResetTokenTest.java` | 8 | 토큰 만료·재사용 불가·consumed 상태 |
| `PasswordResetServiceTest.java` | 13 | 코드 발급·검증·비밀번호 변경 흐름 |
| `EmailAllowlistServiceTest.java` | 9 | 허용 이메일 추가·삭제·조회 |
| `SettingsServiceTest.java` | 6 | 이름·주소·팀·비밀번호 변경 |
| `UserRoleServiceTest.java` | 5 | 역할 변경, TEAM_LEADER 차단 |
| `EmployeeManagementServiceTest.java` | 12 | 상태 전환(ADMIN 보호·ACTIVE↔DISABLED), 직원 삭제, 팀·직급 변경 |
| `CareerCalculatorTest.java` | 6 | 경력 계산, 중복 구간 제거 |

**문서·폴더**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `DocumentAccessServiceTest.java` | 12 | 역할별 접근 허용·거부, PENDING_REVIEW 제한 |
| `DocumentDeleteServiceTest.java` | 9 | 본인 삭제, ADMIN 강제 삭제, 공용 폴더 삭제 |
| `DocumentReviewServiceTest.java` | 5 | 승인·반려 상태 전환 |
| `FolderAccessServiceTest.java` | 13 | SHARED_PUBLIC read, 개인 폴더 격리, 권한 부여 |
| `DocumentUploadServiceTest.java` | 7 | 확장자·magic bytes·신규 업로드·대용량 검토 플래그·중복 체크섬 |
| `SearchServiceTest.java` | 16 | ADMIN/EMPLOYEE 라우팅·folderKind 필터·날짜 sentinel·kw 변환·중복 제거 |
| `ThumbnailServiceTest.java` | 7 | EMPLOYEE 비소유자 403, ADMIN·소유자 허용, 버전 미존재 404, 파일 삭제 실패 계속 |
| `DocumentFileGcServiceTest.java` | 10 | runGc(후보 없음·있음·파일 삭제 실패·프리뷰), orphanScan(IOException·고아·기존 경로 스킵) |
| `DocumentExpiryServiceTest.java` | 6 | findExpired 위임, findExpiringSoon 오늘+30일, 만료 알림 발송, 이메일 실패 계속 |
| `FolderServiceTest.java` | 3 | 기존 폴더 반환, 신규 폴더 저장·감사, 폴더명에 소유자 이름 포함 |
| `DocumentPreviewResolverTest.java` | 10 | 확장자별 미리보기 타입 결정 |
| `DocumentRepositoryIntegrationTest.java` ¹ | 11 | sentinel 날짜·키워드·타입 필터·날짜 범위 (Testcontainers PostgreSQL) |

¹ `@DataJpaTest` + Testcontainers + `@Import(JpaAuditingConfig.class)`

**권한**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `FolderPermissionServiceTest.java` | 8 | 권한 부여(중복·유저없음·폴더없음·성공), 회수, 부여가능 폴더 목록 |

**팀**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `TeamServiceTest.java` | 11 | 팀 CRUD, project_team 토글, 감사 로그 |

**프로젝트 투입 관리**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `ProjectAssignmentTest.java` | 16 | 엔티티 상태 결정, remainingDays, isActiveOn |
| `ProjectTest.java` | 11 | 프로젝트 엔티티 상태·멤버 관리 |
| `CalendarGridBuilderTest.java` | 18 | 요일 오프셋, 클리핑, CANCELLED 제외 |
| `ProjectAssignmentControllerTest.java` | 12 | HTTP 상태, CSRF, 리다이렉트 |
| `ProjectControllerTest.java` | 13 | 프로젝트 CRUD HTTP 상태·권한 |
| `ProjectAssignmentServiceTest.java` | 15 | CRUD, 권한, 통계, 선택 로직 |
| `ProjectServiceTest.java` | 17 | 프로젝트 서비스 CRUD·멤버·상태 전환 |

**보안 슬라이스**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `SecurityAccessTest.java` | 5 | 미인증·EMPLOYEE·SALES·ADMIN 경로 접근 |
| `RouteSecurityTest.java` | 12 | 미인증 302·EMPLOYEE/SALES/ADMIN 권한 매트릭스 (WebMvc 슬라이스) |

**E2E 통합 테스트**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `E2ETest.java` ² | 24 | 인증(9)·관리자(8)·문서/검색(7) — 단일 클래스, Testcontainers PostgreSQL |

² `@SpringBootTest(MOCK)` + `@ActiveProfiles("e2e")` + `AdminInitializer` 시드. Flyway 비활성화(`application-e2e.yml`), `ddl-auto: create`.

**보안 정적 분석**

| 도구 | 항목 수 | 비고 |
|------|---------|------|
| `scripts/security-lint.sh` | 15 | Bash grep 기반, 0 FAIL 유지 |

> 총 자동화 테스트: **Java 393개** (37개 클래스) + security-lint 15항목

---

### 미커버 영역 (현재)

- 서비스 단위 테스트: `SalesProfileQueryService`, `BundleDownloadService`, `SalesProfileExporter`, `SalesMemberService`, `SharedFolderService`, `ColumnViewPreferenceService`, `CertificateService`
- 컨트롤러 슬라이스 테스트: `AdminController` (직접 슬라이스 없음, E2E로 보완 중)

---

## 테스트 도구

### 현재 빌드에 포함된 도구 (`build.gradle`)

```
testImplementation 'org.springframework.boot:spring-boot-starter-test'
    → JUnit 5, AssertJ, Mockito, MockMvc
testImplementation 'org.springframework.security:spring-security-test'
    → @WithMockUser, SecurityMockMvcRequestPostProcessors
testImplementation 'org.testcontainers:junit-jupiter:1.21.0'
testImplementation 'org.testcontainers:postgresql:1.21.0'
testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    → @ServiceConnection, Testcontainers PostgreSQL 18
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
testCompileOnly 'org.projectlombok:lombok'
testAnnotationProcessor 'org.projectlombok:lombok'
```

### 미도입 도구 (도입 전 승인 필요)

| 도구 | 용도 | 비고 |
|------|------|------|
| Playwright/Selenium | E2E 브라우저 테스트 | 별도 런타임 필요 |

---

## 테스트 프로파일

| 프로파일 | 파일 | 용도 |
|----------|------|------|
| `test` | `application-test.yml` | 단위·슬라이스 테스트 — Flyway 비활성, `ddl-auto: create`, stub mail/S3 |
| `e2e` | `application-e2e.yml` | E2E 통합 테스트 — Flyway 비활성, `ddl-auto: create`, `AdminInitializer` 시드 (`admin@test.com / Test1234!`) |

---

## 실행 명령

```bash
# 전체 테스트 + 빌드
./gradlew test
./gradlew build

# 보안 정적 분석 (0 FAIL 유지)
bash scripts/security-lint.sh

# E2E만 실행
./gradlew test --tests "com.eactive.resourcehub.e2e.E2ETest"

# 특정 테스트 클래스만 실행
./gradlew test --tests "com.eactive.resourcehub.user.service.SignupServiceTest"
```

---

## 테스트 작성 원칙

1. **반복 실행 가능** — 테스트 데이터는 각 테스트에서 생성·정리. 외부 상태 의존 금지
2. **외부 서비스 격리** — SMTP: `ConsoleEmailSender` 프로파일 또는 Mock. Certificate Flask: Mock. S3: 임시 디렉토리 `LocalFileStorage`
3. **실패하는 테스트 커밋 금지** — 모든 테스트가 통과하는 상태로만 커밋
4. **테스트 프로파일 분리** — `application-test.yml` / `application-e2e.yml` 로 운영 설정과 분리
5. **E2E는 단일 클래스** — 병렬 컨텍스트 생성으로 인한 레이스 컨디션(AdminInitializer unique constraint) 방지

---

## 테스트하기 어려운 영역

| 영역 | 이유 | 대체 검증 |
|------|------|----------|
| Python Flask 재직증명서 생성 | 별도 컨테이너, LibreOffice 런타임 필요 | 수동 QA (`docs/archive/qa/qa-checklist.md`) |
| 이메일 발송 | 운영 SMTP 의존 | `ConsoleEmailSender` 프로파일로 로그 확인 |
| 파일 GC cron | 스케줄 시간(02:00) 의존 | 수동 GC 실행(`/admin/gc`) 또는 단위 테스트에서 메서드 직접 호출 |
| 썸네일 비동기 생성 | `@Async`, LibreOffice 도구 필요 | 단위 테스트에서 Mock, 운영 환경 수동 확인 |
| S3 파일 스토리지 | 외부 서비스 | `LocalFileStorage` + 임시 디렉토리로 대체 테스트 |
| 세션 만료 (DISABLED 계정) | 실시간 세션 레지스트리 | MockMvc + Mock SessionRegistry |

---

## 보안 정적 분석 항목 (security-lint.sh)

| 번호 | 항목 |
|------|------|
| 1 | JWT 사용 금지 |
| 2 | Remember-Me 금지 |
| 3 | CSRF 비활성화 금지 |
| 4 | 파일 직접 노출 금지 |
| 5 | 컨트롤러 Repository 직접 주입 금지 |
| 6 | SQL 인젝션 위험 패턴 |
| 7 | XSS — th:utext / th:inline javascript 금지 |
| 8 | 업로드 파일 저장경로 UUID 사용 확인 |
| 9 | 컨트롤러 role 직접 비교 금지 |
| 10 | 이메일 발송 트랜잭션 격리 (try/catch 필수) |
| 11 | 템플릿 TEAM_LEADER 배지 금지 |
| 12 | ddl-auto create/create-drop 금지 |
| 13 | HTTP 보안 헤더 설정 확인 |
| 14 | 비밀번호 재설정 코드 로그 노출 금지 |
| 15 | 환경변수 기본값 하드코딩 금지 |
