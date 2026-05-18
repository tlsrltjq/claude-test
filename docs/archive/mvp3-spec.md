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

### 4-1. LICENSE 메타 정책 (Post-MVP3 확정)

- `certTypeMeta` 값: **`ENGINEER` 고정** — 산업기사(`INDUSTRIAL_ENGINEER`) 선택지 제거
- 업로드 폼(`/my/folder/documents`): 자격증 종류 select 제거, hidden input으로 ENGINEER 고정
- 경력계산기(`/sales/career-calculator`): 자격증 종류 select 제거, 체크박스 "정보처리기사" 단일 항목으로 교체
- 등급 기준 참고표: 산업기사 열 삭제, 기사 → 정보처리기사로 명칭 변경

## 5. 새 화면 / 라우트

- `/login/forgot`, `/login/forgot/verify` — 비밀번호 찾기
- `/search` — 본인 권한 모든 문서 + 필터
- `/shared/folders/public` — 전 사원 공용 폴더
- `/settings` — 계정 설정 (Post-MVP3 추가, 아래 §9 참조)

## 6. 새 데이터 모델

- (선택) `password_reset_tokens` (M3-02)
- `folders.type` 컬럼 + 공용 폴더 시드 (M3-07)
- `column_view_preferences` (M3-10)

## 7. 흡수 / 완료

- mvp2 08-excel-export → **mvp3 M3-11에 흡수** (체크 선택 + 엑셀)
- mvp2 09-career-save → **완료** (`CareerSaveService` + `POST /sales/career-calculator/save`)
- mvp2 10-bundle-template → **완료** (`BundleDownloadService` + `POST /sales/profiles/bundle-download`, ZIP 묶음 다운로드)

## 9. 계정 설정 페이지 (Post-MVP3)

> 구현 완료: 2026-05-08

### 라우트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET`  | `/settings?tab=info`     | 계정 정보 (읽기 전용) |
| `GET`  | `/settings?tab=profile`  | 개인정보 수정 폼 |
| `GET`  | `/settings?tab=password` | 비밀번호 변경 폼 |
| `POST` | `/settings/profile`      | 개인정보 저장 |
| `POST` | `/settings/password`     | 비밀번호 변경 |

### 탭별 기능

**계정 정보 탭** (읽기 전용)
- 이름, 이메일, 아이디, 권한, 상태, 팀, 직급, 연락처, 생년월일

**개인정보 수정 탭** (수정 가능 항목)
- 연락처(`phone`), 생년월일(`birthDate`), 직급(`position`)
- 이름·이메일·팀은 관리자 변경 전용 (화면에서 수정 불가)

**비밀번호 변경 탭**
- 현재 비밀번호 확인 후 새 비밀번호 설정
- 8자 이상 필수, 새 비밀번호 확인 불일치 시 서버·클라이언트 양측 오류 표시

### 구현 파일

- `SettingsController` — `/settings/**` 라우팅
- `SettingsService` — `getUser()`, `updateProfile()`, `changePassword()`
- `User.updateProfile(phone, birthDate)` — 엔티티 mutator 추가
- `templates/settings.html` — 3탭 단일 템플릿

### 접근 권한

Spring Security 기본 규칙 적용 — 로그인한 모든 역할(ADMIN/SALES/EMPLOYEE) 접근 가능.

---

## 10. 업로드 안전성 정책 (Post-MVP3 확정)

> 구현 완료: 2026-05-15

### 파일 검증 (이중 검사)

| 순서 | 검사 | 구현 |
|------|------|------|
| 1 | 확장자 허용 목록 | `DocumentUploadService.validateFile()` |
| 2 | Magic bytes 시그니처 | `FileMagicValidator.validate()` — 확장자 위조 차단 |

지원 확장자: `pdf`, `jpg/jpeg`, `png`, `docx`, `pptx`, `hwpx`, `hwp`, `ppt`

### 중복 방어 (이중)

- **DB 레벨**: `V211` partial unique index `(folder_id, document_type, title) WHERE status <> 'DELETED'`  
  → `DataIntegrityViolationException` 처리로 경쟁 조건에서도 안전
- **체크섬 레벨**: SHA-256 기반 사전 중복 탐지 (`findFirstByChecksumInFolder`)  
  → 동일 파일 재업로드 시 즉시 차단

### 업로드 취소

- XHR `abort()` 기반 — 진행 중 업로드 취소 즉시 반영
- 취소 버튼 상태 머신: 기본(이전 페이지) ↔ 업로드 중(취소 요청)

### 고아 파일 GC

- `DocumentFileGcService.runOrphanScan()`: 매일 새벽 2시 자동 실행
- 1시간 이상 된 파일만 스캔 (진행 중 업로드 보호)
- DB 경로 SET과 파일시스템 diff → 불일치 파일 삭제
- S3 스토리지 사용 시 스캔 건너뜀 (기본 메서드 no-op)

---

## 11. 운영 인프라 (Post-MVP3 확정)

> 구현 완료: 2026-05-15

### HTTPS / 리버스 프록시

- **Caddy 2** (`caddy:2-alpine`) — Let's Encrypt 자동 인증서
- 도메인: `CADDY_DOMAIN` 환경변수 → `{env.CADDY_DOMAIN}` 구문으로 `Caddyfile`에 주입
- app 서비스 외부 포트 비노출 — `backend` Docker 네트워크 내부 통신만
- `application-prod.yml`: `server.forward-headers-strategy: native` (X-Forwarded-Proto 신뢰)

### 배포 절차

```
bash scripts/deploy.sh          # 항상 이 스크립트로 배포
```

- 필수 env 미설정 시 배포 중단: `POSTGRES_PASSWORD`, `RESOURCEHUB_ADMIN_PASSWORD`, `CADDY_DOMAIN`
- `RESOURCEHUB_SEED_TEST_PASSWORD` 설정 감지 시 경고 + 확인 프롬프트

### 로그

- `logback-spring.xml` prod 프로파일: 롤링 파일 (50MB 단위, 30일 보관, 1GB 상한)
- 경로: `/data/logs/resourcehub.log` (Docker 볼륨 `resourcehub_logs`)

### 백업

| 스크립트 | 실행 | 내용 |
|----------|------|------|
| `backup-db.sh` | 매일 03:00 | pg_dump gzip, 30일 보관 |
| `backup-uploads.sh` | 매일 03:30 | 업로드 디렉토리 tar.gz, 30일 보관 |

크론잡 등록: 서버에서 `bash scripts/setup-cron.sh` 1회 실행

---

## 8. 모든 단계 공통 보안 (mvp1·mvp2와 동일)

- JWT 금지, Spring Security 세션
- Remember-me 금지 (이메일 cookie는 별도 단순 cookie, 비번 저장 X)
- CSRF on, 세션 30분, RESOURCEHUB_SESSION (httpOnly+sameSite=strict)
- 파일 폴더 정적 노출 금지 — 모든 접근 컨트롤러 경유
- 파일 UUID 파일명, DB는 메타데이터만
- audit_logs 기록 유지 + 신규 액션: `RESET_PASSWORD`, `DELETE_DOCUMENT_SELF`, `CHANGE_USER_STATUS`, `EXPORT_PROFILES`
