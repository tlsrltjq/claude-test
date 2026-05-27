# 제품 기능 스펙

> 현재 코드(V217 기준) 기반 기능 SSOT. "왜"는 `docs/decisions.md`, "어디"는 `docs/architecture.md`, "화면"은 `docs/frontend.md`.
> 참고 소스: 각 Controller/Service, `SecurityConfig.java`, Flyway V1~V217

---

## 제품 개요

이액티브 사내 직원·문서·인력 관리 포털.  
역할(관리자/영업/사원) 기반 접근 제어. 세션 인증. JWT 미사용.

---

## 1. 인증·계정

### 1-1. 회원가입

- 경로: `GET/POST /signup`, `GET/POST /signup/verify`, `POST /signup/resend`
- 흐름: 폼 입력 → 이메일 인증 코드 발송 → 코드 입력 → 계정 ACTIVE
- 필수 입력: 이름, 생년월일, 연락처, 회사 이메일(전체), 주소, 직급, 비밀번호, 개인정보 수집·이용 동의
- 선택 입력: 팀
- 비밀번호 정책: 영문·숫자·특수문자 포함, 8자 이상
- 인증 코드: 10분 TTL, 재발송 가능
- 이메일: 전체 이메일 주소 입력. 사전 등록된 이메일(`allowed_emails` 테이블)에 없으면 가입 차단
- 개인정보 동의: 필수 동의 없으면 가입 불가
- 가입 완료 시: ACTIVE 상태 즉시 활성화 + 개인 폴더 자동 생성 + EmployeeProfile 자동 생성

### 1-2. 로그인

- 경로: `GET/POST /login`
- Spring Security formLogin. 세션 쿠키 `RESOURCEHUB_SESSION` (HttpOnly, SameSite=Strict)
- 세션 30분 타임아웃. sessionFixation: changeSessionId
- 로그인 성공 시 `/dashboard`로 리다이렉트

### 1-3. 비밀번호 찾기

- 경로: `GET/POST /login/forgot`, `GET/POST /login/forgot/verify`
- 흐름: 이메일 입력 → 6자리 코드 발송(5분 TTL) → 코드 확인 → 새 비밀번호 설정
- 코드 재사용 불가 (`consumed_at` 기록)

### 1-4. 계정 설정

- 경로: `GET /settings`, `POST /settings/profile`, `POST /settings/team`, `POST /settings/password`
- 탭: 기본정보(`?tab=info` — 이름·주소·팀), 개인정보(`?tab=profile` — 연락처·생년월일), 비밀번호 변경(`?tab=password`)
- 모든 역할 접근 가능
- 직급은 관리자만 변경 가능 (ADMIN `/admin/employees/{id}/change-position`)

---

## 2. 내 폴더 (사원·관리자·영업 공통)

- 경로: `GET /my/folder`, `GET /my/folder/documents/upload`, `POST /my/folder/documents`
- `GET /my/folder/documents/{documentId}`, `POST /my/folder/documents/{documentId}/expiry`
- `POST /my/folder/documents/{documentId}/delete`

### 업로드

- 허용 확장자: `pdf`, `jpg`, `jpeg`, `png`, `docx`, `hwp`, `hwpx`, `ppt`, `pptx`
- 이중 검증: 확장자 + FileMagicValidator(magic bytes)
- 저장명: UUID 기반. 원본명은 DB(`original_file_name`)에 보관
- 중복 차단: SHA-256 체크섬 + V211 partial unique index (같은 폴더+타입+제목)
- 업로드 후 검토 상태: `PENDING_REVIEW` (관리자 승인 전까지 본인과 ADMIN만 접근)
- 썸네일: PDF·이미지 → ThumbnailService 비동기 생성

### 삭제

- 본인 문서: soft-delete(`deleted_at`, `deleted_by` 기록, status=DELETED)
- 관리자: `/admin/documents/{documentId}/delete`로 모든 문서 삭제 가능
- 파일 실제 삭제: GC(매일 02:00)가 처리

### 만료일 관리

- 문서별 `expires_at` 설정 가능
- 관리자 만료 현황 화면(`/admin/documents/expiry`)에서 확인

---

## 3. 공용 폴더

- 경로: `GET /shared/folders/public`
- `POST /shared/folders/public/documents` (전 인증 사용자)
- `POST /shared/folders/public/documents/{documentId}/delete` (업로더 본인 또는 ADMIN)
- 전 사원 read + 업로드. 삭제는 업로더 본인 또는 ADMIN
- V207 시드로 생성된 단일 SHARED_PUBLIC 폴더 사용

---

## 4. 문서 접근 (다운로드·미리보기·썸네일)

- 경로: `GET /documents/{documentVersionId}/download|preview|thumbnail`
- `POST /documents/{documentVersionId}/thumbnail/regenerate`
- 모든 파일 접근은 `DocumentAccessService.getVersionWithAccessCheck()` 경유 필수
- 접근 규칙:
  - ADMIN: 모든 문서 접근
  - SALES: 모든 문서 접근
  - EMPLOYEE: 본인 폴더 + 권한 부여된 폴더 + 공용 폴더 + 본인이 올린 파일
  - APPROVED 아닌 버전: 본인·ADMIN·SALES만 접근

---

## 5. 검색

- 경로: `GET /search`
- 본인 접근 권한 내 모든 문서 대상
- 제목·파일명 키워드 + 문서 유형·날짜 범위 필터

---

## 6. 내 활동

- 경로: `GET /my/activity`
- 본인의 업로드·다운로드·삭제 감사 로그 조회

---

## 7. 이력서 템플릿

- 경로: `GET /my/folder/resume-template/download` (사원), `GET /sales/resume-template/download` (영업)
- 관리자 업로드: `GET/POST /admin/resume-template`
- 활성 템플릿 1개. 모든 역할 다운로드 가능

---

## 8. 영업 기능 (SALES + ADMIN)

### 8-1. 인력 조회

- 경로: `GET /sales/members`, `GET /sales/members/{userId}/documents`
- `GET /sales/employees/{userId}/documents`
- 활성 직원 목록 + 직원별 문서 조회

### 8-2. 프로필 (영업 인력표)

- 경로: `GET /sales/profiles`, `GET/POST /sales/profiles/export`
- `POST /sales/profiles/bundle-download`
- `POST /sales/profiles/preset`, `POST /sales/profiles/preset/{id}/delete`
- 필터: 이름·직급·팀·가용 상태·기술등급 등
- 컬럼 선택·정렬·프리셋 저장(`column_view_preferences`)
- 엑셀 내보내기: 체크 선택된 사원 → xlsx
- 번들 다운로드: 체크 선택된 사원의 이력서·경력 문서 → ZIP

### 8-3. 경력 계산기

- 경로: `GET/POST /sales/career-calculator`, `POST /sales/career-calculator/save`
- `GET /sales/career-calculator/autofill`
- 날짜 구간 입력 → 중복 제거 옵션 → 총 경력(개월·일) 계산
- 저장: `EmployeeProfile.careerMonths`, `careerTotalDays` 업데이트
- 자동채움: 기존 저장된 경력 구간 불러오기

### 8-4. 프로젝트 투입 관리

- 경로: `GET /sales/calendar`
- `POST /sales/assignments` (ADMIN만)
- `POST /sales/assignments/{id}` (ADMIN만)
- `POST /sales/assignments/{id}/delete` (ADMIN만)
- 캘린더: 월별 직원 배정 현황. 일요일 시작, 기간 클리핑. CANCELLED 제외.
- 배정 등록: 대상 직원·프로젝트명·기간·투입률(0~100)·역할·고객사·메모
- 상태 자동 결정: 오늘 기준 `today < startDate` → PLANNED, `today >= startDate` → ACTIVE
- 중복 투입: 저장 허용, `overlapWarning` flash로 경고만 표시 (ADR-037)
- 인력표 투입 정보 컬럼: 투입 중(ACTIVE) / 투입 예정(PLANNED) / 미투입 배지로 표시
- 대시보드 투입 요약 카드: 현재 투입 중·이번달 시작/종료·미투입 인원 수, 종료 임박 5건

---

## 9. 관리자 기능 (ADMIN 전용)

### 9-1. 대시보드

- 경로: `GET /admin`
- 통계 3개 (전체 사용자, 전체 팀, 검토 대기)
- 빠른 메뉴 10개(직원 관리·팀 관리·문서 검토·만료 현황·통계·재직증명서·양식이력서·인력표 팀 설정·파일 GC·가입 허용)

### 9-2. 직원 관리

- 경로: `GET /admin/employees`, `GET /admin/employees/{userId}`
- `POST /admin/employees/{userId}/toggle-status`
- `POST /admin/employees/{userId}/change-position`
- `POST /admin/employees/{userId}/change-team`
- 직원 검색·필터(이름·직급·역할·팀), 페이지네이션
- 계정 활성/비활성: 비활성 시 기존 세션 즉시 만료
- 역할 변경: `POST /admin/users/{userId}/role` → 세션 즉시 만료(재로그인 시 새 권한 적용)
- 권한 부여/회수: `POST /admin/users/{userId}/permissions/grant|revoke`

### 9-3. 팀 관리

- 경로: `GET/POST /admin/teams`, `GET /admin/teams/{teamId}/edit`, `POST /admin/teams/{teamId}/update`
- `POST /admin/teams/{teamId}/toggle-project`, `POST /admin/teams/{teamId}/delete`
- `GET /admin/teams/project-settings` (인력표 노출 팀 설정)

### 9-4. 문서 검토

- 경로: `GET /admin/documents/review`, `GET /admin/documents/review/{documentVersionId}`
- `POST /admin/documents/review/{documentVersionId}/approve|reject`
- 검토 대기(`PENDING_REVIEW`) 문서 목록 → 승인/반려(반려 시 사유 필수)

### 9-5. 만료 현황

- 경로: `GET /admin/documents/expiry`
- 만료된 문서 + 만료 임박 문서 목록

### 9-6. 통계

- 경로: `GET /admin/statistics`
- 다운로드 통계 (전체·오늘·최근 7일·이번 달) + 업로드 통계, 상위 문서 10건, 최근 30일 다운로드 이력, 최근 20건 업로드 이력

### 9-7. 재직증명서

- 경로: `GET /admin/certificate`, `POST /admin/certificate/generate`, `POST /admin/certificate/create`
- `GET /admin/certificate/download/{filename}`
- Python Flask 컨테이너 호출 → DOCX·PDF 생성. 최신 파일 강조 표시

### 9-8. 파일 GC

- 경로: `GET /admin/gc`, `POST /admin/gc/run`
- 고아 파일(1시간 이상 된 soft-delete 파일) 수동 실행
- 자동: 매일 02:00 cron (`@Scheduled`)

### 9-9. 가입 허용 이메일 관리

- 경로: `GET /admin/allowed-emails`, `POST /admin/allowed-emails`, `POST /admin/allowed-emails/{id}/delete`
- 사전 등록된 이메일만 회원가입 허용 (`allowed_emails` 테이블)
- 이메일 추가·삭제. 등록되지 않은 이메일은 회원가입 폼 제출 시 차단

---

## 10. 접근 제어 요약

→ `docs/SECURITY_AND_PERMISSION.md` 참조 (역할별 URL 패턴, 문서 접근 규칙, 서비스 레이어 검증 포인트)

---

## 11. 보안 정책 (코딩 규칙)

→ `docs/SECURITY_AND_PERMISSION.md` 섹션 7 참조

---

## 12. 외부 의존 서비스

| 서비스 | 연결 | 비고 |
|--------|------|------|
| PostgreSQL 18 | `spring.datasource` | Flyway 자동 마이그레이션 |
| SMTP (이메일) | `spring.mail.*` 환경변수 | 개발: ConsoleEmailSender 프로파일 |
| Python Flask (certificate) | `http://certificate:5001` | 재직증명서 DOCX·PDF 생성 |
| Local FS / S3(R2·MinIO) | `RESOURCEHUB_STORAGE_TYPE` 환경변수 | 파일 스토리지 추상화 |
