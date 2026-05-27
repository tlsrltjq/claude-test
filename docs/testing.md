# 테스트 전략

> 현재 테스트 범위, 도구, 실행 방법, 테스트하기 어려운 영역 기록.
> 참고 소스: `src/test/`, `build.gradle`, `scripts/security-lint.sh`

---

## 현재 테스트 범위

### 자동화 (현재)

**공통 유틸**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `FileMagicValidatorTest.java` | 12 | 9개 확장자 magic bytes 정상·불일치 |
| `FileUtilsTest.java` | 9 | 확장자 추출, 경로 처리 |

**사용자·인증**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `SignupValidationTest.java` | 14 | SignupRequest 유효성 검증 5가지 규칙 |
| `PasswordResetTokenTest.java` | 8 | 토큰 만료·재사용 불가·consumed 상태 |
| `PasswordResetServiceTest.java` | 13 | 코드 발급·검증·비밀번호 변경 흐름 |
| `EmailAllowlistServiceTest.java` | 9 | 허용 이메일 추가·삭제·조회 |
| `SettingsServiceTest.java` | 6 | 이름·주소·팀·비밀번호 변경 |
| `UserRoleServiceTest.java` | 5 | 역할 변경, TEAM_LEADER 차단 |
| `CareerCalculatorTest.java` | 6 | 경력 계산, 중복 구간 제거 |

**문서·폴더**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `DocumentAccessServiceTest.java` | 12 | 역할별 접근 허용·거부, PENDING_REVIEW 제한 |
| `DocumentDeleteServiceTest.java` | 9 | 본인 삭제, ADMIN 강제 삭제, 공용 폴더 삭제 |
| `DocumentReviewServiceTest.java` | 5 | 승인·반려 상태 전환 |
| `FolderAccessServiceTest.java` | 13 | SHARED_PUBLIC read, 개인 폴더 격리, 권한 부여 |

**팀·보안**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `TeamServiceTest.java` | 11 | 팀 CRUD, project_team 토글, 감사 로그 |
| `SecurityAccessTest.java` | 5 | 미인증·EMPLOYEE·SALES·ADMIN 경로 접근 |

**프로젝트 투입 관리**

| 파일 | 케이스 수 | 범위 |
|------|-----------|------|
| `ProjectAssignmentTest.java` | 15 | 엔티티 상태 결정, remainingDays, isActiveOn |
| `ProjectAssignmentRequestTest.java` | 12 | validate() 5가지 오류 규칙 |
| `CalendarGridBuilderTest.java` | 11 | 요일 오프셋, 클리핑, CANCELLED 제외 |
| `ProjectAssignmentServiceTest.java` | 22 | CRUD, 권한, 통계, 선택 로직 |
| `ProjectAssignmentControllerTest.java` | 13 | HTTP 상태, CSRF, 리다이렉트 |

**보안 정적 분석**

| 도구 | 항목 수 | 비고 |
|------|---------|------|
| `scripts/security-lint.sh` | 15 | Bash grep 기반, 0 FAIL 유지 |

> 총 자동화 테스트: **210개** (Java 테스트 195개 + security-lint 15항목)

### 미커버 영역 (현재)

- 서비스 단위 테스트: `SignupService`, `DocumentUploadService`, `SalesProfileQueryService`, `BundleDownloadService`, `SalesProfileExporter`, `SalesMemberService`, `SharedFolderService`, `DocumentFileGcService`, `ColumnViewPreferenceService`, `CertificateService`
- 컨트롤러 슬라이스 테스트: `AdminController`. `/sales/**` EMPLOYEE 역할 차단은 `@WebMvcTest` 슬라이스 제약으로 검증 불가 → 수동 QA 보완
- 통합 테스트: 업로드→검토→승인 전체 흐름, 폴더 권한 부여→접근 흐름
- E2E 테스트: 없음 (Playwright 등 미도입)

---

## 테스트 도구

### 현재 빌드에 포함된 도구 (`build.gradle`)

```
testImplementation 'org.springframework.boot:spring-boot-starter-test'
    → JUnit 5, AssertJ, Mockito, MockMvc
testImplementation 'org.springframework.security:spring-security-test'
    → @WithMockUser, SecurityMockMvcRequestPostProcessors
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
testCompileOnly 'org.projectlombok:lombok'
testAnnotationProcessor 'org.projectlombok:lombok'
```

### 외부 의존성이 필요한 도구 (미도입, 도입 전 승인 필요)

| 도구 | 용도 | 비고 |
|------|------|------|
| Testcontainers | PostgreSQL 실제 DB 통합 테스트 | build.gradle 의존성 추가 필요 |
| Playwright/Selenium | E2E 브라우저 테스트 | 별도 런타임 필요 |

---

## 실행 명령

```bash
# 전체 테스트 + 빌드
./gradlew test
./gradlew build

# 보안 정적 분석 (0 FAIL 유지)
bash scripts/security-lint.sh

# 특정 테스트 클래스만 실행
./gradlew test --tests "com.eactive.resourcehub.user.service.CareerCalculatorTest"
```

---

## 테스트 작성 원칙

1. **반복 실행 가능** — 테스트 데이터는 각 테스트에서 생성·정리. 외부 상태 의존 금지
2. **외부 서비스 격리** — SMTP: `ConsoleEmailSender` 프로파일 또는 Mock 사용. Certificate Flask: Mock 처리. S3: 임시 디렉토리 LocalFileStorage 사용
3. **실패하는 테스트 커밋 금지** — 모든 테스트가 통과하는 상태로만 커밋
4. **테스트 프로파일** — `application-test.yml` 또는 `@TestPropertySource`로 운영 설정과 분리

---

## 테스트 추가 우선순위

### 즉시 추가 가능 (순수 로직, 외부 의존 없음)

| 대상 | 검증 항목 |
|------|----------|
| `SignupService` | 이메일 중복, 허용 목록 차단, 코드 만료 |
| `DocumentUploadService` | magic bytes 검증, 중복 체크섬 차단, 허용 확장자 |
| `SalesMemberService` | 회원 목록 필터·정렬, 자동채움 데이터 |

### `@WebMvcTest` + MockMvc 필요

| 대상 | 검증 항목 |
|------|----------|
| `AdminController` | ADMIN 역할 정상 접근, 비ADMIN 403 |
| `SignupController` | CSRF 없는 POST → 403, 유효성 오류 → 폼 재표시 |

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
| E2E 브라우저 흐름 | Playwright 등 미도입 | 수동 QA (`docs/archive/qa/qa-checklist.md`) |

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
