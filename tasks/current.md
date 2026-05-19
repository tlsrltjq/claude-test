# 현재 작업 컨텍스트

## 지금 단계: 0단계 — 하네스 전면 개편

## 목표
- [x] 기존 `harness/`(mvp1·mvp2·mvp3 stage별 verify.sh + stages 레거시 + run_all.sh + lib) 를 `harness/archive/legacy/` 로 이동
- [x] 기존 `docs/` 의 mvp3-spec / security-policy / improvement-plan / CHANGELOG / 운영 체크리스트 / 배포 체크리스트 / NAS 가이드를 `docs/archive/` 로 이동
- [x] 범용 양식 기반으로 새 파일 작성: `CLAUDE.md`, `HARNESS.md`, `CHANGELOG.md`, `tasks/current.md`, `docs/architecture.md`, `docs/decisions.md`
- [x] 세션 자동화 스크립트 작성: `start_session.sh`, `end_session.sh` (+ `chmod +x`)
- [x] `.gitignore` 에 `.claude/` 추가
- [x] `bash scripts/security-lint.sh` 0 FAIL 재확인
- [x] `bash start_session.sh` 출력 정상 확인
- [x] 사람 검수 후 커밋: `chore: 하네스 전면 개편(범용 양식 도입)`

## 완료 기준
- 루트에 `CLAUDE.md`, `HARNESS.md`, `CHANGELOG.md`, `start_session.sh`, `end_session.sh` 존재
- `docs/` 에 `architecture.md`, `decisions.md`, `archive/` 만 존재
- `tasks/current.md` 가 본 작업으로 채워져 있음
- `harness/` 아래에는 `archive/legacy/`(+ 그 안의 `mvp1`, `mvp2`, `mvp3`, `stages`, `lib`, `run_all.sh`) 만 남음
- `bash scripts/security-lint.sh` PASS 15/15
- `bash start_session.sh` 가 `HARNESS.md` + `tasks/current.md` 를 정상 출력
- 프로덕션 코드(`src/**`)·운영 인프라(`Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`)·`scripts/**` 미수정

## 건드리면 안 되는 파일 (이번 단계)
- `src/**` — 본 단계는 메타 문서만 손봄, 프로덕션 코드 무수정
- `scripts/security-lint.sh`, `scripts/deploy.sh`, `scripts/backup-*.sh`, `scripts/setup-cron.sh`
- `Caddyfile`, `docker-compose.yml`, `docker-compose.prod.yml`, `application-prod.yml`, `logback-spring.xml`
- `harness/archive/legacy/**` — 보존 자료
- `src/main/resources/db/migration/V*.sql`

## 이전 세션에서 멈춘 곳
2026-05-19 세션에서 소스코드 기준 하네스·문서·린트 전수 정합성 감사 완료. 수행 내역:
- `templates/team/` 빈 폴더 삭제
- `security-lint.sh` 15/15 PASS, `start_session.sh` 정상 출력 확인 → 0단계 전 체크리스트 완료 커밋
- 발표 브랜치(`presentation`) 생성 → 발표 자료 markdown 8종 작성 → 보안 위반 3건 시연(FAIL→수정→PASS) → 브랜치 삭제
- `harness/archive/legacy/` + git CLAUDE.md 히스토리 5세대 분석 (발표 자료 참고용)
- 코드 규모 집계: 소스 18,217줄 / 문서 포함 26,945줄
- 잔여 불일치: `architecture.md` 에 SalesMemberService·ProfileRow·audit/dto 미기재 (낮은 우선순위)

## 다음 단계 예고
- 운영 도메인 배포(`scripts/deploy.sh`) 또는 신규 기능 도입
- 선택적: architecture.md 누락 클래스 보완
- 새 단계 시작 시 본 파일을 새 내용으로 덮어쓰기
