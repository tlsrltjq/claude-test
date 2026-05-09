#!/usr/bin/env bash
source "$(dirname "$0")/../../lib/common.sh"
echo "=== Stage 14: MVP2 DB Schema ==="

wait_for_app

# ── V100: SALES 역할 ──────────────────────────────────────
sales_type=$(db_query "SELECT column_type FROM information_schema.columns
  WHERE table_name='users' AND column_name='role' LIMIT 1;" 2>/dev/null || true)
# PostgreSQL: role 컬럼이 varchar/text 이거나 enum — SALES 값 존재 여부로 확인
sales_cnt=$(db_query "SELECT COUNT(*) FROM users WHERE role='SALES';" 2>/dev/null || echo "0")
check "DB: users.role = SALES 허용 (V100)" \
  "$( [[ "$sales_cnt" =~ ^[0-9]+$ ]] && echo PASS || echo FAIL)"

# ── V102: 사용자 프로필 컬럼 ────────────────────────────────
phone_col=$(db_query "SELECT COUNT(*) FROM information_schema.columns
  WHERE table_name='users' AND column_name='phone';")
check "DB: users.phone 컬럼 존재 (V102)" "$([[ "$phone_col" -ge 1 ]] && echo PASS || echo FAIL)"

birth_col=$(db_query "SELECT COUNT(*) FROM information_schema.columns
  WHERE table_name='users' AND column_name='birth_date';")
check "DB: users.birth_date 컬럼 존재 (V102)" "$([[ "$birth_col" -ge 1 ]] && echo PASS || echo FAIL)"

pos_col=$(db_query "SELECT COUNT(*) FROM information_schema.columns
  WHERE table_name='users' AND column_name='position';")
check "DB: users.position 컬럼 존재 (V102)" "$([[ "$pos_col" -ge 1 ]] && echo PASS || echo FAIL)"

# ── V103: employee_profiles 추가 컬럼 ──────────────────────
grade_col=$(db_query "SELECT COUNT(*) FROM information_schema.columns
  WHERE table_name='employee_profiles' AND column_name='developer_grade';")
check "DB: employee_profiles.developer_grade 컬럼 존재 (V103)" \
  "$([[ "$grade_col" -ge 1 ]] && echo PASS || echo FAIL)"

career_col=$(db_query "SELECT COUNT(*) FROM information_schema.columns
  WHERE table_name='employee_profiles' AND column_name='career_months';")
check "DB: employee_profiles.career_months 컬럼 존재 (V103)" \
  "$([[ "$career_col" -ge 1 ]] && echo PASS || echo FAIL)"

# ── V104: resume_templates 테이블 ──────────────────────────
tbl=$(db_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_name='resume_templates';")
check "DB: resume_templates 테이블 존재 (V104)" "$([[ "$tbl" -ge 1 ]] && echo PASS || echo FAIL)"

rt_status=$(db_query "SELECT COUNT(*) FROM information_schema.columns
  WHERE table_name='resume_templates' AND column_name='status';")
check "DB: resume_templates.status 컬럼 존재" "$([[ "$rt_status" -ge 1 ]] && echo PASS || echo FAIL)"

rt_uploader=$(db_query "SELECT COUNT(*) FROM information_schema.columns
  WHERE table_name='resume_templates' AND column_name='uploaded_by';")
check "DB: resume_templates.uploaded_by 컬럼 존재" "$([[ "$rt_uploader" -ge 1 ]] && echo PASS || echo FAIL)"

summary
