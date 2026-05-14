# MVP3 — 단계 분할 계획

> Claude Code 1 sitting = 1 stage 가 되도록 자른 분할안. DB 영향이 큰 것부터 / 의존성이 있는 것 먼저.
> 사용자가 DECISIONS.md 의 12개 항목을 결정하면, 이 plan을 확정하고 stages/ 폴더 안에 prompt/acceptance/verify를 채운다.

---

## 의존성 그래프 (간단히)

```
M3-01 (네이밍·직급)
   ↓
M3-02 (로그인 UX) ── M3-03 (비번 찾기) ── M3-04 (이메일 인증 5분)
   ↓
M3-05 (Dashboard 보강)
   ↓
M3-06 (DocumentType 정비) ── M3-07 (태그 제거)
   ↓
M3-08 (통합 검색 화면)
   ↓
M3-09 (전 사원 공용 폴더)
   ↓
M3-10 (sales/members 정렬) ── M3-11 (관리자 직원 목록 검색)
   ↓
M3-12 (sales/profiles 컬럼·등급 위젯·경력 표시·사용자 프리셋)
   ↓
M3-13 (sales/profiles 체크 → 엑셀 — mvp2 08 흡수)
   ↓
M3-14 (career-calculator 검색)
   ↓
M3-15 (계정 활성/비활성 토글)
```

DB 마이그레이션 영향 단계: 01, 04, 06, 09, 12, 13. 그 외는 화면/서비스 변경 위주.

---

## 단계 카탈로그

### M3-01 — 직급·권한 네이밍 정비
- DECISIONS: D-06, D-09
- 변경:
  - Position enum 에 `MANAGING_DIRECTOR("상무")` 추가 (V200 마이그레이션, enum 순서 그대로 또는 sortOrder 컬럼 추가)
  - UserRole enum의 화면 표시 매핑 — `displayName()` 메서드 또는 i18n 메시지: ADMIN→관리자, SALES→영업, EMPLOYEE→사원
  - 모든 화면(헤더 배지, 직원 목록, 직원 상세, /admin/users/{id}/role select, /sales/profiles, dashboard)에서 한글 표시
  - 직급 표시도 한글(displayName) 사용 (이미 Position에 displayName 있음)
- DB: V200 (position 체크 제약 갱신 — `MANAGING_DIRECTOR` 허용)
- 의존성: 없음 — 가장 먼저
- 추정 크기: 작음 (한 sitting 안에 마무리)

### M3-02 — /login UX (ID 저장)
- DECISIONS: D-08
- 변경:
  - `/login` 화면에 "이메일 기억하기" 체크박스
  - 체크 시 `RESOURCEHUB_LAST_EMAIL` cookie 30일 (httpOnly=false, sameSite=strict)
  - 페이지 진입 시 JS가 cookie 읽어서 input value 채움
  - 체크 해제 + 로그인 시 cookie 삭제
- DB: 없음
- 의존성: 없음 (M3-01 후 진행 권장 — 한글 라벨 통일 위해)

### M3-03 — /login 비밀번호 찾기
- DECISIONS: D-07
- 변경:
  - 새 화면 `/login/forgot` (이메일 입력) → `/login/forgot/verify` (6자리 인증코드 + 새 비밀번호 두 번)
  - 인증코드 발급/검증은 기존 `email_verification_tokens` 테이블 재사용 (또는 `password_reset_tokens` 별도 — 권장 별도 테이블, 토큰 용도 구분)
  - 비밀번호 정책 동일 적용
  - audit_logs `RESET_PASSWORD`
- DB: V201 (`password_reset_tokens` 테이블 권장) 또는 0 (재사용 시)
- 의존성: M3-02 와 같이 진행 가능

### M3-04 — 이메일 인증 5분 + 타이머
- DECISIONS: D-12
- 변경:
  - `email_verification_tokens.expired_at` 산정 = 발급 + 5분 (코드 상수 변경)
  - `/signup/verify` 화면에 5분 카운트다운 JS
  - 1분 미만 시 빨간색 경고
  - 만료 후 "재발송" 버튼 활성, 클릭 시 동일 화면에서 새 토큰 발급
  - 새 토큰 발급 시 기존 토큰들 모두 verified_at 또는 expired_at NOW 처리(무효화)
  - `/signup/resend` 화면 일관성
- DB: 없음 (또는 `email_verification_tokens.invalidated_at` 추가)
- 의존성: 회원가입 흐름이 이미 동작 — 영향 적음

### M3-05 — /signup 폼 정비
- 변경:
  - 폼 입력 순서 재배치: 이름 → 생년월일 → 연락처 → 회사 이메일 → 팀 → 직급 → 비밀번호
  - 생년월일 입력을 8자 텍스트(YYYYMMDD)로 받아 LocalDate 변환 (예: 20010904)
  - 직급 select 에 "상무"(M3-01 결과) 노출
  - 이메일 잘못 입력 시 비밀번호 필드 유지 → 검증 케이스로 acceptance에 명시
- DB: 없음 (M3-01 마이그레이션이 이미 적용된 상태)
- 의존성: M3-01

### M3-06 — /dashboard 내 정보 보강
- 변경:
  - 카드: 이름, 이메일, 권한(한글), 상태(한글), **개발자 등급**, **생년월일**, **본인 팀**
  - 응답 시 employee_profiles + team join
- DB: 없음
- 의존성: M3-01

### M3-07 — DocumentType 정비
- DECISIONS: D-04
- 변경:
  - `LICENSE` 표시명 "정보처리기사"로 변경 (한글 displayName 메서드 또는 i18n)
  - `EMPLOYMENT_CERTIFICATE` deprecated 표시 (업로드 옵션에서 제외, 기존 데이터 보존)
  - `PROFILE_PHOTO` 신규 추가 (V202)
  - 허용 확장자에 `ppt, pptx` 추가 (application.yml + 검증 서비스)
  - 증명사진은 jpg/png 만
- DB: V202 (DocumentType 체크 제약 갱신: PROFILE_PHOTO 허용)
- 의존성: 없음

### M3-08 — 태그 기능 제거 (횡단)
- DECISIONS: D-05
- 변경:
  - 모든 화면에서 태그 표시·필터·입력 제거
  - 컨트롤러 파라미터에서 태그 제외
  - DB는 그대로 (Soft retire) — 마이그레이션 없음
- DB: 없음
- 의존성: M3-07 (문서 업로드 폼 다시 만지므로 같이 처리해도 OK)

### M3-09 — /myfolder 본인 삭제
- 변경:
  - 본인 문서 삭제 버튼 + 컨트롤러 (`DELETE /my/folder/documents/{id}`)
  - mvp2 02-download-policy 의 ADMIN 삭제 로직 재사용 (DocumentDeleteService)
  - 권한: 본인 + ADMIN — 기존 ADMIN 삭제는 그대로
  - audit_logs `DELETE_DOCUMENT_SELF` (또는 기존 `DELETE_DOCUMENT` reuse + reason="self")
- DB: 없음
- 의존성: 없음

### M3-10 — /search 통합 검색 화면
- 변경:
  - 진입 시 본인 권한 모든 문서 표시 (내 폴더 + 공용 폴더 + permissions FOLDER_ACCESS 받은 폴더)
  - 검색 필터: 제목·파일명, 종류(D-04 enum), 업로드자, 기간, 폴더 종류(개인/공용/공유)
  - 태그 표시·필터 제거(M3-08)
  - 결과 카드/표 — 클릭 시 문서 상세
- DB: 없음
- 의존성: M3-08, (M3-11 — 공용 폴더 만들어진 뒤가 더 자연스러움)

### M3-11 — /shared/folders 전 사원 공용 폴더
- DECISIONS: D-10
- 변경:
  - `folders.type ENUM('PERSONAL','SHARED_PUBLIC')` 컬럼 + 시드 1행
  - `/shared/folders/public` 화면 — 모두 read/write
  - 업로드: 모두 가능
  - 다운로드: 모두 가능
  - 삭제: 본인이 올린 행 + ADMIN
  - 가이드/이력서 양식 등 카테고리는 폴더 안의 documents에서 자유롭게 (별도 분류 필요 시 추후)
- DB: V203 (folders.type 추가 + 공용 폴더 시드)
- 의존성: M3-08

### M3-12 — /sales/members 정렬
- 변경:
  - 기본 정렬: 직급(Position enum 순서)
  - 컬럼 헤더 클릭: 팀, 역할 정렬
  - mvp2 05-search-filter-sort 와 일관된 패턴 (sort/direction 쿼리 파라미터)
- DB: 없음
- 의존성: M3-01

### M3-13 — /admin/employees 검색 + 화면 정리
- 변경:
  - 검색: 이름·직급·권한·팀 (4축, AND)
  - 표시: 직급 한글, 권한 한글
  - "개인폴더" 컬럼 삭제 (모두 가짐)
- DB: 없음
- 의존성: M3-01

### M3-14 — /sales/profiles 본격 정비 (1차)
- DECISIONS: D-01, D-02, D-03
- 변경:
  - **컬럼은 14개 유지** (D-01) — 정리는 추후, 유연성은 D-02 프리셋이 제공
  - 개발자 등급 인원 체크 표 (요약 위젯)
  - 사용자 정의 컬럼 프리셋 — `column_view_preferences (id, user_id, name, columns_json, sort_json, is_default, career_display, created_at, updated_at)` (V204)
  - 경력 표시 3가지 토글 — 상단 셀렉트 (n년n월n일 / n개월 / n일), 선택값은 프리셋에 저장
  - `employee_profiles.career_total_days INT NOT NULL DEFAULT 0` 컬럼 추가 (V205) — 일 단위까지 저장. `career_months`는 derived (career_total_days / 30) 또는 그대로 유지하되 정확도는 days가 진실 원천.
  - 경력 계산기(M3-12 또는 mvp2 09)에서 저장 시 days로 정확히 환산해 저장
- DB: V204 (column_view_preferences) + V205 (career_total_days)
- 의존성: M3-01, M3-07

### M3-15 — /sales/profiles 체크 후 엑셀 (mvp2 08 흡수)
- 변경:
  - 표 행별 체크박스
  - "선택 인원 엑셀" 버튼 → 체크된 user_id 만 export
  - 검색/필터/정렬 결과와 결합
  - audit_logs `EXPORT_PROFILES`
  - mvp2 stages/08-excel-export 폴더는 mvp3 M3-15로 흡수했다고 README에 명시
- DB: 없음
- 의존성: M3-14

### M3-16 — /sales/career-calculator 검색
- 변경:
  - 검색 input + 검색 버튼 (오른쪽)
  - Enter 키 핸들러
  - 현재 동작 안 한다는 버그 원인을 찾아 동작하게 — 폼 action 또는 JS 핸들러 누락 추정
- DB: 없음
- 의존성: 없음 — 독립적

### M3-17 — 계정 활성/비활성 토글
- DECISIONS: D-11
- 변경:
  - `/admin/employees/{id}` 상세에 [활성/비활성] 토글 버튼
  - 비활성화: users.status=DISABLED + Spring Security SessionRegistry 로 즉시 세션 무효화
  - 활성화: status=ACTIVE
  - audit_logs `CHANGE_USER_STATUS`
- DB: 없음 (UserStatus.DISABLED 기존 enum 재사용)
- 의존성: M3-01 (관리자 화면 손볼 때 같이)

---

## 단계 묶기 (사이즈 조정 — 최종 분할안)

작은 변경 여럿을 묶을지, 따로 갈지 사용자 결정. 권장:

| ID | 묶음 | 포함 | 크기 |
|----|------|------|------|
| **M3-01** | 직급·권한 네이밍 + 한글화 | M3-01 | 중 |
| **M3-02** | 로그인 UX (ID 저장 + 비번 찾기 + 인증 5분 타이머) | M3-02 + M3-03 + M3-04 | 큼 |
| **M3-03** | /signup 폼 정비 | M3-05 | 작 |
| **M3-04** | /dashboard 내 정보 보강 | M3-06 | 작 |
| **M3-05** | DocumentType 정비 + 태그 제거 | M3-07 + M3-08 | 중 |
| **M3-06** | /myfolder 본인 삭제 | M3-09 | 작 |
| **M3-07** | /shared/folders 공용 폴더 | M3-11 | 중 |
| **M3-08** | /search 통합 검색 화면 | M3-10 | 중 |
| **M3-09** | /sales/members 정렬 + 관리자 직원 목록 검색·정리 | M3-12 + M3-13 | 중 |
| **M3-10** | /sales/profiles 컬럼·등급 위젯·경력 표시·프리셋 | M3-14 | 큼 |
| **M3-11** | /sales/profiles 체크 → 엑셀 (mvp2 08 흡수) | M3-15 | 중 |
| **M3-12** | /sales/career-calculator 검색 | M3-16 | 작 |
| **M3-13** | 계정 활성/비활성 토글 | M3-17 | 작 |

→ **결과 13개 단계.** Claude Code가 각 단계를 1 sitting에 끝낼 수 있는 크기.

> M3-02 가 좀 큰 편이라 더 쪼개고 싶으면 M3-02a(ID 저장) + M3-02b(비번 찾기) + M3-02c(타이머) 로 분할.
> M3-10 도 큰 편이라 컬럼 정리/위젯 / 프리셋 / 경력 표시 토글로 3분할 가능.

---

## 권장 진행 순서

1. **DECISIONS.md 12항목 결정** (사용자 컨펌)
2. **STAGE_PLAN 확정** (이 표 묶음을 합의)
3. **stages/M3-01 prompt 채움** + 진행
4. 끝나면 stages/M3-02 ...

> mvp2의 08-excel-export, 09-career-save, 10-bundle-template 처리:
> - 08-excel-export → M3-11에 흡수
> - 09-career-save → 보류 (사용자가 "선택한 사람 경력 불러오기는 나중" 발언과 같은 맥락)
> - 10-bundle-template → 보류

---

## 핵심 보안·일관성 제약 (변경 없음 — 모든 단계에서 유지)

- JWT 금지, 세션 인증
- CSRF on
- 세션 30분, 쿠키 RESOURCEHUB_SESSION
- 파일 폴더 정적 노출 금지 — 컨트롤러 경유
- 파일 UUID 파일명, DB는 메타데이터만
- audit_logs는 모든 쓰기/삭제/검색 통계 행위에 기록
