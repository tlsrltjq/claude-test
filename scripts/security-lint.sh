#!/usr/bin/env bash
# security-lint.sh — 이 프로젝트의 보안 규칙 위반을 정적으로 검사한다.
# 종료 코드: 0=이상 없음, 1=위반 발견
#
# 사용:
#   bash scripts/security-lint.sh              # 전체 검사
#   bash scripts/security-lint.sh --changed    # git diff HEAD의 파일만
set -u

JAVA_SRC="src/main/java"
TEMPLATES="src/main/resources/templates"
FAIL=0

# 색상
RED='\033[0;31m'; YELLOW='\033[1;33m'; GREEN='\033[0;32m'; NC='\033[0m'

err()  { echo -e "${RED}[FAIL]${NC}  $*"; FAIL=$((FAIL+1)); }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $*"; }

# 검사 대상 파일 결정
if [ "${1:-}" = "--changed" ]; then
  JAVA_FILES=$(git diff --name-only HEAD -- '*.java' 2>/dev/null | xargs -I{} find . -path "./$JAVA_SRC/**/{}" 2>/dev/null || true)
  HTML_FILES=$(git diff --name-only HEAD -- '*.html' 2>/dev/null | xargs -I{} find . -path "./$TEMPLATES/**/{}" 2>/dev/null || true)
  [ -z "$JAVA_FILES" ] && JAVA_FILES=$(find "$JAVA_SRC" -name "*.java")
  [ -z "$HTML_FILES" ] && HTML_FILES=$(find "$TEMPLATES" -name "*.html")
else
  JAVA_FILES=$(find "$JAVA_SRC" -name "*.java")
  HTML_FILES=$(find "$TEMPLATES" -name "*.html")
fi

echo "============================================"
echo " eActive Resource Hub — Security Lint"
echo "============================================"
echo

# ─────────────────────────────────────────────
# 1. JWT 사용 금지
# ─────────────────────────────────────────────
echo "[1] JWT 사용 금지"
hits=$(echo "$JAVA_FILES" | xargs grep -lE "io\.jsonwebtoken|com\.auth0\.jwt|JwtUtil|JwtToken|parseJwt|createJwt|\.compact\(\)" 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "JWT 라이브러리/패턴 감지 (세션 기반 인증만 허용):"
  echo "$hits" | sed 's/^/         /'
else
  ok "JWT 미사용 확인"
fi

# ─────────────────────────────────────────────
# 2. Remember-Me 설정 금지
# ─────────────────────────────────────────────
echo "[2] Remember-Me 금지"
hits=$(echo "$JAVA_FILES" | xargs grep -lE "\.rememberMe\(\)|rememberMeServices|TokenBasedRememberMeServices" 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "Remember-Me 설정 감지:"
  echo "$hits" | sed 's/^/         /'
else
  ok "Remember-Me 미사용 확인"
fi

# ─────────────────────────────────────────────
# 3. CSRF 비활성화 금지
# ─────────────────────────────────────────────
echo "[3] CSRF 비활성화 금지"
hits=$(echo "$JAVA_FILES" | xargs grep -lE "csrf\(\)\.disable\(\)|\.csrf\(AbstractHttpConfigurer::disable\)|csrf\.disable" 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "CSRF 비활성화 감지:"
  echo "$hits" | sed 's/^/         /'
else
  ok "CSRF 활성화 상태 확인"
fi

# ─────────────────────────────────────────────
# 4. 파일 직접 노출 금지 (컨트롤러 우회)
# ─────────────────────────────────────────────
echo "[4] 파일 직접 노출 금지"
# static/ 또는 resources/files/ 등을 직접 서빙하는 addResourceHandlers 탐지
hits=$(echo "$JAVA_FILES" | xargs grep -lE "addResourceHandlers|ResourceHandlerRegistry" 2>/dev/null || true)
if [ -n "$hits" ]; then
  # 실제 업로드 경로를 노출하는지 확인
  dangerous=$(echo "$hits" | xargs grep -lE "upload|storage|files/" 2>/dev/null || true)
  if [ -n "$dangerous" ]; then
    err "업로드 경로를 addResourceHandlers로 직접 노출하는 코드 감지:"
    echo "$dangerous" | sed 's/^/         /'
  else
    warn "addResourceHandlers 사용 — 업로드 경로 미노출 여부를 수동 확인: $hits"
  fi
else
  ok "정적 파일 직접 노출 없음"
fi

# ─────────────────────────────────────────────
# 5. 컨트롤러 레이어 직접 Repository 주입 금지 (전 컨트롤러 균등)
#    8da9eaa 커밋(refactor: 컨트롤러 Repository 직접 주입 제거) 이후
#    MVP1_EXEMPT 면제 목록 폐기. 모든 컨트롤러에서 Repository 직접 주입은 FAIL.
# ─────────────────────────────────────────────
echo "[5] 컨트롤러 레이어 직접 Repository 주입 금지"
CONTROLLER_FILES=$(echo "$JAVA_FILES" | grep -E "/controller/" || true)
if [ -n "$CONTROLLER_FILES" ]; then
  all_hits=$(echo "$CONTROLLER_FILES" | xargs grep -lE "private final [A-Za-z]*Repository" 2>/dev/null || true)
  if [ -n "$all_hits" ]; then
    err "컨트롤러에서 Repository 직접 주입 감지 (Service 레이어 경유 필수, docs/decisions.md ADR-022):"
    echo "$all_hits" | sed 's/^/         /'
  else
    ok "컨트롤러 DB 직접 접근 없음"
  fi
fi

# ─────────────────────────────────────────────
# 6. SQL 인젝션 위험 패턴 (사용자 입력이 native query에 직접 연결되는 경우만)
# @Query JPQL 멀티라인 문자열 연결(+)은 컴파일 타임 상수로 안전 — 제외
# ─────────────────────────────────────────────
echo "[6] SQL 인젝션 위험 패턴"
# nativeQuery=true인 곳에서 변수(파라미터)를 + 연결하거나
# entityManager.createNativeQuery에 변수 연결 시 위험
hits=$(echo "$JAVA_FILES" | xargs grep -nE \
  'createNativeQuery\s*\(\s*"[^"]*"\s*\+\s*[a-zA-Z]|nativeQuery\s*=\s*true[^}]*\+\s*[a-zA-Z]' \
  2>/dev/null || true)
if [ -n "$hits" ]; then
  err "사용자 입력이 Native Query에 직접 연결되는 패턴 감지:"
  echo "$hits" | sed 's/^/         /'
else
  ok "SQL 인젝션 위험 패턴 없음"
fi

# ─────────────────────────────────────────────
# 7. XSS — Thymeleaf th:utext / th:inline 금지
# ─────────────────────────────────────────────
echo "[7] XSS — th:utext / th:inline javascript 금지"
hits=$(echo "$HTML_FILES" | xargs grep -lnE 'th:utext|th:inline\s*=\s*"javascript"' 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "XSS 위험: th:utext 또는 th:inline=\"javascript\" 감지:"
  echo "$hits" | sed 's/^/         /'
else
  ok "th:utext/th:inline 미사용 확인"
fi

# ─────────────────────────────────────────────
# 8. 파일명 UUID 저장 (원본 파일명을 저장경로로 사용 금지)
# ─────────────────────────────────────────────
echo "[8] 업로드 파일 저장경로에 원본 파일명 사용 금지"
hits=$(echo "$JAVA_FILES" | xargs grep -nE "getOriginalFilename\(\)\s*[^;]*\.(store|save|write|copy|move)\|Paths\.get.*getOriginalFilename" 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "getOriginalFilename()을 저장 경로에 사용하는 코드 감지:"
  echo "$hits" | sed 's/^/         /'
else
  ok "UUID 저장 패턴 사용 확인"
fi

# ─────────────────────────────────────────────
# 9. 권한 검사 Service 레이어 외부 누락 (컨트롤러에서 직접 User.role 비교)
# ─────────────────────────────────────────────
echo "[9] 컨트롤러에서 role 직접 비교 금지 (Service 레이어 위임 필수)"
if [ -n "${CONTROLLER_FILES:-}" ]; then
  hits=$(echo "$CONTROLLER_FILES" | xargs grep -nE "\.getRole\(\)\s*==|\.getRole\(\)\.equals\(|\.getRole\(\)\.name\(\)\s*\." 2>/dev/null || true)
  if [ -n "$hits" ]; then
    err "컨트롤러에서 role 직접 비교 감지:"
    echo "$hits" | sed 's/^/         /'
  else
    ok "컨트롤러 role 직접 비교 없음"
  fi
fi

# ─────────────────────────────────────────────
# 10. @Transactional 메서드 내 이메일 발송 격리 누락 (메서드 단위 정밀 검사)
#    Python 보조 스크립트로 enclosing method/class 의 @Transactional 여부와
#    try { ... } 감싸짐 여부를 함께 본다. (docs/decisions.md ADR-010)
# ─────────────────────────────────────────────
echo "[10] 이메일 발송 트랜잭션 격리 (@Transactional 메서드 내 emailSender 호출은 try/catch 필수)"
EMAIL_CHECKER="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lint/check_email_transactional.py"
if [ ! -f "$EMAIL_CHECKER" ]; then
  warn "보조 스크립트 누락: $EMAIL_CHECKER — 검사 건너뜀"
elif ! command -v python3 >/dev/null 2>&1; then
  warn "python3 미설치 — 검사 건너뜀"
else
  EMAIL_FILES=$(echo "$JAVA_FILES" | xargs grep -l "emailSender" 2>/dev/null || true)
  if [ -z "$EMAIL_FILES" ]; then
    ok "emailSender 사용 파일 없음"
  else
    output=$(python3 "$EMAIL_CHECKER" $EMAIL_FILES 2>&1)
    rc=$?
    if [ $rc -eq 0 ]; then
      ok "이메일 발송 격리 검사 통과 (메서드 단위)"
    else
      err "이메일 발송 try/catch 누락 (트랜잭션 롤백 방지 필요, docs/decisions.md ADR-010):"
      echo "$output" | sed 's/^/         /'
    fi
  fi
fi

# ─────────────────────────────────────────────
# 11. TEAM_LEADER 신규 사용 금지 (templates 한정 — Java는 @Deprecated로 컴파일러가 경고)
# ─────────────────────────────────────────────
echo "[11] 템플릿 TEAM_LEADER 신규 배지/표시 금지"
# user-role.html의 필터 조건(th:if != TEAM_LEADER)은 허용
hits=$(echo "$HTML_FILES" | xargs grep -nE "th:case=\"'TEAM_LEADER'\"|TEAM_LEADER.*badge|badge.*TEAM_LEADER" 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "템플릿에 TEAM_LEADER 배지 잔존:"
  echo "$hits" | sed 's/^/         /'
else
  ok "템플릿 TEAM_LEADER 배지 없음"
fi

# ─────────────────────────────────────────────
# 12. ddl-auto: create/create-drop 금지 (Flyway 전용)
# ─────────────────────────────────────────────
echo "[12] ddl-auto create/create-drop 금지"
hits=$(grep -rn "ddl-auto:\s*create" src/main/resources/ 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "ddl-auto: create 감지 — Flyway만 스키마 변경 가능:"
  echo "$hits" | sed 's/^/         /'
else
  ok "ddl-auto 안전 설정 확인"
fi

# ─────────────────────────────────────────────
# 13. HTTP 보안 헤더 누락 (SecurityConfig)
# ─────────────────────────────────────────────
echo "[13] HTTP 보안 헤더 설정 확인"
SECURITY_CONFIG=$(find "$JAVA_SRC" -name "SecurityConfig.java" | head -1)
if [ -n "$SECURITY_CONFIG" ]; then
  if grep -qE "\.headers\(|headersWith" "$SECURITY_CONFIG"; then
    ok "SecurityConfig headers 블록 존재"
  else
    warn "SecurityConfig에 .headers() 블록 없음 — X-Frame-Options 등 HTTP 보안 헤더 미설정 (docs/decisions.md ADR-013 참조)"
  fi
else
  warn "SecurityConfig.java 를 찾을 수 없음"
fi

# ─────────────────────────────────────────────
# 14. 비밀번호 재설정 코드 로그 노출 금지
# ─────────────────────────────────────────────
echo "[14] 비밀번호 재설정 코드 로그 노출 금지"
hits=$(echo "$JAVA_FILES" | xargs grep -nE 'log\.(info|debug|trace)\s*\(.*code=\{\}' 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "재설정 코드를 로그에 출력하는 패턴 감지 (docs/decisions.md ADR-012):"
  echo "$hits" | sed 's/^/         /'
else
  ok "재설정 코드 로그 노출 없음"
fi

# ─────────────────────────────────────────────
# 15. 환경변수 기본값에 비밀번호·시크릿 하드코딩 금지
#     변수명이 *PASSWORD / *SECRET / *KEY / *TOKEN 이고 기본값이 있는 경우만
# ─────────────────────────────────────────────
echo "[15] 환경변수 하드코딩 기본값 (비밀번호·시크릿) 금지"
hits=$(grep -rn '\${[A-Z_]*\(PASSWORD\|SECRET\|KEY\|TOKEN\):[^}][^}]*}' src/main/resources/ 2>/dev/null || true)
if [ -n "$hits" ]; then
  err "application.yml 환경변수에 비밀번호 기본값 하드코딩 감지 (docs/decisions.md ADR-011):"
  echo "$hits" | sed 's/^/         /'
else
  ok "환경변수 기본값 하드코딩 없음"
fi

# ─────────────────────────────────────────────
# 16. safeReferer 중복 구현 금지 (open redirect 방어 비대칭 위험)
# ─────────────────────────────────────────────
echo "[16] safeReferer 중복 구현 금지"
count=$(echo "$JAVA_FILES" | xargs grep -lE "private static.*safeReferer" 2>/dev/null | wc -l | tr -d ' ')
if [ "$count" -gt 1 ]; then
  files=$(echo "$JAVA_FILES" | xargs grep -lE "private static.*safeReferer" 2>/dev/null)
  warn "safeReferer가 ${count}개 파일에 중복 구현됨 — 한 쪽이 수정되면 open redirect 방어 비대칭 발생, RedirectUtils 유틸로 통합 권장:"
  echo "$files" | sed 's/^/         /'
  FAIL=$((FAIL+1))
else
  ok "safeReferer 중복 구현 없음"
fi

# ─────────────────────────────────────────────
# 17. LocalFileStorage 경로 탈출 방어 (resolve 후 startsWith 검증 필수)
# ─────────────────────────────────────────────
echo "[17] LocalFileStorage 경로 탈출 방어"
FS_FILE=$(echo "$JAVA_FILES" | grep -E "LocalFileStorage\.java" | head -1)
if [ -n "$FS_FILE" ]; then
  if grep -q "startsWith" "$FS_FILE"; then
    ok "LocalFileStorage 경로 탈출 방어 확인"
  else
    err "LocalFileStorage.load()/delete()에서 resolve() 후 startsWith(baseDir) 검증 누락 — 경로 탈출 공격 가능:"
    echo "$FS_FILE" | sed 's/^/         /'
  fi
else
  warn "LocalFileStorage.java 를 찾을 수 없음"
fi

# ─────────────────────────────────────────────
# 18. 광범위한 예외(Exception/RuntimeException) catch 블록 내 예외 메시지 모델 노출 금지
#     catch (IllegalArgumentException e) 등 구체적 비즈니스 예외는 허용
# ─────────────────────────────────────────────
echo "[18] 광범위한 예외 catch 블록 내 예외 메시지 모델 노출 금지"
EX_CHECKER="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lint/check_exception_model_leak.py"
if [ ! -f "$EX_CHECKER" ]; then
  warn "보조 스크립트 누락: $EX_CHECKER — 검사 건너뜀"
elif ! command -v python3 >/dev/null 2>&1; then
  warn "python3 미설치 — 검사 건너뜀"
else
  output=$(python3 "$EX_CHECKER" $JAVA_FILES 2>&1)
  rc=$?
  if [ $rc -eq 0 ]; then
    ok "광범위한 예외 catch 내 메시지 노출 없음"
  else
    err "catch (Exception/RuntimeException) 블록에서 e.getMessage()를 모델에 직접 노출 감지 — 내부 오류 정보가 뷰에 노출될 수 있음:"
    echo "$output" | sed 's/^/         /'
  fi
fi

# ─────────────────────────────────────────────
# 결과 요약
# ─────────────────────────────────────────────
echo
echo "============================================"
if [ "$FAIL" -eq 0 ]; then
  echo -e "${GREEN} 보안 린트 통과 — 위반 없음${NC}"
else
  echo -e "${RED} 보안 위반 ${FAIL}건 발견 — 커밋 불가${NC}"
fi
echo "============================================"
exit $FAIL
