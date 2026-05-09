# 최종 품질 개선 계획 (M3-14)

> 작성일: 2026-05-07  
> 최종 업데이트: 2026-05-08  
> 검증 스크립트: `bash harness/mvp3/stages/M3-14/verify.sh`  
> 린터: `bash scripts/security-lint.sh` → **0 FAIL 달성 (15/15 PASS)**

---

## 완료 현황 요약

| 항목 | 상태 |
|------|------|
| 보안 1-1 HTTP 헤더 | ✅ 완료 |
| 보안 1-2 비밀번호 재설정 코드 로그 제거 | ✅ 완료 |
| 보안 1-3 환경변수 하드코딩 기본값 제거 | ✅ 완료 |
| 성능 2-1 AdminController N+1 제거 | 🔲 미완료 |
| 성능 2-2 읽기 전용 트랜잭션 | 🔲 미완료 |
| 성능 2-3 직원 목록 페이지네이션 | 🔲 미완료 |
| 성능 2-4 썸네일 @Async | 🔲 미완료 |
| UX 3-1 폼 제출 스피너 | ✅ 완료 (업로드 폼) |
| UX 3-2 네비바 active 상태 | 🔲 미완료 |
| UX 3-3 빈 목록 Empty State | ✅ 완료 (UI 전면 개선 시 적용) |
| UX 3-4 커스텀 에러 페이지 | ✅ 완료 (403/404/500) |
| 코드품질 4-1 GlobalExceptionHandler 확장 | 🔲 미완료 |
| 코드품질 4-2 FileUtils 헬퍼 | 🔲 미완료 |

---

## 1. 보안 (Security)

### 1-1. HTTP 보안 헤더 추가 ✅ 완료
**파일**: `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`

`securityFilterChain()` 안에 `.headers()` 블록 추가:

```java
.headers(headers -> headers
    .frameOptions(frame -> frame.deny())
    .contentTypeOptions(Customizer.withDefaults())
    .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
    .contentSecurityPolicy(csp -> csp.policyDirectives(
        "default-src 'self'; " +
        "script-src 'self' cdn.jsdelivr.net 'unsafe-inline'; " +
        "style-src 'self' cdn.jsdelivr.net 'unsafe-inline'; " +
        "img-src 'self' data:; " +
        "font-src 'self' cdn.jsdelivr.net"
    ))
)
```

- HSTS는 `application.yml secure: true` 전환 시 함께 추가 (현재 로컬 개발 환경)
- 완료 조건: `security-lint.sh [13]` WARN → OK

### 1-2. 비밀번호 재설정 코드 로그 제거 ✅ 완료
**파일**: `src/main/java/com/eactive/resourcehub/user/service/PasswordResetService.java`

```java
// Before (line 52)
log.info("[PASSWORD_RESET] 코드 발급 — email={}, code={}", email, code);

// After
log.info("[PASSWORD_RESET] 코드 발급 — email={}", email);
```

- 완료 조건: `security-lint.sh [14]` FAIL → OK

### 1-3. 환경변수 하드코딩 기본값 제거 ✅ 완료
**파일**: `src/main/resources/application.yml`

```yaml
# Before (line 53)
password: ${RESOURCEHUB_ADMIN_PASSWORD:Admin1234!}

# After
password: ${RESOURCEHUB_ADMIN_PASSWORD}
```

DB 비밀번호도 동일하게 처리:
```yaml
# Before (line 13)
password: ${SPRING_DATASOURCE_PASSWORD:resourcehub}

# After
password: ${SPRING_DATASOURCE_PASSWORD}
```

- `.env` 파일 또는 `docker-compose.yml` `environment:` 에 실제 값 설정
- 완료 조건: `security-lint.sh [15]` FAIL → OK

---

## 2. 성능 (Performance)

### 2-1. AdminController N+1 제거
**파일**: `src/main/java/com/eactive/resourcehub/admin/controller/AdminController.java`

`employees()` 메서드에서 `teamRepository.findAll()`이 여러 곳에서 중복 호출됨.  
메서드 상단에서 한 번만 호출해 변수에 담아 재사용.

```java
// Before: 여러 메서드에서 각각 teamRepository.findAll() 호출
// After: 메서드 상단에서 한 번 호출
List<Team> teams = teamRepository.findAll();
```

### 2-2. 읽기 전용 트랜잭션 적용
아래 Service 조회 메서드에 `@Transactional(readOnly = true)` 추가:

| 클래스 | 메서드 |
|--------|--------|
| `AdminUserService` | `findPendingUsers()`, `findEmployees()`, `findEmployee()` |
| `DocumentService` | `findByFolder()`, `findById()` |
| `FolderService` | `findMyFolders()`, `findSharedFolders()` |
| `SalesMemberService` | `findAll()`, `findById()` |

클래스 레벨 `@Transactional`이 있으면 메서드 레벨로 override.

### 2-3. 직원 목록 서버사이드 페이지네이션
**파일**: `src/main/java/com/eactive/resourcehub/admin/controller/AdminController.java`

```java
// Before
public String employees(String q, String position, String role, Long teamId, Model model)

// After
public String employees(String q, String position, String role, Long teamId,
                        @RequestParam(defaultValue = "0") int page, Model model) {
    Pageable pageable = PageRequest.of(page, 20, Sort.by("name"));
    Page<User> result = adminUserService.findEmployees(q, position, role, teamId, pageable);
    model.addAttribute("users", result.getContent());
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", result.getTotalPages());
    ...
}
```

**파일**: `src/main/resources/templates/admin/employees.html`  
Bootstrap pagination 컴포넌트 추가:

```html
<nav th:if="${totalPages > 1}">
  <ul class="pagination justify-content-center">
    <li class="page-item" th:classappend="${currentPage == 0} ? 'disabled'">
      <a class="page-link" th:href="@{/admin/employees(page=${currentPage - 1}, q=${q})}">이전</a>
    </li>
    <li class="page-item" th:each="i : ${#numbers.sequence(0, totalPages - 1)}"
        th:classappend="${i == currentPage} ? 'active'">
      <a class="page-link" th:href="@{/admin/employees(page=${i}, q=${q})}" th:text="${i + 1}"></a>
    </li>
    <li class="page-item" th:classappend="${currentPage == totalPages - 1} ? 'disabled'">
      <a class="page-link" th:href="@{/admin/employees(page=${currentPage + 1}, q=${q})}">다음</a>
    </li>
  </ul>
</nav>
```

### 2-4. 썸네일 생성 @Async 전환
**파일**: `src/main/java/com/eactive/resourcehub/document/service/ThumbnailService.java`

```java
@Async
public void generateAndSave(DocumentVersion version) { ... }
```

**파일**: `src/main/java/com/eactive/resourcehub/EactiveResourceHubApplication.java`

```java
@EnableAsync
@SpringBootApplication
public class EactiveResourceHubApplication { ... }
```

- `@Async` 메서드는 `void` 반환 — 트랜잭션 전파 주의 (새 트랜잭션 필요 시 `@Transactional(propagation = REQUIRES_NEW)`)
- `DocumentUploadService`의 기존 `try/catch` 래퍼 유지

---

## 3. UX / UI

### 3-1. 폼 제출 로딩 스피너
대상 페이지: 로그인, 업로드, 비밀번호 변경  
공통 패턴 (각 페이지 `<script>` 블록에 추가):

```javascript
document.getElementById('submitForm').addEventListener('submit', function() {
  var btn = document.getElementById('submitBtn');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>처리 중...';
});
```

### 3-2. 네비게이션 바 active 상태
각 관리자 페이지 네비바 링크에 Thymeleaf 조건부 클래스:

```html
<a class="nav-link"
   th:classappend="${#request.requestURI.startsWith('/admin/employees')} ? ' active' : ''"
   th:href="@{/admin/employees}">직원 관리</a>
```

### 3-3. 빈 목록 Empty State
대상 템플릿:

| 템플릿 | 조건 |
|--------|------|
| `admin/employees.html` | 검색 결과 없음 |
| `shared/folders.html` | 공유 폴더 없음 |
| `shared/folder-documents.html` | 문서 없음 |
| `my/folder-documents.html` | 내 폴더 문서 없음 |

공통 패턴:

```html
<div th:if="${#lists.isEmpty(items)}" class="text-center text-muted py-5">
  <p class="mb-0">항목이 없습니다.</p>
</div>
```

### 3-4. 커스텀 에러 페이지 ✅ 완료
Spring Boot Thymeleaf 자동 인식 경로: `templates/error/{status}.html`

| 파일 | 내용 |
|------|------|
| `templates/error/404.html` | "페이지를 찾을 수 없습니다" + 홈으로 버튼 |
| `templates/error/403.html` | "접근 권한이 없습니다" + 홈으로 버튼 |
| `templates/error/500.html` | "서버 오류가 발생했습니다" + 홈으로 버튼 |

---

## 4. 코드 품질 (Code Quality)

### 4-1. GlobalExceptionHandler 확장
**파일**: `src/main/java/com/eactive/resourcehub/common/exception/GlobalExceptionHandler.java`

현재 `MaxUploadSizeExceededException`만 처리 → 아래 핸들러 추가:

```java
@ExceptionHandler(ResponseStatusException.class)
public String handleResponseStatus(ResponseStatusException e, Model model) {
    int status = e.getStatusCode().value();
    model.addAttribute("message", e.getReason());
    if (status == 403) return "error/403";
    if (status == 404) return "error/404";
    return "error/500";
}

@ExceptionHandler(Exception.class)
public String handleGeneric(Exception e, HttpServletRequest req, Model model) {
    log.error("Unhandled exception on {}: {}", req.getRequestURI(), e.getMessage(), e);
    return "error/500";
}
```

### 4-2. FileUtils 공통 헬퍼
**신규 파일**: `src/main/java/com/eactive/resourcehub/common/util/FileUtils.java`

```java
public final class FileUtils {
    private FileUtils() {}

    public static String extension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx >= 0) ? filename.substring(idx + 1).toLowerCase() : "";
    }

    public static boolean isAllowedExtension(String filename, Set<String> allowed) {
        return allowed.contains(extension(filename));
    }
}
```

기존 중복 확장자 추출 코드 → `FileUtils.extension()` 호출로 교체.

---

## 작업 순서 (권장)

```
1. 보안 1-2, 1-3  (린터 FAIL 제거 → 커밋 가능 상태 먼저 확보)
2. 보안 1-1       (HTTP 헤더 — 린터 WARN → OK)
3. 코드 품질 4-1  (에러 핸들러 — 에러 페이지와 묶어서 구현)
4. UX 3-4         (커스텀 에러 페이지)
5. 성능 2-1~2-4   (N+1 / readOnly / 페이지네이션 / @Async)
6. UX 3-1~3-3     (스피너 / active / empty state)
7. 코드 품질 4-2  (FileUtils)
```

---

## 완료 기준

- `bash scripts/security-lint.sh` → **0 FAIL, 0 WARN**
- `bash harness/mvp3/stages/M3-14/verify.sh` → **14/14 PASS**
- `./gradlew build` → **BUILD SUCCESSFUL**
- Docker 기동 시 환경변수 미설정 → **앱 기동 실패** (의도된 동작)
