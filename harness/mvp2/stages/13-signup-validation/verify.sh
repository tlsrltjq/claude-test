#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
RES="$PROJECT_ROOT/src/main/resources"
TMPL="$RES/templates/signup.html"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP2 Stage 13 — 회원가입 폼 빈칸 안내 =="
echo

# SignupRequest 검증 애노테이션
SR="$SRC/user/dto/SignupRequest.java"
check "SignupRequest: birthDateStr @NotBlank"  grep -q '@NotBlank' "$SR" && grep -A2 'birthDateStr' "$SR" | grep -q '@NotBlank\|NotBlank' >/dev/null 2>&1 || bash -c "grep -B2 'birthDateStr' '$SR' | grep -q 'NotBlank'"
check "SignupRequest: birthDateStr @Pattern"   grep -q 'birthDateStr' "$SR" && grep -q '@Pattern' "$SR"
check "SignupRequest: phone @Pattern"          grep -q 'phone' "$SR" && grep -q '@Pattern' "$SR"
check "SignupRequest: emailPrefix @Pattern"    grep -q 'emailPrefix' "$SR" && grep -q '@Pattern' "$SR"

# 컨트롤러: 필드 레벨 검증
CTRL="$SRC/user/controller/SignupController.java"
check "SignupController: isPasswordComplex 메서드"  grep -q 'isPasswordComplex' "$CTRL"
check "SignupController: 복잡도 bindingResult.rejectValue" bash -c "grep -q 'rejectValue.*password\|rejectValue.*complexity' '$CTRL'"
check "SignupController: 불일치 bindingResult.rejectValue" bash -c "grep -q 'rejectValue.*passwordConfirm\|rejectValue.*mismatch' '$CTRL'"

# 템플릿
check "signup.html: novalidate 속성"          grep -q 'novalidate'       "$TMPL"
check "signup.html: was-validated JS"         grep -q 'was-validated'    "$TMPL"
check "signup.html: birthDateStr invalid-feedback" grep -q 'birthDateStr' "$TMPL" && grep -q 'invalid-feedback' "$TMPL"
check "signup.html: th:if hasErrors 패턴"     grep -q 'hasErrors'        "$TMPL"
check "signup.html: th:unless 폴백 안내"      grep -q 'th:unless'        "$TMPL"
check "signup.html: has-validation (input-group)" grep -q 'has-validation' "$TMPL"
check "signup.html: 직급 선택 안내"           grep -q '직급을 선택해주세요' "$TMPL"
check "signup.html: 비밀번호 확인 안내"       grep -q '비밀번호 확인을 입력해주세요' "$TMPL"

echo; echo "  passed: $PASS  failed: $FAIL"
[ "$FAIL" -eq 0 ]
