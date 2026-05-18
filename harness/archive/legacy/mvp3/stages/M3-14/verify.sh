#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-14 — 보안·성능·UX 최종 품질 =="; echo

# ── 보안 헤더 ──────────────────────────────────────────────
echo "--- 보안 헤더 ---"
SECURITY_CFG=$(find "$SRC" -name "SecurityConfig.java" | head -1)
check "SecurityConfig .headers() 블록"       bash -c "grep -qE '\.headers\(|headersWith' '$SECURITY_CFG'"
check "X-Frame-Options DENY"                  bash -c "grep -qE 'frameOptions|DENY|frameOptionsConfig' '$SECURITY_CFG'"
check "X-Content-Type-Options nosniff"        bash -c "grep -qE 'contentTypeOptions|nosniff' '$SECURITY_CFG'"
check "Content-Security-Policy 설정"          bash -c "grep -qE 'contentSecurityPolicy|ContentSecurityPolicy' '$SECURITY_CFG'"

# ── 비밀번호 재설정 코드 로그 제거 ───────────────────────────
echo "--- 로그 보안 ---"
PRS=$(find "$SRC" -name "PasswordResetService.java" | head -1)
check "PasswordResetService code 로그 제거"   bash -c "! grep -qE 'log\.(info|debug).*code=\{\}' '$PRS'"

# ── 환경변수 기본값 ────────────────────────────────────────
echo "--- 환경변수 ---"
check "application.yml 비밀번호 기본값 없음"  bash -c "! grep -qE '\\\$\{[A-Z_]*:(Admin|Password|Secret)' '$RES/application.yml'"

# ── 커스텀 에러 페이지 ─────────────────────────────────────
echo "--- 에러 페이지 ---"
check "templates/error/404.html 존재"         test -f "$RES/templates/error/404.html"
check "templates/error/403.html 존재"         test -f "$RES/templates/error/403.html"
check "templates/error/500.html 존재"         test -f "$RES/templates/error/500.html"

# ── GlobalExceptionHandler 확장 ───────────────────────────
echo "--- 예외 처리 ---"
GEH=$(find "$SRC" -name "GlobalExceptionHandler.java" | head -1)
check "GlobalExceptionHandler ResponseStatusException 핸들러" bash -c "grep -q 'ResponseStatusException' '$GEH'"
check "GlobalExceptionHandler generic Exception 핸들러"       bash -c "grep -qE '@ExceptionHandler\(Exception' '$GEH'"

# ── 비동기 썸네일 ──────────────────────────────────────────
echo "--- 성능: @Async ---"
THUMB=$(find "$SRC" -name "ThumbnailService.java" | head -1)
check "ThumbnailService @Async 선언"          bash -c "grep -q '@Async' '$THUMB'"
check "@EnableAsync 선언 존재"                bash -c "grep -rq '@EnableAsync' '$SRC'"

# ── 서버사이드 페이지네이션 ───────────────────────────────
echo "--- 성능: 페이지네이션 ---"
EMP="$RES/templates/admin/employees.html"
check "employees.html 페이지 파라미터 존재"   bash -c "grep -qE 'page=|pageable|pagination' '$EMP'"

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
