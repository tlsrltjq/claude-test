#!/usr/bin/env bash
set -u
PASS=0
FAIL=0

HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC_BASE="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"

check() {
  local desc="$1"; shift
  if "$@" >/dev/null 2>&1; then
    echo "  [PASS] $desc"; PASS=$((PASS+1))
  else
    echo "  [FAIL] $desc"; FAIL=$((FAIL+1))
  fi
}

echo "== Stage 01 — Skeleton =="
echo "  project: $PROJECT_ROOT"
echo

check "build.gradle exists"        test -f "$PROJECT_ROOT/build.gradle"
check "settings.gradle exists"     test -f "$PROJECT_ROOT/settings.gradle"
check "application.yml exists"     test -f "$PROJECT_ROOT/src/main/resources/application.yml"
check "docker-compose.yml exists"  test -f "$PROJECT_ROOT/docker-compose.yml"
check ".env.example exists"        test -f "$PROJECT_ROOT/.env.example"
check ".gitignore exists"          test -f "$PROJECT_ROOT/.gitignore"
check "README.md exists"           test -f "$PROJECT_ROOT/README.md"

if [ -f "$PROJECT_ROOT/.gitignore" ]; then
  check ".gitignore has .env"      grep -q "^\.env\$" "$PROJECT_ROOT/.gitignore"
  check ".gitignore has storage/"  grep -q "^storage/" "$PROJECT_ROOT/.gitignore"
  check ".gitignore has logs/"     grep -q "^logs/" "$PROJECT_ROOT/.gitignore"
  check ".gitignore has *.log"     grep -qE "^\*\.log\$" "$PROJECT_ROOT/.gitignore"
fi

# Application main class anywhere under SRC_BASE
check "Spring main class exists"   bash -c "find '$SRC_BASE' -maxdepth 3 -name '*Application.java' | grep -q ."

for pkg in common/config common/exception common/security common/file user team employee document permission audit; do
  check "package $pkg exists"      test -d "$SRC_BASE/$pkg"
done

if [ -f "$PROJECT_ROOT/src/main/resources/application.yml" ]; then
  check "upload.base-dir uses env var" \
    grep -q "RESOURCEHUB_UPLOAD_BASE_DIR" "$PROJECT_ROOT/src/main/resources/application.yml"
fi

if [ -f "$PROJECT_ROOT/build.gradle" ]; then
  check "build.gradle uses Java 21"   grep -qE "(JavaLanguageVersion\.of\(21\)|sourceCompatibility *=? *['\"]?21|languageVersion.*21)" "$PROJECT_ROOT/build.gradle"
  check "build.gradle uses Spring Boot 3.5" grep -qE "org\.springframework\.boot.*3\.5|id 'org.springframework.boot' version '3\.5" "$PROJECT_ROOT/build.gradle"
fi

echo
echo "  passed: $PASS"
echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
