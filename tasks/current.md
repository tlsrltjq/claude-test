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
2026-05-31: 직원 문서 카드 레이아웃 통일 완료.

완료 항목:
- fix: admin/employee-documents.html — doc-icon-area 래퍼 적용, 파일명 accent 색상, 날짜 yy.MM.dd, 미리보기·다운로드·삭제 버튼 정비 ("상세" 버튼 제거)
- fix: sales/employee-documents.html — 동일한 카드 구조 적용(doc-icon-area, accent 파일명, yy.MM.dd)
- 전체 빌드: BUILD SUCCESSFUL

**다음 작업 없음 — 사용자 지시 대기**
