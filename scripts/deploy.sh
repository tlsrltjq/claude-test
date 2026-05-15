#!/usr/bin/env bash
# ── eActive Resource Hub — 운영 배포 스크립트 ─────────────────
# 반드시 이 스크립트로 배포하세요. docker-compose.yml(개발용)을 직접 사용하지 마세요.
# 사용법: bash scripts/deploy.sh
set -euo pipefail

COMPOSE_FILE="docker-compose.prod.yml"
RED='\033[0;31m'
YLW='\033[0;33m'
GRN='\033[0;32m'
NC='\033[0m'

echo "================================================"
echo "  eActive Resource Hub — 운영 배포"
echo "  compose: $COMPOSE_FILE"
echo "================================================"

# ── 1. 필수 환경변수 검증 ────────────────────────────────────
MISSING=()

check_var() {
    local var="$1"
    local val="${!var:-}"
    if [[ -z "$val" ]]; then
        MISSING+=("$var")
    fi
}

check_var POSTGRES_PASSWORD
check_var RESOURCEHUB_ADMIN_PASSWORD
check_var CADDY_DOMAIN

if [[ ${#MISSING[@]} -gt 0 ]]; then
    echo -e "${RED}[오류] 아래 필수 환경변수가 설정되지 않았습니다:${NC}"
    for v in "${MISSING[@]}"; do
        echo "  - $v"
    done
    echo ""
    echo ".env 파일을 확인하거나 환경변수를 설정한 후 다시 실행하세요."
    exit 1
fi

# ── 2. 테스트 계정 시드 경고 ─────────────────────────────────
SEED_PW="${RESOURCEHUB_SEED_TEST_PASSWORD:-}"
if [[ -n "$SEED_PW" ]]; then
    echo -e "${RED}[보안 경고] RESOURCEHUB_SEED_TEST_PASSWORD 가 설정되어 있습니다!${NC}"
    echo "  운영 환경에서는 이 변수를 반드시 제거하세요."
    echo "  설정 시 SALES 권한 테스트 계정이 자동 생성됩니다."
    echo ""
    read -r -p "그래도 계속 진행하시겠습니까? (yes 입력 시 진행): " confirm
    if [[ "$confirm" != "yes" ]]; then
        echo "배포를 중단합니다."
        exit 1
    fi
fi

echo ""
echo -e "${GRN}[1/3] 환경변수 검증 통과${NC}"
echo "  CADDY_DOMAIN = $CADDY_DOMAIN"
echo "  POSTGRES_PASSWORD = (설정됨)"
echo "  RESOURCEHUB_ADMIN_PASSWORD = (설정됨)"
echo ""

# ── 3. 빌드 및 배포 ──────────────────────────────────────────
echo -e "${GRN}[2/3] Docker 이미지 빌드 및 컨테이너 기동${NC}"
docker compose -f "$COMPOSE_FILE" up -d --build

echo ""
echo -e "${GRN}[3/3] 헬스체크 대기 (최대 120초)${NC}"
for i in $(seq 1 24); do
    sleep 5
    if docker compose -f "$COMPOSE_FILE" exec -T app \
           wget -qO- http://localhost:8080/health > /dev/null 2>&1; then
        echo -e "${GRN}헬스체크 통과!${NC}"
        break
    fi
    echo "  대기 중... (${i}/24)"
    if [[ $i -eq 24 ]]; then
        echo -e "${YLW}[경고] 120초 내 헬스체크 미통과. 로그를 확인하세요:${NC}"
        echo "  docker compose -f $COMPOSE_FILE logs app"
    fi
done

echo ""
echo "================================================"
echo -e "${GRN}배포 완료${NC}"
echo "  서비스 상태 확인: docker compose -f $COMPOSE_FILE ps"
echo "  앱 로그 확인:     docker compose -f $COMPOSE_FILE logs -f app"
echo "  Caddy 로그 확인:  docker compose -f $COMPOSE_FILE logs -f caddy"
echo "================================================"
