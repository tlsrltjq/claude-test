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
2026-05-31: UI 버그 수정·사이드바 정리 세션 완료.

완료 항목:
- fix: GlobalModelAttributeAdvice 추가 — 사이드바 `currentUser` 분리(직원 상세 등 컨트롤러 `user` 모델과 충돌 해결)
- fix: 통계 페이지 다운로드·업로드 이력 테이블 레이아웃 개선(colgroup·members-table·팀 컬럼·sticky thead)
- fix: 직원 문서 목록 테이블 스타일 정비(members-table, 썸네일 140px→36px, colgroup 8컬럼)
- chore: 사이드바 팀 관리·팀 프로젝트 설정 2항목 제거
- 전체 빌드: 393개 전 통과, security-lint 15/15 PASS (변경 내용은 HTML·Java 1개 신규 클래스만)

**다음 작업 없음 — 사용자 지시 대기**
