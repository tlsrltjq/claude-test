# 프론트엔드·화면 명세

> 화면별 라우트·역할 접근·주요 UI 요소. Thymeleaf + Bootstrap 5.
> 참고 소스: `src/main/resources/templates/**/*.html`, 각 Controller, `SecurityConfig.java`

---

## 역할별 접근 가능 화면 요약

| 역할 | 접근 가능 영역 |
|------|--------------|
| 관리자(ADMIN) | 전체 |
| 영업(SALES) | `/sales/**`, 공통 화면 전체 |
| 사원(EMPLOYEE) | 공통 화면 전체 (admin·sales 제외) |
| 미인증 | `/login/**`, `/signup/**`, `/health` |

---

## 공통 화면

### 로그인 (`/login`)
- 템플릿: `login.html`
- 입력: 이메일 prefix, 비밀번호, ID 저장(localStorage)
- 오류: `?error` 파라미터 → 오류 메시지 표시
- 로그아웃 후: `?logout` 파라미터 → 안내 메시지
- 로그인 성공 → `/dashboard`

### 비밀번호 찾기 (`/login/forgot`, `/login/forgot/verify`)
- 템플릿: `login-forgot.html`, `login-forgot-verify.html`
- 1단계: 이메일 입력 → 6자리 코드 발송
- 2단계: 코드 입력(5분 타이머) → 새 비밀번호 설정

### 회원가입 (`/signup`, `/signup/verify`)
- 템플릿: `signup.html`, `signup-verify.html`
- 1단계: 이름·생년월일·연락처·이메일·직급·팀·비밀번호 입력
  - 생년월일: 숫자 8자리 입력 → `.` 자동 삽입(YYYY.MM.DD 표시)
  - 비밀번호 강도 표시 바
  - 폼 검증 실패 시 비밀번호 포함 모든 필드 값 유지 (sessionStorage 활용)
- 2단계: 이메일 인증 코드 입력(10분 타이머), 재발송 가능

### 대시보드 (`/dashboard`)
- 템플릿: `dashboard.html`
- 역할별 메뉴 그리드 표시
- 본인 정보(이름·직급·팀·역할) 표시

### 설정 (`/settings`)
- 템플릿: `settings.html`
- 탭: 기본정보 (`?tab=info`, 기본), 비밀번호 변경 (`?tab=password`)
- 기본정보: 연락처·생년월일 수정
- 비밀번호: 현재 비밀번호 확인 후 변경

### 검색 (`/search`)
- 템플릿: `search.html`
- 접근: 모든 인증 사용자
- 본인 접근 권한 내 문서 전체 검색
- 필터: 키워드, 문서 유형, 날짜 범위

### 에러 페이지
- `error/403.html` — 권한 없음
- `error/404.html` — 페이지 없음
- `error/500.html` — 서버 오류

---

## 내 폴더 화면 (전 역할)

### 내 폴더 목록 (`/my/folder`)
- 템플릿: `my/folder.html`
- 본인 개인 폴더 문서 목록
- 업로드 버튼 → `/my/folder/documents/upload`

### 문서 업로드 (`/my/folder/documents/upload`)
- 템플릿: `my/upload.html`
- 입력: 문서 유형(deprecated 제외), 제목, 파일(드래그앤드롭), 만료일(선택), 발급일(선택)
- 자격증: cert_type_meta hidden=ENGINEER 고정
- 허용 확장자: pdf, jpg, jpeg, png, docx, hwp, hwpx, ppt, pptx

### 문서 상세 (`/my/folder/documents/{documentId}`)
- 템플릿: `my/document-detail.html`
- 파일 미리보기(PDF·이미지), 다운로드, 버전 목록, 만료일 수정, 삭제

### 내 활동 (`/my/activity`)
- 템플릿: `my/activity.html`
- 본인의 감사 로그(업로드·다운로드·삭제) 목록

---

## 공용 폴더 화면 (전 역할)

### 공유 폴더 목록 (`/shared/folders`)
- 템플릿: `shared/folders.html`
- 권한 부여된 개인 폴더 목록 표시

### 개인 폴더 문서 목록 (`/shared/folders/{folderId}/documents`)
- 템플릿: `shared/folder-documents.html`
- 권한 부여된 폴더의 APPROVED 문서 목록

### 공용 폴더 (`/shared/folders/public`)
- 템플릿: `shared/public-folder.html`
- 전 사원 read
- ADMIN: 업로드·삭제 버튼 표시. 그 외: 표시 안 함

---

## 영업 화면 (SALES + ADMIN)

### 인력 목록 (`/sales/members`)
- 템플릿: `sales/members.html`
- 활성 직원 목록, 이름 클릭 → 문서 목록으로 이동

### 직원 문서 목록 (`/sales/members/{userId}/documents`)
- 템플릿: `sales/member-documents.html`
- 해당 직원의 APPROVED 문서 목록, 다운로드 가능

### 영업 직원 문서 (`/sales/employees/{userId}/documents`)
- 템플릿: `sales/employee-documents.html`
- 프로필 화면에서 진입하는 문서 상세 뷰

### 프로필(인력표) (`/sales/profiles`)
- 템플릿: `sales/profiles.html`
- 필터, 컬럼 선택·정렬, 프리셋 저장/불러오기
- 체크박스로 사원 선택 → 엑셀 내보내기 / 번들 다운로드
- 경력 표시 형식: 연월일(ymd) / 월(m)

### 경력 계산기 (`/sales/career-calculator`)
- 템플릿: `sales/career-calculator.html`
- 날짜 구간 동적 추가·삭제
- 중복 제거 옵션
- 결과: 총 개월·일, Y년 N개월 형식
- 저장: EmployeeProfile 업데이트
- 자동채움: 기존 저장값 불러오기

---

## 관리자 화면 (ADMIN 전용)

### 관리자 대시보드 (`/admin`)
- 템플릿: `admin/dashboard.html`
- 통계 카드 3개(전체 사용자·팀·검토 대기)
- 빠른 메뉴 9개(직원 관리·팀 관리·문서 검토·만료 현황·통계·재직증명서·양식이력서·인력표 팀 설정·파일 GC)
- 상단 네비바(문서검토·만료현황·팀관리·직원목록·재직증명서·통계·내화면·로그아웃)

### 직원 목록 (`/admin/employees`)
- 템플릿: `admin/employees.html`
- 검색(이름), 필터(직급·역할·팀), 페이지네이션
- 직원명 클릭 → 직원 상세

### 직원 상세 (`/admin/employees/{userId}`)
- 템플릿: `admin/employee-detail.html`
- 탭: 기본정보(`?tab=info`), 직급(`?tab=position`), 팀(`?tab=team`), 역할(`?tab=role`), 권한(`?tab=permissions`), 문서(`?tab=documents`)
- 기본정보: 계정 활성/비활성 토글
- 역할 변경 → 세션 즉시 만료 안내
- 권한 탭: 개인 폴더 접근 권한 부여·회수

### 직원 문서 목록/상세 (`/admin/employees/{userId}/documents/**`)
- 템플릿: `admin/employee-documents.html`, `admin/employee-document-detail.html`
- 모든 상태 문서 조회, 미리보기·다운로드·삭제

### 팀 관리 (`/admin/teams`)
- 템플릿: `admin/teams.html`, `admin/team-edit.html`
- 팀 생성·수정·삭제

### 인력표 팀 설정 (`/admin/teams/project-settings`)
- 템플릿: `admin/project-settings.html`
- 팀별 `project_team` 토글 — 영업 인력표 노출 여부

### 역할 변경 (`/admin/users/{userId}/role`)
- 템플릿: `admin/user-role.html`

### 권한 관리 (`/admin/users/{userId}/permissions`)
- 템플릿: `admin/user-permissions.html`

### 문서 검토 목록 (`/admin/documents/review`)
- 템플릿: `admin/documents-review.html`
- PENDING_REVIEW 문서 목록

### 문서 검토 상세 (`/admin/documents/review/{documentVersionId}`)
- 템플릿: `admin/document-review-detail.html`
- 미리보기, 승인·반려(반려 시 사유 입력)

### 만료 현황 (`/admin/documents/expiry`)
- 템플릿: `admin/documents-expiry.html`
- 만료된 문서 + 만료 임박 문서

### 통계 (`/admin/statistics`)
- 템플릿: `admin/statistics.html`
- 다운로드 통계, 상위 10 문서, 최근 30일 이력

### 이력서 템플릿 관리 (`/admin/resume-template`)
- 템플릿: `admin/resume-template.html`
- 현재 활성 템플릿 표시, 새 템플릿 업로드

### 재직증명서 (`/admin/certificate`)
- 템플릿: `admin/certificate.html`
- 직원 선택 → DOCX·PDF 생성 → 다운로드

### 파일 GC (`/admin/gc`)
- 템플릿: `admin/gc.html`
- 보존 기간(retentionDays) 표시, 수동 GC 실행 버튼, 결과 메시지

---

## 공통 UI 패턴

- 성공/오류 메시지: flash attribute(`successMessage`, `errorMessage`) → alert 컴포넌트
- 네비바: 현재 경로 기준 active 표시 (JavaScript)
- 로딩 스피너: POST 폼 제출 시 오버레이 표시
- CSRF 토큰: 모든 POST 폼에 `<input type="hidden" name="_csrf" th:value="${_csrf.token}">`
- 외부 CDN: Bootstrap 5.3.3, Bootstrap Icons 1.11.3 (cdn.jsdelivr.net)
