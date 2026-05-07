#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"

check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== Stage 03 — Auth =="
echo

check "V2 migration exists" test -f "$RES/db/migration/V2__create_email_verification_tokens.sql"
check "SecurityConfig present" bash -c "grep -rl '@EnableWebSecurity\|SecurityFilterChain' '$SRC' | grep -q ."
check "no JWT library used"     bash -c "! grep -rE 'io\\.jsonwebtoken|nimbusds|JwtBuilder' '$SRC' >/dev/null"
check "EmailSender interface"   bash -c "grep -rl 'interface[[:space:]]\\+EmailSender' '$SRC' | grep -q ."
check "ConsoleEmailSender impl" bash -c "grep -rl 'class[[:space:]]\\+ConsoleEmailSender' '$SRC' | grep -q ."
check "Signup controller"       bash -c "grep -rl '/signup' '$SRC' | grep -q ."
check "Login controller or form-login" bash -c "grep -rE '/login|formLogin' '$SRC' >/dev/null"

# Templates
for v in login signup signup-verify signup-pending dashboard; do
  check "template: $v.html" bash -c "test -f '$RES/templates/$v.html' || test -f '$RES/templates/$v/index.html'"
done

# Application yml
if [ -f "$RES/application.yml" ]; then
  check "yml: RESOURCEHUB_SESSION cookie name" grep -q "RESOURCEHUB_SESSION" "$RES/application.yml"
  check "yml: session timeout"                 grep -qE "timeout: *30m|timeout: *PT30M|timeout: *1800" "$RES/application.yml"
  check "yml: same-site strict"                grep -qiE "same.?site: *strict|same-site: *strict" "$RES/application.yml"
  check "yml: company-email-domain"            grep -q "company-email-domain" "$RES/application.yml"
fi

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
