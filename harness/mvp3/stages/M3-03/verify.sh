#!/usr/bin/env bash
set -u
PASS=0; FAIL=0
HARNESS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_ROOT="$(cd "$HARNESS_DIR/../.." && pwd)"
RES="$PROJECT_ROOT/src/main/resources"
SRC="$PROJECT_ROOT/src/main/java/com/eactive/resourcehub"
check() { local d="$1"; shift; if "$@" >/dev/null 2>&1; then echo "  [PASS] $d"; PASS=$((PASS+1)); else echo "  [FAIL] $d"; FAIL=$((FAIL+1)); fi; }

echo "== MVP3 M3-03 — /signup 폼 정비 =="
echo

S="$RES/templates/signup.html"
if [ -f "$S" ]; then
  check "birth_date 8자 input"   bash -c "grep -qE 'maxlength=\"8\"|pattern=\"[\\\\d]\\{8\\}\"' '$S'"
  check "생년월일 placeholder"    bash -c "grep -qE 'placeholder=\"[0-9]{8}\"|20010904' '$S'"
  check "이메일 suffix 표기"      bash -c "grep -qE 'eactive.co.kr|company-email-domain' '$S'"

  # 입력 순서 (Python이 input name 추출)
  python3 - "$S" <<'PY'
import re, sys
html = open(sys.argv[1]).read()
names = re.findall(r'<input[^>]+name="([^"]+)"', html, flags=re.IGNORECASE)
sels  = re.findall(r'<select[^>]+name="([^"]+)"', html, flags=re.IGNORECASE)
order = []
# 단순히 등장 순서를 모두 모은다
for m in re.finditer(r'<(input|select)[^>]+name="([^"]+)"', html, flags=re.IGNORECASE):
    order.append(m.group(2))
# 의도된 순서 — 이름/생년월일/연락처/이메일/팀/직급/비밀번호 키워드 매핑
expected = ['name','birth','phone','email','team','position','password']
indices = []
for k in expected:
    idx = next((i for i,n in enumerate(order) if k.lower() in n.lower()), -1)
    indices.append(idx)
ok = all(x != -1 for x in indices) and indices == sorted(indices)
print("  [PASS] signup form field order" if ok else f"  [FAIL] signup form field order: order={order}")
PY
fi

echo; echo "  passed: $PASS"; echo "  failed: $FAIL"
[ "$FAIL" -eq 0 ]
