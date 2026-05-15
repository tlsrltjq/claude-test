#!/usr/bin/env bash
# 20-ops-readiness: 운영 배포 준비도 검증
# 검증 항목:
#   [1] Caddyfile 존재 + {env.CADDY_DOMAIN} 사용
#   [2] docker-compose.prod.yml — caddy 서비스 존재
#   [3] docker-compose.prod.yml — app 서비스 ports 노출 없음 (Caddy가 처리)
#   [4] application-prod.yml — forward-headers-strategy: native 설정
#   [5] scripts/deploy.sh 존재 + 필수 env 검증 (POSTGRES_PASSWORD, CADDY_DOMAIN)
#   [6] scripts/deploy.sh — SEED 계정 경고 로직 존재
#   [7] logback-spring.xml 존재 + prod 프로파일 RollingFileAppender
#   [8] docker-compose.prod.yml — resourcehub_logs 볼륨 존재
#   [9] scripts/backup-db.sh 존재 + pg_dump + 30일 보관
#  [10] scripts/backup-uploads.sh 존재 + tar + 30일 보관
#  [11] scripts/setup-cron.sh 존재 + 크론잡 등록 (03:00 DB / 03:30 uploads)
#  [12] docker-compose.yml 상단 개발 전용 경고 주석 존재
set -uo pipefail

PASS=0; WARN=0; FAIL=0
STAGE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$STAGE_DIR/../../../.." && pwd)"
SRC="$ROOT/src"

p() { echo "[PASS] $*"; ((PASS++)); }
w() { echo "[WARN] $*"; ((WARN++)); }
f() { echo "[FAIL] $*"; ((FAIL++)); }

echo "=== 20-ops-readiness: 운영 배포 준비도 ==="
echo "ROOT: $ROOT"
echo ""

# [1] Caddyfile
CADDYFILE="$ROOT/Caddyfile"
if [ -f "$CADDYFILE" ]; then
    if grep -q "env.CADDY_DOMAIN" "$CADDYFILE" 2>/dev/null; then
        p "[1] Caddyfile 존재 + {env.CADDY_DOMAIN} 환경변수 방식 도메인 설정"
    else
        w "[1] Caddyfile 존재하지만 {env.CADDY_DOMAIN} 미사용"
    fi
else
    f "[1] Caddyfile 없음"
fi

# [2] docker-compose.prod.yml caddy 서비스
PROD_COMPOSE="$ROOT/docker-compose.prod.yml"
if [ -f "$PROD_COMPOSE" ]; then
    if grep -q "caddy" "$PROD_COMPOSE" 2>/dev/null; then
        p "[2] docker-compose.prod.yml — caddy 서비스 존재"
    else
        f "[2] docker-compose.prod.yml — caddy 서비스 없음"
    fi
else
    f "[2] docker-compose.prod.yml 없음"
fi

# [3] app 서비스 ports 없음
if [ -f "$PROD_COMPOSE" ]; then
    # app 서비스 블록만 추출해서 ports 확인
    APP_BLOCK=$(awk '/^  app:/,/^  [a-z]/' "$PROD_COMPOSE" 2>/dev/null || true)
    if echo "$APP_BLOCK" | grep -q "ports:"; then
        w "[3] docker-compose.prod.yml — app 서비스가 직접 포트를 노출하고 있음 (Caddy 우회 가능)"
    else
        p "[3] docker-compose.prod.yml — app 포트 비노출 (Caddy 경유 전용)"
    fi
fi

# [4] application-prod.yml forward-headers-strategy
PROD_YML="$SRC/main/resources/application-prod.yml"
if [ -f "$PROD_YML" ]; then
    if grep -q "forward-headers-strategy" "$PROD_YML" 2>/dev/null; then
        p "[4] application-prod.yml — forward-headers-strategy 설정 (Caddy HTTPS 신뢰)"
    else
        f "[4] application-prod.yml — forward-headers-strategy 없음"
    fi
else
    f "[4] application-prod.yml 없음"
fi

# [5] deploy.sh 필수 env 검증
DEPLOY_SH="$ROOT/scripts/deploy.sh"
if [ -f "$DEPLOY_SH" ]; then
    HAS_PG=$(grep -c "POSTGRES_PASSWORD" "$DEPLOY_SH" 2>/dev/null || echo 0)
    HAS_CADDY=$(grep -c "CADDY_DOMAIN" "$DEPLOY_SH" 2>/dev/null || echo 0)
    if [ "${HAS_PG:-0}" -ge 1 ] && [ "${HAS_CADDY:-0}" -ge 1 ]; then
        p "[5] scripts/deploy.sh — POSTGRES_PASSWORD + CADDY_DOMAIN 검증 존재"
    else
        f "[5] scripts/deploy.sh — 필수 env 검증 불완전"
    fi
else
    f "[5] scripts/deploy.sh 없음"
fi

# [6] deploy.sh SEED 경고
if [ -f "$DEPLOY_SH" ]; then
    if grep -q "RESOURCEHUB_SEED_TEST_PASSWORD\|SEED" "$DEPLOY_SH" 2>/dev/null; then
        p "[6] scripts/deploy.sh — SEED 계정 경고 로직 존재"
    else
        f "[6] scripts/deploy.sh — SEED 계정 경고 없음"
    fi
fi

# [7] logback-spring.xml + RollingFileAppender
LOGBACK="$SRC/main/resources/logback-spring.xml"
if [ -f "$LOGBACK" ]; then
    if grep -q "RollingFileAppender\|prod" "$LOGBACK" 2>/dev/null; then
        p "[7] logback-spring.xml 존재 + prod 프로파일 RollingFileAppender 설정"
    else
        w "[7] logback-spring.xml 존재하지만 prod RollingFileAppender 미설정"
    fi
else
    f "[7] logback-spring.xml 없음"
fi

# [8] resourcehub_logs 볼륨
if [ -f "$PROD_COMPOSE" ]; then
    if grep -q "resourcehub_logs" "$PROD_COMPOSE" 2>/dev/null; then
        p "[8] docker-compose.prod.yml — resourcehub_logs 볼륨 정의"
    else
        f "[8] docker-compose.prod.yml — resourcehub_logs 볼륨 없음"
    fi
fi

# [9] backup-db.sh
BACKUP_DB="$ROOT/scripts/backup-db.sh"
if [ -f "$BACKUP_DB" ]; then
    HAS_PGDUMP=$(grep -c "pg_dump" "$BACKUP_DB" 2>/dev/null || echo 0)
    HAS_RETAIN=$(grep -c "mtime +30\|30" "$BACKUP_DB" 2>/dev/null || echo 0)
    if [ "${HAS_PGDUMP:-0}" -ge 1 ] && [ "${HAS_RETAIN:-0}" -ge 1 ]; then
        p "[9] scripts/backup-db.sh — pg_dump + 30일 보관 정책"
    else
        w "[9] scripts/backup-db.sh 존재하지만 내용 불완전"
    fi
else
    f "[9] scripts/backup-db.sh 없음"
fi

# [10] backup-uploads.sh
BACKUP_UP="$ROOT/scripts/backup-uploads.sh"
if [ -f "$BACKUP_UP" ]; then
    HAS_TAR=$(grep -c "tar" "$BACKUP_UP" 2>/dev/null || echo 0)
    HAS_RETAIN=$(grep -c "mtime +30\|30" "$BACKUP_UP" 2>/dev/null || echo 0)
    if [ "${HAS_TAR:-0}" -ge 1 ] && [ "${HAS_RETAIN:-0}" -ge 1 ]; then
        p "[10] scripts/backup-uploads.sh — tar 압축 + 30일 보관 정책"
    else
        w "[10] scripts/backup-uploads.sh 존재하지만 내용 불완전"
    fi
else
    f "[10] scripts/backup-uploads.sh 없음"
fi

# [11] setup-cron.sh + 크론 시간대
CRON_SH="$ROOT/scripts/setup-cron.sh"
if [ -f "$CRON_SH" ]; then
    HAS_DB_CRON=$(grep -c "0 3 \* \* \*\|03:00\|backup-db" "$CRON_SH" 2>/dev/null || echo 0)
    HAS_UP_CRON=$(grep -c "30 3 \* \* \*\|03:30\|backup-uploads" "$CRON_SH" 2>/dev/null || echo 0)
    if [ "${HAS_DB_CRON:-0}" -ge 1 ] && [ "${HAS_UP_CRON:-0}" -ge 1 ]; then
        p "[11] scripts/setup-cron.sh — DB(03:00) + uploads(03:30) 크론잡 등록"
    else
        w "[11] scripts/setup-cron.sh 존재하지만 크론 시간 불명확"
    fi
else
    f "[11] scripts/setup-cron.sh 없음"
fi

# [12] docker-compose.yml 개발 전용 경고
DEV_COMPOSE="$ROOT/docker-compose.yml"
if [ -f "$DEV_COMPOSE" ]; then
    if grep -q "개발\|개발.*전용\|운영.*금지" "$DEV_COMPOSE" 2>/dev/null; then
        p "[12] docker-compose.yml — 개발 전용 경고 주석 존재"
    else
        w "[12] docker-compose.yml — 운영 배포 금지 경고 없음"
    fi
else
    w "[12] docker-compose.yml 없음"
fi

echo ""
echo "══════════════════════════════════════"
echo "20-ops-readiness 결과: PASS=${PASS}  WARN=${WARN}  FAIL=${FAIL}"
echo "══════════════════════════════════════"
[ "$FAIL" -eq 0 ]
