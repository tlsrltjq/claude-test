#!/usr/bin/env bash
# 21-certificate: 재직증명서 자동 발급 시스템 검증
# 검증 항목:
#   [1] certificate/generate.py 존재 + 핵심 함수 포함
#   [2] certificate/app.py 존재 + Flask 엔드포인트 포함
#   [3] certificate/Dockerfile 존재 + LibreOffice 설치 포함
#   [4] certificate/requirements.txt 존재 (python-docx, flask)
#   [5] certificate/employees.csv 존재 + 이름 컬럼 포함
#   [6] generate.py: --name / --csv / --all / --create 인수 지원
#   [7] generate.py: {{발급일자}} PLACEHOLDERS 정의
#   [8] generate.py: _replace_in_doc — run 분할 대응 로직
#   [9] CertificateController.java 존재 + /admin/certificate 매핑
#  [10] CertificateService.java 존재 + isAvailable / generate / download 메서드
#  [11] templates/admin/certificate.html 존재
#  [12] docker-compose.prod.yml — certificate 서비스 존재
#  [13] docker-compose.yml — certificate 서비스 존재 (개발용)
#  [14] 관리자 nav 15개 템플릿 모두 재직증명서 링크 포함
#  [15] Docker 런타임: 컨테이너 실행 및 health 확인 (선택적)
set -uo pipefail

PASS=0; WARN=0; FAIL=0
STAGE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$STAGE_DIR/../../../.." && pwd)"
SRC="$ROOT/src"
CERT_DIR="$ROOT/certificate"

p() { echo "[PASS] $*"; ((PASS++)); }
w() { echo "[WARN] $*"; ((WARN++)); }
f() { echo "[FAIL] $*"; ((FAIL++)); }

echo "=== 21-certificate: 재직증명서 자동 발급 시스템 ==="
echo "ROOT: $ROOT"
echo ""

# [1] generate.py
GEN="$CERT_DIR/generate.py"
if [ -f "$GEN" ]; then
    FUNCS=$(grep -c "def generate_one\|def generate_all\|def generate_from_csv\|def create_template" "$GEN" 2>/dev/null || echo 0)
    if [ "${FUNCS:-0}" -ge 4 ]; then
        p "[1] generate.py 존재 + 핵심 함수 4개 포함"
    else
        w "[1] generate.py 존재하지만 핵심 함수 부족 (${FUNCS}/4)"
    fi
else
    f "[1] certificate/generate.py 없음"
fi

# [2] app.py Flask 엔드포인트
APP="$CERT_DIR/app.py"
if [ -f "$APP" ]; then
    ROUTES=$(grep -c "@app.route" "$APP" 2>/dev/null || echo 0)
    if [ "${ROUTES:-0}" -ge 5 ]; then
        p "[2] app.py 존재 + Flask 라우트 ${ROUTES}개 포함"
    else
        w "[2] app.py 존재하지만 라우트 부족 (${ROUTES}개)"
    fi
else
    f "[2] certificate/app.py 없음"
fi

# [3] Dockerfile + LibreOffice
DOCKERFILE="$CERT_DIR/Dockerfile"
if [ -f "$DOCKERFILE" ]; then
    if grep -q "libreoffice" "$DOCKERFILE" 2>/dev/null; then
        p "[3] Dockerfile 존재 + LibreOffice 설치 포함"
    else
        f "[3] Dockerfile 존재하지만 LibreOffice 설치 없음"
    fi
else
    f "[3] certificate/Dockerfile 없음"
fi

# [4] requirements.txt
REQ="$CERT_DIR/requirements.txt"
if [ -f "$REQ" ]; then
    HAS_DOCX=$(grep -c "python-docx" "$REQ" 2>/dev/null || echo 0)
    HAS_FLASK=$(grep -c "flask" "$REQ" 2>/dev/null || echo 0)
    if [ "${HAS_DOCX:-0}" -ge 1 ] && [ "${HAS_FLASK:-0}" -ge 1 ]; then
        p "[4] requirements.txt — python-docx + flask 포함"
    else
        w "[4] requirements.txt 존재하지만 의존성 불완전"
    fi
else
    f "[4] certificate/requirements.txt 없음"
fi

# [5] employees.csv
CSV="$CERT_DIR/employees.csv"
if [ -f "$CSV" ]; then
    if grep -q "이름" "$CSV" 2>/dev/null; then
        p "[5] employees.csv 존재 + 이름 컬럼 포함"
    else
        w "[5] employees.csv 존재하지만 이름 컬럼 없음"
    fi
else
    f "[5] certificate/employees.csv 없음"
fi

# [6] argparse 4개 인수
if [ -f "$GEN" ]; then
    ARGS=$(grep -c "\-\-name\|\-\-csv\|\-\-all\|\-\-create" "$GEN" 2>/dev/null || echo 0)
    if [ "${ARGS:-0}" -ge 4 ]; then
        p "[6] generate.py — --name / --csv / --all / --create 인수 지원"
    else
        f "[6] generate.py — 인수 지원 불완전 (${ARGS}/4)"
    fi
fi

# [7] PLACEHOLDERS 딕셔너리
if [ -f "$GEN" ]; then
    if grep -q "PLACEHOLDERS\|발급일자" "$GEN" 2>/dev/null; then
        p "[7] generate.py — {{발급일자}} PLACEHOLDERS 정의"
    else
        f "[7] generate.py — PLACEHOLDERS 딕셔너리 없음"
    fi
fi

# [8] run 분할 대응 (_replace_in_doc)
if [ -f "$GEN" ]; then
    if grep -q "_replace_in_doc\|join.*runs\|enumerate.*runs" "$GEN" 2>/dev/null; then
        p "[8] generate.py — run 분할 대응 치환 로직 존재"
    else
        w "[8] generate.py — run 분할 대응 로직 불명확"
    fi
fi

# [9] CertificateController
CTRL=$(find "$SRC" -name "CertificateController.java" 2>/dev/null | head -1)
if [ -n "$CTRL" ]; then
    if grep -q "/admin/certificate" "$CTRL" 2>/dev/null; then
        p "[9] CertificateController.java — /admin/certificate 매핑 확인"
    else
        f "[9] CertificateController.java — 매핑 없음"
    fi
else
    f "[9] CertificateController.java 없음"
fi

# [10] CertificateService
SVC=$(find "$SRC" -name "CertificateService.java" 2>/dev/null | head -1)
if [ -n "$SVC" ]; then
    COUNT=$(grep -c "isAvailable\|generate\|download" "$SVC" 2>/dev/null || echo 0)
    if [ "${COUNT:-0}" -ge 3 ]; then
        p "[10] CertificateService.java — 주요 메서드 포함"
    else
        w "[10] CertificateService.java 존재하지만 메서드 부족"
    fi
else
    f "[10] CertificateService.java 없음"
fi

# [11] certificate.html 템플릿
TMPL="$SRC/main/resources/templates/admin/certificate.html"
if [ -f "$TMPL" ]; then
    p "[11] templates/admin/certificate.html 존재"
else
    f "[11] templates/admin/certificate.html 없음"
fi

# [12] docker-compose.prod.yml certificate 서비스
PROD_COMPOSE="$ROOT/docker-compose.prod.yml"
if [ -f "$PROD_COMPOSE" ] && grep -q "certificate" "$PROD_COMPOSE" 2>/dev/null; then
    p "[12] docker-compose.prod.yml — certificate 서비스 존재"
else
    f "[12] docker-compose.prod.yml — certificate 서비스 없음"
fi

# [13] docker-compose.yml certificate 서비스 (개발)
DEV_COMPOSE="$ROOT/docker-compose.yml"
if [ -f "$DEV_COMPOSE" ] && grep -q "certificate" "$DEV_COMPOSE" 2>/dev/null; then
    p "[13] docker-compose.yml — certificate 서비스 존재 (개발)"
else
    f "[13] docker-compose.yml — certificate 서비스 없음"
fi

# [14] 관리자 nav 15개 템플릿 모두 링크 포함
TMPL_DIR="$SRC/main/resources/templates/admin"
TOTAL=0; LINKED=0
for html in "$TMPL_DIR"/*.html; do
    ((TOTAL++))
    if grep -q "admin/certificate" "$html" 2>/dev/null; then
        ((LINKED++))
    fi
done
if [ "$LINKED" -eq "$TOTAL" ]; then
    p "[14] 관리자 nav — 전체 ${TOTAL}개 템플릿에 재직증명서 링크 포함"
else
    w "[14] 관리자 nav — 일부 템플릿에 링크 없음 (${LINKED}/${TOTAL})"
fi

# [15] Docker 런타임 헬스체크 (컨테이너 실행 중인 경우만)
if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "resourcehub-certificate"; then
    HEALTH=$(curl -s --max-time 3 http://localhost:5001/health 2>/dev/null || echo "")
    if echo "$HEALTH" | grep -q '"ok"'; then
        p "[15] Docker 컨테이너 실행 중 + /health 응답 정상"
    else
        w "[15] 컨테이너 실행 중이지만 /health 응답 없음"
    fi
else
    w "[15] certificate 컨테이너 미실행 — 런타임 검사 건너뜀"
fi

echo ""
echo "══════════════════════════════════════"
echo "21-certificate 결과: PASS=${PASS}  WARN=${WARN}  FAIL=${FAIL}"
echo "══════════════════════════════════════"
[ "$FAIL" -eq 0 ]
