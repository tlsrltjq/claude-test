# Stage 01 — Context

## SSOT
`docs/PROJECT_SPEC.md` 의 §8 기술 스택, §10 파일 저장 방식, §13 로컬 개발 구조, §16 패키지 구조, §25 절대 원칙.

## 이전 단계 결과
없음. 비어있는 `eactive-resource-hub/` 디렉토리에서 시작한다.

## 이번 단계 핵심 제약
- 회원가입/로그인/파일 업로드 등 **실제 기능은 만들지 않는다.**
- Spring Security 의존성은 추가하되, **이번 단계에서는 모든 요청을 허용**해서 `/health`가 동작하도록 한다 (구체 구성은 3단계에서).
- DB 스키마는 만들지 않는다. Flyway 의존성은 넣되 마이그레이션 파일은 다음 단계.
- `.gitignore`에 `.env`, `storage/`, `logs/`, `*.log` 반드시 포함.
- 업로드 경로는 `${RESOURCEHUB_UPLOAD_BASE_DIR:./storage/uploads}` 형식으로 환경변수 치환 가능하게.

## 코드가 들어갈 위치
- `../eactive-resource-hub/` 프로젝트 루트
- 패키지: `com.eactive.resourcehub.*`
