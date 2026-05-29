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
2026-05-29: 버그 수정 + 기능 개선 + 문서 최신화 완료. Flyway V226. BUILD SUCCESSFUL.

완료 항목:
- fix: 대시보드 LazyInitializationException — ProjectAssignmentRepository.findByUserId에 JOIN FETCH pa.project 추가 (사원 500 오류·영업 빈화면 해결)
- feat: 파일 크기 임계값 10MB→20MB (20MB 이하 즉시 승인, 초과 시 관리자 검토)
- feat: multipart max-file-size 100MB로 확장, max-request-size 200MB
- feat: zip·xlsx·xls 확장자 허용 추가 (magic bytes 검증 포함)
- docs: architecture·data-model·decisions·spec·frontend 전체 최신화 (V226 기준, ADR-039 추가)
- chore: tasks/current.md 간소화

**다음 작업 없음 — 사용자 지시 대기**
