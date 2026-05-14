# Harness — MVP2 작업 환경

mvp2.pdf의 MVP2 기능을 단계별로 진행하기 위한 하네스. 사용 방법은 `mvp1/harness/README.md`와 동일.

## 차이점 (mvp1과 비교)

- **SSOT**: `mvp2/docs/PROJECT_SPEC_MVP2.md` + `MIGRATION_FROM_MVP1.md`
- **stage 폴더**: `01-permissions ~ 10-bundle-template` (MVP2 단계 ID)
- **progress.json**: MVP2 전용
- **회귀 검사**: `verify.sh ... --with-mvp1` 옵션으로 mvp1 verify를 함께 실행
- **start.sh 헤더**: MVP2 SSOT를 우선 가리키며, 권한이 ADMIN/SALES/EMPLOYEE 3개로 단순화됐음을 명시

## 단계 (mvp2.pdf §11)

### 1차 (영업부가 실제로 쓰는 화면)
1. `01-permissions` — TEAM_LEADER → SALES, 라우팅 변경 (V100)
2. `02-download-policy` — 사유 화면 제거, 바로 다운로드, 관리자 삭제 (V101)
3. `03-profile-fields` — position enum, birth_date, phone, 이메일 자동 조합, 비밀번호 정책 (V102)
4. `04-sales-profiles` — `/sales/profiles` 14컬럼 표 (V103)
5. `05-search-filter-sort` — 검색·필터·정렬·컬럼 토글
6. `06-resume-template` — 양식 이력서 (V104)
7. `07-career-calculator` — 단독 경력 계산기

### 2차
8. `08-excel-export` — 엑셀 내보내기
9. `09-career-save` — 계산기 → 프로필 저장 연동 (V105)
10. `10-bundle-template` — 투입인력서 묶음 (V106~)

## 워크플로우

```bash
cd ~/Desktop/ai_eactive_hub/mvp2

# 진행 현황
bash harness/scripts/status.sh

# 단계 시작 — AI에 붙여넣을 표준 프롬프트
bash harness/scripts/start.sh 01

# AI가 ../eactive-resource-hub/ 안에 코드 작성

# 자동 검증 (이 단계만)
bash harness/scripts/verify.sh 01

# 자동 검증 + MVP1 회귀까지 (큰 변경이 있을 때 권장)
bash harness/scripts/verify.sh 01 --with-mvp1

# 메모 + progress.json 갱신
bash harness/scripts/log.sh 01 "..."
```

## MVP2 핵심 원칙 (모든 단계 공통)

- **MVP1 acceptance를 깨지 않는다.** 큰 변경 후엔 `--with-mvp1`로 회귀 점검.
- **DB 마이그레이션은 V100 이상 번호로.** MVP1 V1~V99 자리는 hot-fix용.
- **권한**: ADMIN / SALES / EMPLOYEE — TEAM_LEADER는 deprecated.
- **다운로드**: ADMIN/SALES는 사유 없이 바로, EMPLOYEE는 본인만.
- **양식 이력서**: 단일 active 행만 유지.
- **mvp1 자료는 수정 금지.** 동결본.
