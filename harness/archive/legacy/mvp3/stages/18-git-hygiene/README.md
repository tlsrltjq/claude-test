# 18-git-hygiene: Git 위생 검증

## 목적

소스 저장소의 git 위생 상태를 자동으로 검사한다.  
민감 정보 커밋 유출, .gitignore 누락, 불필요한 merge 전 브랜치 잔존 등을 탐지한다.

## 검사 항목

| # | 항목 | 설명 |
|---|------|------|
| [1] | .gitignore 핵심 패턴 | `.env`, `storage/`, `build/`, `.idea/` 등 필수 패턴 존재 여부 |
| [2] | 민감 파일 미추적 | `.env`, `docker-compose.override.yml`이 git-tracked 상태가 아닌지 확인 |
| [3] | 커밋 히스토리 민감 패턴 | 최근 50 커밋 diff에 `password=`, `secret=`, `AKIA` 등 하드코딩 패턴 미존재 |
| [4] | 작업 폴더 clean | 미커밋 변경사항 없음 (운영 배포 전 체크용) |
| [5] | main 브랜치 기준 | 현재 브랜치가 main이거나 main과 diverge 없음 |
| [6] | merge된 feature 브랜치 | main에 merge 완료된 로컬 브랜치 목록 출력 (WARN — 삭제 권장) |

## 실행

```bash
bash harness/mvp3/stages/18-git-hygiene/verify.sh
```

## 합격 기준

`acceptance.md` 참조.
