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
# 5. 컨트롤러 레이어 직접 Repository 주입 금지 (신규 파일만 FAIL, 기존은 WARN)
# ─────────────────────────────────────────────
echo "[5] 컨트롤러 레이어 직접 Repository 주입 금지 (신규 파일)"
CONTROLLER_FILES=$(echo "$JAVA_FILES" | grep -E "/controller/" || true)
# 기술 부채 허용 목록 (Service 레이어 분리 리팩토링 전까지 WARN 처리)
MVP1_EXEMPT="SharedFolderController|MyFolderController|MyActivityController|AdminController|DashboardController|SalesProfileController|SalesController|CareerCalculatorController|SignupController"
if [ -n "$CONTROLLER_FILES" ]; then
  all_hits=$(echo "$CONTROLLER_FILES" | xargs grep -lE "private final [A-Za-z]*Repository" 2>/dev/null || true)
  new_hits=$(echo "$all_hits" | grep -vE "$MVP1_EXEMPT" || true)
  old_hits=$(echo "$all_hits" | grep -E "$MVP1_EXEMPT" || true)
  if [ -n "$new_hits" ]; then
    err "신규 컨트롤러에서 Repository 직접 주입 감지 (Service 레이어 경유 필수):"
    echo "$new_hits" | sed 's/^/         /'
  fi
  if [ -n "$old_hits" ]; then
    warn "기존 컨트롤러 Repository 직접 주입 (기술 부채 — 리팩토링 대상):"
    echo "$old_hits" | sed 's/^/         /'
  fi
  [ -z "$all_hits" ] && ok "컨트롤러 DB 직접 접근 없음"
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
# 10. @Transactional 메서드 내 이메일 발송 격리 누락
# ─────────────────────────────────────────────
echo "[10] 이메일 발송 트랜잭션 격리 (@Transactional 내 emailSender 직접 호출 시 try/catch 필수)"
hits=$(echo "$JAVA_FILES" | xargs grep -l "emailSender" 2>/dev/null || true)
if [ -n "$hits" ]; then
  for f in $hits; do
    # emailSender 호출이 try 블록 없이 @Transactional 클래스/메서드 내에 있는지 휴리스틱 검사
    if grep -q "@Transactional" "$f" && grep -q "emailSender\." "$f"; then
      if ! grep -qE "try\s*\{" "$f"; then
        err "이메일 발송 try/catch 누락 ($f) — 트랜잭션 롤백 방지 필요"
      fi
    fi
  done
  ok "이메일 발송 격리 검사 완료"
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
