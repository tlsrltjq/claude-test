#!/usr/bin/env bash
# 하네스 공용 유틸리티 — 모든 verify.sh에서 source

BASE_URL="${BASE_URL:-http://localhost:8080}"
COOKIE_JAR="$(mktemp)"
PASS=0; FAIL=0

# ── 출력 헬퍼 ────────────────────────────────────────────────
green()  { printf "\033[32m%s\033[0m\n" "$*"; }
red()    { printf "\033[31m%s\033[0m\n" "$*"; }
yellow() { printf "\033[33m%s\033[0m\n" "$*"; }

check() {
  local label="$1"; local result="$2"
  if [[ "$result" == "PASS" ]]; then
    green "  ✔ $label"; ((PASS++))
  else
    red   "  ✘ $label"; ((FAIL++))
  fi
}

summary() {
  echo ""
  if [[ $FAIL -eq 0 ]]; then
    green "결과: ${PASS}/${PASS} PASS"
  else
    red   "결과: ${PASS}/$((PASS+FAIL)) PASS  (실패 $FAIL건)"
  fi
  rm -f "$COOKIE_JAR"
  [[ $FAIL -eq 0 ]]   # exit code: 0=성공 1=실패
}

# ── HTTP 헬퍼 ────────────────────────────────────────────────
get_status() {
  curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_JAR" "$BASE_URL$1"
}

get_body() {
  curl -sL -b "$COOKIE_JAR" "$BASE_URL$1"
}

# 로그인 페이지에서 CSRF 토큰 추출 후 POST 로그인
# 사용: login <loginId> <password>
login() {
  local id="$1" pw="$2"
  curl -sc "$COOKIE_JAR" "$BASE_URL/login" -o /tmp/_lp.html -s
  local csrf; csrf=$(grep -o 'name="_csrf" value="[^"]*"' /tmp/_lp.html | grep -o 'value="[^"]*"' | cut -d'"' -f2)
  local loc; loc=$(curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
    -X POST "$BASE_URL/login" \
    -d "username=$(urlencode "$id")&password=$(urlencode "$pw")&_csrf=$csrf" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -D - -o /dev/null -s | grep -i "^location:" | tr -d '\r' | awk '{print $2}')
  [[ "$loc" != *"error"* ]]
}

# POST with CSRF — returns Location header (redirect target)
post_form() {
  local path="$1"; shift
  local page; page=$(get_body "$path")
  local csrf; csrf=$(echo "$page" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | grep -o 'value="[^"]*"' | cut -d'"' -f2)
  curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
    -X POST "$BASE_URL$path" \
    -d "_csrf=$csrf&$*" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -D - -o /dev/null -s | grep -i "^location:" | tr -d '\r' | awk '{print $2}'
}

# POST with CSRF — returns HTTP status code
post_form_status() {
  local path="$1"; shift
  local page; page=$(get_body "$path")
  local csrf; csrf=$(echo "$page" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | grep -o 'value="[^"]*"' | cut -d'"' -f2)
  curl -sb "$COOKIE_JAR" -c "$COOKIE_JAR" \
    -X POST "$BASE_URL$path" \
    -d "_csrf=$csrf&$*" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -o /dev/null -w "%{http_code}" -s
}

urlencode() {
  python3 -c "import urllib.parse,sys; print(urllib.parse.quote(sys.argv[1]))" "$1" 2>/dev/null \
    || printf '%s' "$1" | sed 's/@/%40/g;s/!/%21/g;s/ /+/g'
}

# DB 직접 쿼리
db_query() {
  docker exec resourcehub-postgres psql -U resourcehub -d resourcehub -tAq -c "$1" 2>/dev/null
}

# 앱 기동 확인
wait_for_app() {
  local n=0
  until curl -s "$BASE_URL/health" | grep -q "OK"; do
    ((n++)); [[ $n -gt 30 ]] && { red "앱이 응답하지 않습니다."; exit 1; }
    sleep 2
  done
}
