# CLAUDE.md
# Claude Code / Codex / 모든 AI 에이전트가 세션 시작 시 자동으로 읽는 파일

## 세션 시작 시 필수 작업 (자동 실행)
1. `HARNESS.md` 읽기
2. `tasks/current.md` 확인
3. 현재 단계와 목표를 한 줄로 요약 출력:
   `현재 단계: [단계명] / 목표: [목표] / 완료 기준: [기준]`
4. 위 확인 완료 후 작업 시작

## 세션 종료 시 필수 작업 (사람이 "세션 종료" 지시 후 자동 실행)
1. `HARNESS.md` "현재 상태" 섹션 갱신
2. `CHANGELOG.md` 한 줄 추가 (형식: `YYYY-MM-DD | 단계 | feat/fix/chore/docs: 내용`)
3. `tasks/current.md` "이전 세션에서 멈춘 곳" 갱신
4. 변경 사항 git commit & push (`origin/main`)
5. 도커 재빌드 및 재기동: `docker compose build --no-cache app && docker compose up -d app`

## 작업 중 행동 규칙
- 코드 수정 전: 영향 받는 파일 목록 먼저 보고
- 새 패키지/의존성 추가 전: 반드시 사람에게 확인 요청
- 수정 금지 폴더·파일 접촉 시: 반드시 사람에게 확인 요청
  - `src/main/resources/db/migration/V*.sql` — 이미 적용된 Flyway 번호는 절대 수정 금지 (새 번호만 추가)
  - `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java` — 보안 핵심
  - `.env`, `.env.example` — 시크릿
  - `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml` — 운영 인프라
  - `harness/archive/legacy/**` — 옛 하네스 보존 (읽기 전용)
- 완료 기준 달성 시: 멈추고 결과 보고 (추가 구현 금지)
- 불확실한 부분: 가정하고 진행하지 말고 질문할 것
- 커밋 메시지 접두어: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`

## 검증 명령 (작업 후 권장 실행)
- `bash scripts/security-lint.sh` — 보안 정적 분석 (15개 항목, 0 FAIL 유지)
- `./gradlew build` — 컴파일·테스트 (BUILD SUCCESSFUL 유지)

## 참고 파일 위치
- 전체 요약: `HARNESS.md`
- 시스템 상세: `docs/architecture.md`
- 기술 결정: `docs/decisions.md`
- 현재 작업: `tasks/current.md`
- 변경 이력: `CHANGELOG.md`
- 과거 자료: `docs/archive/`, `harness/archive/legacy/`
