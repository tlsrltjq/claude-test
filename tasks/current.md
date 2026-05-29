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
2026-05-29: 캘린더 정합성·성능·동의서 구현 완료. Flyway V227.

완료 항목:
- fix: 캘린더 CANCELLED 배정 cascade (ProjectService.update → cancelByProject 일괄 처리)
- fix: memberCounts 부풀림 — countAssignmentsByProject IN ('ACTIVE','PLANNED')으로 수정
- fix: dayMap 미사용 모델 속성 제거 (컨트롤러에서 addAttribute 제거, 메서드는 유지)
- fix: 사이드바 섹션 순서 내 화면→영업·인력→관리자
- perf: ZIP/Excel StreamingResponseBody 스트리밍 (OOM 방지)
- perf: DeployStats COUNT DISTINCT 쿼리 (Java 집계 → DB 위임)
- perf: 문서 검색 업로더·날짜 필터 DB 위임 (Java 후처리 제거)
- perf: PDF 썸네일 temp file + 30MB 크기 제한
- perf: 캘린더 종료 프로젝트 6개월 필터 (getAllNonCancelledProjectsSince)
- perf: Flyway V227 audit_logs 복합 인덱스 2개 추가
- feat: 회원가입 개인정보 동의서 — 7개 섹션 modal-xl 모달, 5체크박스(전체동의 toggle+indeterminate), 내용보기 섹션 스크롤, 폼 제출 시 전체 동의 검증

**다음 작업 없음 — 사용자 지시 대기**
