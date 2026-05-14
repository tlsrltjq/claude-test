#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 12 — 계정 활성/비활성 토글 + 즉시 세션 무효화 =="
echo

# SecurityConfig: SessionRegistry 빈
SEC="$SRC/common/security/SecurityConfig.java"
check "SecurityConfig: SessionRegistry 빈"         grep -q 'SessionRegistry'          "$SEC"
check "SecurityConfig: HttpSessionEventPublisher"  grep -q 'HttpSessionEventPublisher' "$SEC"
check "SecurityConfig: maximumSessions 설정"       grep -q 'maximumSessions'          "$SEC"
check "SecurityConfig: expiredUrl (/login?expired)" grep -q 'expiredUrl'              "$SEC"

# 서비스
EMS="$SRC/user/service/EmployeeManagementService.java"
check "EmployeeManagementService: toggleStatus 메서드"  grep -q 'toggleStatus'   "$EMS"
check "toggleStatus: ADMIN 계정 차단"                   grep -q 'ADMIN'          "$EMS"
check "toggleStatus: activate() 호출"                   grep -q 'activate'       "$EMS"
check "toggleStatus: disable() 호출"                    grep -q 'disable'        "$EMS"

# UserRepository: ACTIVE+DISABLED 복합 조회
UR="$SRC/user/repository/UserRepository.java"
check "UserRepository: findByStatusInWithTeam"  grep -q 'findByStatusInWithTeam' "$UR"

# 컨트롤러 엔드포인트 + 세션 만료
CTRL="$SRC/user/controller/AdminController.java"
check "POST /admin/employees/{id}/toggle-status"  grep -q 'toggle-status' "$CTRL"
check "SessionInformation.expireNow() 호출"       grep -q 'expireNow'     "$CTRL"
check "AdminController: SessionRegistry 주입"     grep -q 'SessionRegistry' "$CTRL"

# 감사 로그
ACT="$SRC/audit/entity/AuditActionType.java"
check "AuditActionType: DISABLE_USER"  grep -q 'DISABLE_USER' "$ACT"
check "AuditActionType: ENABLE_USER"   grep -q 'ENABLE_USER'  "$ACT"

# 템플릿
check "admin/employees.html: 상태 뱃지"          grep -q '활성\|비활성'       "$RES/templates/admin/employees.html"
check "admin/employees.html: 토글 버튼"          grep -q 'toggle-status'      "$RES/templates/admin/employees.html"
check "admin/employee-detail.html: 토글 버튼"    grep -q 'toggle-status'      "$RES/templates/admin/employee-detail.html"
check "login.html: ?expired 메시지"              grep -q 'param.expired'       "$RES/templates/login.html"

echo; echo "  passed: $PASS  failed: $FAIL"
[ "$FAIL" -eq 0 ]
