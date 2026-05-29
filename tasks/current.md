# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V226.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 15/15 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓

## 이전 세션에서 멈춘 곳
2026-05-29: 기능 개선 + 문서 최신화 완료. Flyway V226. BUILD SUCCESSFUL.

완료 항목:
- join_date(입사일) 필드: V225 마이그레이션, User 엔티티, 회원가입(선택)·설정 편집·재직증명서 {{입사일}} 연동
- 대시보드(/dashboard): 내 프로젝트 현황 카드(모든 권한), 영업 메뉴 버튼 우선→KPI 아래, KPI 4종 아이콘+컬러 재디자인, 종료임박 프로젝트명 우선 레이아웃, 관리자 메뉴 4종(재직증명서·허용이메일 추가)
- 계정 삭제 FK 전면 수정(V226): project_assignments → SET NULL + user_name 스냅샷(이름 보존), 누락 FK 6개(email_verification_tokens·password_reset_tokens·column_view_preferences CASCADE, resume_templates·document_versions.reviewed_by·documents.deleted_by SET NULL)
- ProjectAssignment.getDisplayName(): user null 시 userName 스냅샷 fallback, 관련 템플릿(dashboard·project-detail) 적용
- 허용 이메일(/admin/allowed-emails): 단건·텍스트 일괄(쉼표·줄바꿈)·엑셀(.xlsx/.xls) 일괄 등록, 메모 제거, 2컬럼 레이아웃·목록 내 검색 필터
- 문서 최신화: tasks/current.md·HARNESS.md·docs/architecture.md·docs/data-model.md·docs/decisions.md·docs/spec.md·docs/frontend.md 전체 업데이트

**다음 작업 없음 — 사용자 지시 대기**
