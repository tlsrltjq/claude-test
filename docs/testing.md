# 테스트 전략

> 현재 테스트 범위, 도구, 실행 방법, 테스트하기 어려운 영역 기록.
> 참고 소스: `src/test/`, `build.gradle`, `scripts/security-lint.sh`

---

## 현재 테스트 범위

### 자동화 (현재)

| 범위 | 파일 | 케이스 수 | 비고 |
|------|------|-----------|------|
| 경력 계산기 순수 로직 | `CareerCalculatorTest.java` | 6 | 외부 의존 없음, 항상 통과 |
| 보안 정적 분석 | `scripts/security-lint.sh` | 15개 항목 | Bash grep 기반, 빠른 실행 |

### 미커버 영역 (현재)

- 서비스 단위 테스트: SignupService, DocumentAccessService, FolderAccessService, PasswordResetService, FileMagicValidator 등
- 컨트롤러 슬라이스 테스트: 역할별 접근 제어, CSRF 검증
- 통합 테스트: 업로드→검토→승인 전체 흐름, 폴더 권한 부여→접근 흐름
- E2E 테스트: 없음

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

## 단위 테스트 우선순위 (추가 예정)

### 즉시 추가 가능 (순수 로직, 외부 의존 없음)

| 대상 | 검증 항목 |
|------|----------|
| `SignupService` | 이메일 중복, 비밀번호 복잡도 검증, 코드 만료 |
| `PasswordResetService` | 토큰 발급·만료·재사용 불가 |
| `FileMagicValidator` | 9개 확장자 magic bytes 정상·불일치 케이스 |
| `CareerCalculator` | 기존 6개 + 엣지케이스 추가 |
| `FileUtils` | 확장자 추출, 경로 처리 |

### `@DataJpaTest` + H2 또는 Testcontainers 필요

| 대상 | 검증 항목 |
|------|----------|
| `DocumentAccessService` | 역할별 접근 허용·거부, PENDING_REVIEW 접근 제한 |
| `FolderAccessService` | SHARED_PUBLIC read, 개인 폴더 격리, 권한 부여 후 접근 |
| `DocumentUploadService` | 중복 업로드 차단(checksum), 허용 확장자 검증 |
| `DocumentReviewService` | 승인·반려 상태 전환, 재승인 불가 |
| `FolderPermissionService` | 권한 부여·회수·조회 |

### `@WebMvcTest` + MockMvc 필요

| 대상 | 검증 항목 |
|------|----------|
| `SecurityConfig` | 미인증 → 리다이렉트, EMPLOYEE → `/admin/**` 403, SALES → `/admin/**` 403 |
| `SignupController` | CSRF 없는 POST → 403, 유효성 오류 → 폼 재표시 |
| `AdminController` | ADMIN 역할 정상 접근, 비ADMIN 403 |

---

## 테스트하기 어려운 영역

| 영역 | 이유 | 대체 검증 |
|------|------|----------|
| Python Flask 재직증명서 생성 | 별도 컨테이너, LibreOffice 런타임 필요 | 수동 QA (`docs/qa-checklist.md`) |
| 이메일 발송 | 운영 SMTP 의존 | `ConsoleEmailSender` 프로파일로 로그 확인 |
| 파일 GC cron | 스케줄 시간(02:00) 의존 | 수동 GC 실행(`/admin/gc`) 또는 단위 테스트에서 메서드 직접 호출 |
| 썸네일 비동기 생성 | `@Async`, LibreOffice 도구 필요 | 단위 테스트에서 Mock, 운영 환경 수동 확인 |
| S3 파일 스토리지 | 외부 서비스 | `LocalFileStorage` + 임시 디렉토리로 대체 테스트 |
| 세션 만료 (DISABLED 계정) | 실시간 세션 레지스트리 | MockMvc + Mock SessionRegistry |
| E2E 브라우저 흐름 | Playwright 등 미도입 | 수동 QA (`docs/qa-checklist.md`) |

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
