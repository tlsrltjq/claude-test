# Harness — eActive Resource Hub 단계별 AI 코딩 작업 환경

이 폴더는 **eActive Resource Hub를 AI(Cursor/Claude/ChatGPT 등)에게 단계별로 만들게 하기 위한 작업 하네스**다.
실제 Spring Boot 코드는 `../eactive-resource-hub/` 에 들어가고, 이 폴더는 거기에 들어갈 코드를 **만들기 위한 도구**들의 모음이다.

---

## 0. 하네스의 철학

> 한 번에 다 시키지 말고, 한 단계씩 시킨다. 단계마다 입력·산출물·검증을 명시하고, 검증을 통과해야 다음 단계로 넘어간다.

세 가지 원칙을 지킨다.

1. **단계는 잘게 — 검증은 빡세게.** PDF의 1~10단계를 그대로 따른다. 단계 끝마다 PDF의 검증 항목을 체크리스트로 통과시킨다.
2. **컨텍스트는 명시적으로.** AI에게 매번 똑같은 SSOT(`docs/PROJECT_SPEC.md`)와 단계별 prompt/context를 묶어 던진다. 사람이 매번 새로 설명하지 않는다.
3. **결과는 추적한다.** `state/progress.json`에 단계별 시작/완료/검증 결과가 남는다. `logs/` 에는 AI에게 던진 프롬프트와 받은 응답 요약이 남는다.

---

## 1. 디렉토리 구조

```
harness/
├─ README.md                  # 이 파일
├─ stages/                    # 단계 정의 — 진행은 ID 순서대로
│  ├─ 01-skeleton/
│  ├─ 02-db-flyway/
│  ├─ 03-auth/
│  ├─ 04-team-folder/
│  ├─ 05-upload/
│  ├─ 06-preview-download/
│  ├─ 07-permissions/
│  ├─ 08-thumbnail/
│  ├─ 09-review/
│  └─ 10-deploy/
├─ templates/
│  └─ stage/                  # 새 단계를 추가할 때 복사할 템플릿
├─ scripts/                   # 하네스 운영 스크립트
│  ├─ status.sh               # 전체 진행 현황
│  ├─ start.sh                # 특정 단계 시작 (프롬프트+컨텍스트 출력)
│  ├─ verify.sh               # 특정 단계 검증
│  └─ log.sh                  # 작업 로그 추가
├─ state/
│  └─ progress.json           # 단계별 상태 추적
└─ logs/                      # AI 작업 기록 (수동 또는 log.sh로 추가)
```

각 `stages/NN-XXX/` 폴더는 다음 파일들로 구성된다.

| 파일 | 역할 |
|------|------|
| `README.md` | 단계 개요 — 무엇을, 왜, 어떤 제약 아래 |
| `prompt.md` | AI에게 던질 본 프롬프트 (PDF 단계별 프롬프트 그대로) |
| `context.md` | AI에게 같이 던질 컨텍스트 (이전 단계 결과·제약·SSOT 링크) |
| `deliverables.md` | 이 단계에서 만들어져야 할 산출물 목록 |
| `acceptance.md` | 사람이 손으로 체크할 수락 기준 (PDF 검증 항목) |
| `verify.sh` | 자동 검증 스크립트 (가능한 항목만) |

---

## 2. 표준 워크플로우 (1 단계당 한 사이클)

```
status.sh                             # 어디까지 됐는지 확인
└─ start.sh 01                        # 1단계 프롬프트+컨텍스트 출력
   └─ [AI에게 프롬프트 전달]            # Cursor/Claude 등에 붙여넣기
      └─ [AI가 ../eactive-resource-hub/ 에 코드 생성]
         └─ verify.sh 01              # 자동 검증 (있는 항목만)
            └─ acceptance.md 수동 체크  # 사람이 직접 확인
               └─ log.sh 01 "메모"     # 결과 기록 + progress.json 업데이트
                  └─ [다음 단계로]
```

핵심: **acceptance.md를 통과하지 못하면 다음 단계로 가지 않는다.**

---

## 3. start.sh 가 만드는 표준 프롬프트 형식

`scripts/start.sh NN`을 실행하면 아래 형식으로 출력된다. 이걸 그대로 AI 도구에 붙여넣으면 된다.

```
=== eActive Resource Hub — Stage NN ===

[프로젝트 SSOT]
docs/PROJECT_SPEC.md 의 핵심 제약을 준수하라:
- Java 21 + Spring Boot 3.5.x + Gradle + PostgreSQL
- 인증: Spring Security 세션 기반 (JWT 금지)
- 파일: 디스크 저장 + UUID 파일명, DB는 메타데이터만
- 권한: ADMIN / TEAM_LEADER / EMPLOYEE + permissions(개별 폴더)
- 패키지: com.eactive.resourcehub
- 모든 파일 접근은 컨트롤러 경유, 직접 노출 금지

[이전 단계 컨텍스트]
<context.md 내용>

[이번 단계 작업]
<prompt.md 내용>

[수락 기준]
<acceptance.md 내용>

코드는 ../eactive-resource-hub/ 아래에 생성하라.
이번 단계에 명시되지 않은 기능은 만들지 마라.
```

---

## 4. 단계 진행 상태 (progress.json 스키마)

```json
{
  "project": "eactive-resource-hub",
  "currentStage": "01-skeleton",
  "stages": [
    {
      "id": "01-skeleton",
      "title": "프로젝트 골격",
      "status": "pending | in_progress | verified | blocked",
      "startedAt": null,
      "completedAt": null,
      "verifiedAt": null,
      "verifyResult": null,
      "notes": []
    }
  ]
}
```

상태 값:
- `pending` — 아직 시작 안 함
- `in_progress` — AI에게 프롬프트 전달함, 결과 아직 검증 안 됨
- `verified` — verify.sh + acceptance.md 모두 통과
- `blocked` — 검증 실패 — 메모 보고 다시 시도

---

## 5. AI 도구별 사용 팁

### Cursor / Windsurf 같은 IDE 통합형
- `start.sh 01` 출력을 그대로 채팅창에 붙여넣음
- AI가 `../eactive-resource-hub/`에 파일을 직접 생성/수정
- 끝나면 `verify.sh 01`로 1차 검증 → acceptance 수동 체크

### Claude Code / Claude Agent SDK
- 같은 프롬프트를 워크스페이스 루트에서 실행
- 자동으로 파일 시스템에 반영됨

### ChatGPT 웹 / Claude.ai 웹
- AI가 코드를 텍스트로 뱉으면, 로컬에 저장은 사람이 수동
- 대신 모델이 추론에 더 많은 토큰을 쓸 수 있어 설계 검토에 유리
- 이 방식일 경우 `logs/`에 응답 전체를 저장해 두면 좋음

---

## 6. 단계 추가/수정 시

1. `templates/stage/`를 복사해 새 폴더(`stages/NN-XXX/`)를 만든다.
2. `prompt.md` / `acceptance.md` / `verify.sh`를 채운다.
3. `state/progress.json`에 새 단계를 추가한다.
4. 이 README의 디렉토리 구조 표를 갱신한다.

---

## 7. 자주 빠지는 함정

- **PDF의 "아직 만들지 마" 목록 무시 금지.** 단계마다 명시된 NOT-DOING 리스트를 AI에게 함께 전달해야 범위가 부푸는 걸 막을 수 있다. `prompt.md` 끝에 그대로 옮겨놨다.
- **한 단계에서 두 단계 일을 시키지 말 것.** 다음 단계 파일은 다음 단계에서 만든다.
- **검증을 건너뛰지 말 것.** acceptance.md는 PDF 검증 항목을 그대로 옮긴 것이다. 통과하지 못하면 그 위에 쌓이는 모든 단계가 위태로워진다.
- **AI가 새 라이브러리/도구를 끌어오면 의심하라.** PROJECT_SPEC.md의 기술 스택 표 바깥은 원칙적으로 추가하지 않는다.

---

## 8. 다음 액션

```bash
cd /Users/shingiseop/Desktop/ai_eactive_hub
bash harness/scripts/status.sh
bash harness/scripts/start.sh 01
```
