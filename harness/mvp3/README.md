# mvp3/harness

mvp1·mvp2 와 동일한 하네스. **차이점**:

- 회귀 검사 옵션 두 개: `--with-mvp1`, `--with-mvp2`. 큰 변경 후엔 둘 다 켜고 돌리기 권장.
- start.sh 헤더가 MVP3 SSOT(PROJECT_SPEC_MVP3 / DECISIONS / STAGE_PLAN / MIGRATION_FROM_MVP2)를 참조.
- DB 마이그레이션은 **V200~** 만 사용.

## 사용 흐름 (단계 prompt 채워진 후)

```bash
cd ~/Desktop/ai_eactive_hub/mvp3
bash harness/scripts/status.sh
bash harness/scripts/start.sh M3-01
bash harness/scripts/verify.sh M3-01 --with-mvp1 --with-mvp2
bash harness/scripts/log.sh M3-01 "..."
```

## 주의

`stages/` 폴더는 **현재 비어 있음.** `docs/DECISIONS.md` 와 `docs/STAGE_PLAN.md` 를 사용자가 컨펌한 뒤 13개 단계 폴더를 채워 넣는다.
