# MVP2 Stage 06 — Context

## SSOT
`mvp2/docs/PROJECT_SPEC_MVP2.md` §8 양식 이력서 기능.

## 이전 단계 결과
- /sales/profiles 14컬럼 표 + 검색/필터/정렬 동작
- 이력서 칸은 RESUME 최신 APPROVED DocumentVersion 보여줌

## 핵심 제약
- **단일 active 행만 유지** — 새로 업로드 시 기존 ACTIVE → ARCHIVED (트랜잭션)
- 다운로드는 컨트롤러 경유, 디스크 직노출 금지
- 양식 자체 삭제는 1차에서 안 함 (새 업로드로 대체)
- 5단계 업로드 정책(허용 확장자, 20MB) 따름

## 코드가 들어갈 위치
- 마이그레이션: `V104__create_resume_templates.sql`
- 도메인: `template/entity/`, `template/repository/`, `template/service/`, `template/controller/`
- 템플릿: `templates/admin/resume-template.html`, `templates/my/folder.html` (버튼 추가)
