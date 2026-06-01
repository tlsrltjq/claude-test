# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V227.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 18/18 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓

## 이전 세션에서 멈춘 곳
2026-06-01: 프로젝트 등록 전용 페이지 신설 완료.

완료 항목:
- feat: GET /sales/projects/new 엔드포인트 추가 (ProjectController)
- feat: project-new.html 신규 — 좌우 2단 레이아웃
  · 왼쪽: 프로젝트 정보 폼 + 선택된 직원 태그 요약
  · 오른쪽: 이름 검색 + 팀 필터 + 팀별 그룹 헤더 + 전체 선택 버튼
  · 직원 목록 max-height 420px (모달 대비 3배 공간)
- feat: calendar.html — "프로젝트 등록" 버튼 → /sales/projects/new 링크 교체
  기존 모달 HTML + 관련 JS 제거
- feat: 직원 추가 모달 (project-detail.html) — select → 검색형 리스트 전환
- feat: 인력 현황 탭 — 팀 필터 select 추가
- 전체 빌드: BUILD SUCCESSFUL, security-lint 18/18 PASS

**다음 작업 없음 — 사용자 지시 대기**
