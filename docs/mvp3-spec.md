# MVP3 — SSOT 요약

> 이 문서는 MVP3 라운드의 SSOT (한 장 요약).
> 상세는 REQUIREMENTS / DECISIONS / STAGE_PLAN / MIGRATION_FROM_MVP2 참조.

---

## 1. 라운드 목적

영업부가 실제로 쓰면서 발견한 **사용성/일관성 문제 13개**를 한 라운드로 정리. 큰 신기능보다는 **운영 마감과 흐름 정비** 중심.

## 2. 핵심 변경 4축

1. **인증/UX 마감** — ID 저장, 비번 찾기, 5분 타이머, 폼 순서
2. **문서 도메인 정비** — DocumentType 정비, 태그 제거, ppt/pptx 허용, 본인 삭제, 통합 검색, 전 사원 공용 폴더
3. **영업부 화면 마감** — sales/members 정렬, sales/profiles 컬럼 정리·등급 위젯·프리셋·경력 표시·체크 후 엑셀, career-calculator 검색 동작 복구
4. **관리자 화면 마감** — 직원 목록 검색·정리, 직급 한글, 권한 한글, 계정 활성/비활성

## 3. 권한·네이밍

- enum: ADMIN / SALES / EMPLOYEE (그대로)
- 화면 표시: **관리자 / 영업 / 사원** (한글)
- Position: 상무 추가 → 9개 직급
- TEAM_LEADER 제거 그대로 유지 (mvp2 01에서 deprecated)

## 4. DocumentType (MVP3 후)

활성: RESUME / CAREER_DESCRIPTION / GRADUATION_CERTIFICATE / **LICENSE(=정보처리기사)** / HEALTH_INSURANCE_PROOF / **PROFILE_PHOTO** / ETC
deprecated: EMPLOYMENT_CERTIFICATE

허용 확장자 (업로드 정책): pdf, jpg, jpeg, png, docx, hwp, hwpx, **ppt, pptx**

## 5. 새 화면 / 라우트

- `/login/forgot`, `/login/forgot/verify` — 비밀번호 찾기
- `/search` — 본인 권한 모든 문서 + 필터
- `/shared/folders/public` — 전 사원 공용 폴더

## 6. 새 데이터 모델

- (선택) `password_reset_tokens` (M3-02)
- `folders.type` 컬럼 + 공용 폴더 시드 (M3-07)
- `column_view_preferences` (M3-10)

## 7. 흡수 / 보류

- mvp2 08-excel-export → **mvp3 M3-11에 흡수** (체크 선택 + 엑셀)
- mvp2 09-career-save → **보류** (다음 라운드)
- mvp2 10-bundle-template → **보류**

## 8. 모든 단계 공통 보안 (mvp1·mvp2와 동일)

- JWT 금지, Spring Security 세션
- Remember-me 금지 (이메일 cookie는 별도 단순 cookie, 비번 저장 X)
- CSRF on, 세션 30분, RESOURCEHUB_SESSION (httpOnly+sameSite=strict)
- 파일 폴더 정적 노출 금지 — 모든 접근 컨트롤러 경유
- 파일 UUID 파일명, DB는 메타데이터만
- audit_logs 기록 유지 + 신규 액션: `RESET_PASSWORD`, `DELETE_DOCUMENT_SELF`, `CHANGE_USER_STATUS`, `EXPORT_PROFILES`
