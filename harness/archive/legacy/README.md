# harness/archive/legacy/

`범용 하네스 양식` 으로 전면 개편하면서 보관용으로 옮긴 옛 검증 하네스. 회귀 검사가 필요하면 여기서 꺼내 쓸 수 있도록 구조를 유지한다.

| 디렉토리 | 시기 | 비고 |
|----------|------|------|
| `mvp1/` | ~2026-05-01 | MVP1 단계별 verify.sh |
| `mvp2/` | ~2026-05-05 | MVP2 단계별 verify.sh |
| `mvp3/` | ~2026-05-15 | MVP3 + post-MVP3 + 19/20/21 단계 verify.sh (`scripts/verify.sh`, `state/progress.json` 포함) |
| `stages/` | 초기 시도 | mvp1 보다 앞서 만들어졌던 검증 시도 (현재는 미사용) |
| `lib/` | 공용 | 옛 검증 스크립트 공통 함수 (`common.sh`) |
| `run_all.sh` | 공용 | `stages/*/verify.sh` 일괄 실행 (현재는 미사용) |

> 현재 활성 검증 도구는 루트의 `scripts/security-lint.sh` 와 `./gradlew build` 뿐이다. 운영 가이드는 `HARNESS.md` 와 `tasks/current.md` 참조.
