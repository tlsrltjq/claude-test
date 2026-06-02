# 보안 및 권한

> 역할별 접근 규칙, 서비스 레이어 검증 포인트, 코딩 보안 규칙의 단일 출처.
> "왜"는 `docs/decisions.md`(ADR-001~010), "보안 설정 위치"는 `docs/architecture.md`.

---

## 1. 역할 구조

| 역할 | 값 | 설명 |
|------|----|------|
| ADMIN | `ROLE_ADMIN` | 전체 관리 권한 |
| SALES | `ROLE_SALES` | 영업 인력표·투입 관리 조회 |
| EMPLOYEE | `ROLE_EMPLOYEE` | 본인 폴더·공용 폴더 접근 |

---

## 2. URL 패턴별 접근 제어

| 경로 패턴 | 허용 역할 |
|-----------|----------|
| `/login/**`, `/signup/**`, `/health` | 인증 불필요 |
| `/admin/**` | ADMIN |
| `/sales/**` | ADMIN, SALES |
| 그 외 모든 경로 | 인증된 모든 역할 |

> URL 패턴 외에 파일 다운로드·미리보기는 `DocumentAccessService`에서 추가 권한 검사.

---

## 3. 문서·파일 접근 규칙

- **모든 파일 접근**: `DocumentAccessService.getVersionWithAccessCheck()` 경유 필수. 정적 URL 직접 노출 금지.
- **역할별 문서 접근**:
  - ADMIN·SALES: 모든 문서 접근
  - EMPLOYEE: 본인 폴더 + 권한 부여된 폴더 + 공용 폴더(SHARED_PUBLIC) + 본인 업로드 파일
  - APPROVED 아닌 버전: 본인·ADMIN·SALES만 접근
- **폴더 접근**: `FolderAccessService` — 역할별 read/write 판정
- **권한 부여/회수**: `FolderPermissionService` — `Permission` 엔티티 기반

---

## 4. 서비스 레이어 검증 포인트

| 기능 | 검증 위치 |
|------|----------|
| 파일 다운로드·미리보기·썸네일 | `DocumentAccessService.getVersionWithAccessCheck()` |
| 폴더 read/write 판정 | `FolderAccessService` |
| 폴더 권한 부여·회수 | `FolderPermissionService` |
| 투입 관리 CRUD | `ProjectAssignmentService.requireAdmin()` |
| 역할 비교 | Service 레이어에서만 — 컨트롤러 직접 비교 금지 |

---

## 5. 세션 및 인증 흐름

- `SecurityConfig.securityFilterChain` — CSRF on, 세션 30분, sessionFixation `changeSessionId`, maximumSessions(-1) + SessionRegistry
- `CustomUserDetailsService` → `CustomUserDetails` (Spring Security)
- HTTP 헤더: `X-Frame-Options=sameOrigin`, `X-Content-Type-Options=nosniff`, `Referrer-Policy=strict-origin-when-cross-origin`, CSP(nonce 방식)
- **세션 즉시 만료**: 역할·계정 상태 변경 시 `SessionRegistry`로 기존 세션 강제 만료
- **로그인 브루트포스 방어**: `LoginAttemptService` — 10회 실패 시 카운터 리셋 후 `/login/forgot?toomany`로 유도, 성공 시 카운터 초기화
- **인증코드 브루트포스 방어**: 비밀번호 재설정·회원가입 인증코드 모두 5회 실패 시 토큰/세션 무효화 후 처음부터 재시도 요구
- **CSP nonce**: `CspNonceFilter` → 요청마다 랜덤 16바이트 nonce 생성 → `CspNonceHeaderWriter`가 `script-src 'nonce-{값}'` 헤더 동적 생성 → `CspNonceInterceptor`가 Thymeleaf 모델에 `cspNonce` 주입

---

## 6. 감사 로그

- **진입점**: `AuditService.log()` — `@Transactional(propagation = REQUIRES_NEW)`
- **대상 액션**: UPLOAD, DOWNLOAD, VIEW, DELETE_DOCUMENT, APPROVE_DOCUMENT, REJECT_DOCUMENT, SUBMIT_REVIEW, CHANGE_ROLE, GRANT_PERMISSION, REVOKE_PERMISSION, REGENERATE_THUMBNAIL, RESET_PASSWORD, EXPORT_PROFILES, BUNDLE_DOWNLOAD, DISABLE_USER, ENABLE_USER, ASSIGN_PROJECT, UPDATE_ASSIGNMENT, DELETE_ASSIGNMENT, UPLOAD/DOWNLOAD_RESUME_TEMPLATE, UPDATE_CAREER_PROFILE, CREATE, UPDATE, DELETE

---

## 7. 코딩 보안 규칙 (위반 시 security-lint.sh FAIL)

| 규칙 | 근거 |
|------|------|
| JWT 사용 금지 — 세션만 | ADR-001 |
| Remember-Me 금지 | ADR-002 |
| CSRF 항상 활성화 | ADR-003 |
| 파일 정적 노출 금지 | ADR-006 |
| Repository 컨트롤러 직접 주입 금지 | ADR-006, ADR-022 |
| 컨트롤러에서 `role` 직접 비교 금지 | ADR-006 |
| 스키마 변경은 Flyway만 (`ddl-auto: validate`) | ADR-007 |
| 이메일 발송: `@Transactional` 내 `try/catch` 필수 | ADR-010 |
| 감사 로그 `REQUIRES_NEW` 트랜잭션 필수 | ADR-022 |
| 인증코드 5회 실패 시 토큰/세션 무효화 | ADR-040 |
| 로그인 10회 실패 시 비밀번호 재설정 유도 (잠금 없음) | ADR-041 |
| CSP script-src: nonce 방식, unsafe-inline 금지 | ADR-042 |
| 엑셀 업로드: 확장자·매직바이트·1,000행 제한 | ADR-043 |
| SampleDataFixRunner: `@Profile("!prod")` 필수 | ADR-044 |
