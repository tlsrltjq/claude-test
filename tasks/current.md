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
2026-06-01: 프로젝트 캘린더 직원 선택 UX 개선 완료.

완료 항목:
- feat: 프로젝트 등록 모달 (`calendar.html`)
  - 이름 검색 input + 팀 필터 select 추가
  - 체크박스 목록 높이 160→240px, modal-dialog-scrollable
  - 선택 카운터 뱃지 + 선택 취소 태그 (✕ 클릭)
  - 모달 닫힐 때 전체 초기화
- feat: 직원 추가 모달 (`project-detail.html`)
  - `<select>` → 이름 검색 + 팀 필터 + 클릭 선택 리스트 전환
  - 선택된 직원 하이라이트 표시, 미선택 시 추가 버튼 disabled
- feat: 인력 현황 탭 팀 필터 추가 (`calendar.html`)
  - 팀 목록 JS 자동 수집 (서버 요청 없음)
  - data-team 속성 테이블 행 추가
- 서버 코드 변경 없음 — 순수 HTML + JS
- 전체 빌드: BUILD SUCCESSFUL, security-lint 18/18 PASS

**다음 작업 없음 — 사용자 지시 대기**
