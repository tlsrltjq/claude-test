# MVP3 — 결정 사항

> 단계 prompt를 본격 채우기 전 의사결정. **2026-05-04 사용자 컨펌 완료.**

## 결정 요약

| ID | 결정 | 비고 |
|----|------|------|
| D-01 | 14컬럼 일단 유지 + D-02 프리셋으로 유연성 제공 | 영업이 직접 보고 컬럼 정리는 추후 |
| D-02 | 사용자별 프리셋 (`column_view_preferences`) | 한 사용자가 N개 프리셋 |
| D-03 | 표시 방식 (a) 상단 셀렉트 토글 / **DB는 일 단위까지 저장** | `career_total_days` 컬럼 + (months는 derived) |
| D-04 | 표시명만 변경 (LICENSE→정보처리기사) / EMPLOYMENT_CERTIFICATE deprecated / PROFILE_PHOTO 신규 | enum 값 그대로, displayName만 |
| D-05 | A — 컬럼/필터 표시만 제거, DB 보존 | 다음 라운드 cleanup |
| D-06 | A — 화면 표시만 한글, enum 그대로 | i18n 또는 displayName |
| D-07 | C — 인증코드 + 새 비밀번호 입력 화면 | 평문 비번 메일 X |
| D-08 | OK — `RESOURCEHUB_LAST_EMAIL` cookie 30일, sameSite=strict, 비번 저장 X | |
| D-09 | 권장값 — `MANAGING_DIRECTOR("상무")` 전무 ↓ 이사 ↑ | |
| D-10 | A — `folders.type ENUM('PERSONAL','SHARED_PUBLIC')` 추가, 기존 모델 재사용 | 시드 1행 |
| D-11 | OK / OK / 보존 — `UserStatus.DISABLED` 재사용 / 즉시 세션 무효화 / permissions 보존 | |
| D-12 | 권장값 — 1분 미만 빨간색, 자동 재발송 X, 이전 토큰 무효화 | |

> 아래 본문은 의사결정 맥락을 위해 그대로 보존. 결정값은 위 표가 우선.

---

---

## D-01. /sales/profiles — 어느 컬럼을 살리고 어느 걸 빼는가?

현재 14개 컬럼:
직급 / 이름 / 나이 / 생년월일 / 연락처 / 이메일 / 개발자 등급 / 경력 / 이력서 / 경력기술서 / 정보처리기사·자격증 / 졸업증명서 / 건강보험자격득실확인서 / 재직증명서 / 기타자료 / 업데이트 날짜

후보:
- ❓ **재직증명서 컬럼** — D-04에서 DocumentType에서 제거하기로 하면 표 컬럼도 같이 제거
- ❓ **증명사진 컬럼** — DocumentType 추가 시 표에도 추가? (사진은 표 작은 셀에 보이는 게 어색하니 "있음/없음"만 표시 권장)
- ❓ **나이 vs 생년월일** — 둘 중 하나만? 또는 토글로 둘 다?
- ❓ **연락처 / 이메일** — 영업부가 자주 본다고 가정하면 둘 다 유지 권장
- ❓ **건강보험자격득실확인서** — 영업이 자주 보는가? 안 보면 별도 화면으로 빼고 표에서는 보유 여부만

**권장 기본 컬럼 (M3-09 들어갈 때 합의)**:
직급 / 이름 / 생년월일(YYYYMMDD) / 연락처 / 이메일 / 개발자 등급 / 경력 / 이력서 / 경력기술서 / 정보처리기사 / 졸업증명서 / 건강보험 / 증명사진 / 기타 / 업데이트일 (재직증명서 제거)

→ **결정 필요**: 위 14개 중 빼고 싶은 것 / 추가하고 싶은 것

---

## D-02. /sales/profiles — 사용자 정의 컬럼 옵션

"사용했던 컬럼을 재사용하기 쉽게"

옵션 A: **사용자별 프리셋** (`column_view_preferences` 테이블)
- 한 사용자가 여러 프리셋 저장 (예: "기본", "이력서 검토용", "프로젝트 매칭용")
- 권장 ✅ — 영업부 개인이 자기만의 시야를 가질 수 있음

옵션 B: **전역 프리셋** (`column_view_templates` 테이블, ADMIN만 등록)
- 회사 표준 시야

옵션 C: **둘 다**
- 전역 프리셋 + 본인 프리셋

→ **결정 필요**: A / B / C / 다른 방식

---

## D-03. /sales/profiles — 경력 표시 3가지 방식

`n년 n월 n일` / `n개월` / `n일`

표시 방식이:
- (a) **사용자가 셀렉트로 토글** — 표 상단에 표시 단위 선택, 표 전체에 적용
- (b) **컬럼별 모드 저장** — 프리셋(D-02)에 저장
- (c) **컬럼 헤더에 토글** — 컬럼별로 클릭해서 단위 변경

권장: (a) **상단 셀렉트로 표 전체 토글** + 프리셋(D-02)에 선택값 저장.

또 한 가지 — `n년 n월 n일` 표시는 **현재 DB는 `career_months`(int) 만 갖고 있음**. "n일" 정확도를 내려면 정확한 시작/종료 날짜 입력이 필요. 현재 경력 계산기는 일 단위 정확도가 있으니 그 결과를 같이 저장하는 정책이 필요.

→ **결정 필요**: 표시 방식 (a/b/c) + DB에 어떤 단위까지 저장 (월 vs 일)

---

## D-04. DocumentType 변경

| MVP2 | MVP3 |
|------|------|
| RESUME (이력서) | 유지 |
| CAREER_DESCRIPTION (경력기술서) | 유지 |
| GRADUATION_CERTIFICATE (졸업증명서) | 유지 |
| LICENSE (자격증) | **이름 변경 → 정보처리기사** (`LICENSE` enum 그대로 두고 displayName만 "정보처리기사"로 권장 — DB 호환) |
| EMPLOYMENT_CERTIFICATE (재직증명서) | **삭제** ❓ soft (deprecated) vs hard (drop) |
| HEALTH_INSURANCE_PROOF (건강보험) | 유지 |
| ETC (기타) | 유지 |
| (없음) | **PROFILE_PHOTO (증명사진)** 신규 추가 |

권장:
- LICENSE는 enum 값 그대로, 화면 표시명만 "정보처리기사"로 변경 (기존 데이터 보존)
- EMPLOYMENT_CERTIFICATE는 **deprecated** — enum 값은 남기고 신규 업로드 옵션에서만 제거 (기존 업로드된 재직증명서는 보존)
- PROFILE_PHOTO 신규 추가, 허용 확장자는 jpg/png만

→ **결정 필요**: LICENSE 표시명만 변경 vs 새 enum LICENSE_INFOPROC 추가 / EMPLOYMENT_CERTIFICATE soft vs hard / PROFILE_PHOTO 확장자 정책

---

## D-05. 태그 기능 — 데이터까지 drop?

옵션 A: **컬럼/필터 표시만 제거** — DB 컬럼/태그 테이블은 그대로 (recovery 가능)
옵션 B: **테이블 drop + 마이그레이션** — 깨끗하게 제거

권장: **A** — drop은 운영 한참 후 별도 cleanup 마이그레이션으로.

→ **결정 필요**: A / B

---

## D-06. 권한 네이밍 한글화 — 화면만? enum까지?

옵션 A: **화면 표시만 한글** — UserRole enum은 ADMIN/SALES/EMPLOYEE 그대로, 표시 시 "관리자/영업/사원"으로 매핑
옵션 B: **enum 자체를 한글로** — DB까지 영향, 운영/검색 어려움

권장: **A** — 화면에서만 매핑. 헤더/배지/select option 등 모든 곳에서 displayName 사용. enum은 그대로.

→ **결정 필요**: A / B

---

## D-07. 비밀번호 찾기 — 임시 비밀번호 평문 발송?

사용자 메모: "랜덤 문자열 이메일로 발송"

옵션 A: **임시 비밀번호 평문 메일** — 받자마자 강제 변경 (다음 로그인 시 비번 변경 화면)
옵션 B: **재설정 링크** — 토큰 포함 URL 클릭 → 새 비밀번호 입력 화면
옵션 C: **6자리 인증코드 + 새 비번 입력 화면** (가입 인증과 동일 패턴) — **권장**

권장: **C** — 인증코드 검증 후 화면에서 새 비밀번호 두 번 입력. 메일에 평문 비번 안 들어가서 안전.

→ **결정 필요**: A / B / C

---

## D-08. ID 저장 — 보안 원칙과 충돌?

원칙: Remember-me 사용 금지, 비밀번호는 절대 클라이언트 저장 안 함.

해석: "ID 저장 = 이메일만 cookie에 저장 (HttpOnly=false, JS가 읽어서 input value에 채움)". 비밀번호는 매번 입력.

권장: **별도 cookie `RESOURCEHUB_LAST_EMAIL` (HttpOnly=false, sameSite=strict, 30일)** + login 페이지 JS가 읽어서 채움. 체크박스 해제 시 cookie 삭제.

→ **결정 필요**: 위 권장안 OK?

---

## D-09. 직급 "상무" 추가 위치

현재 Position enum 순서:
REPRESENTATIVE(대표) → EXECUTIVE_DIRECTOR(전무) → DIRECTOR(이사) → GENERAL_MANAGER(부장) → DEPUTY_GENERAL_MANAGER(차장) → MANAGER(과장) → ASSISTANT_MANAGER(대리) → STAFF(사원)

상무는 일반적으로 전무 아래 / 이사 위.

권장:
```
REPRESENTATIVE(대표)
EXECUTIVE_DIRECTOR(전무)
MANAGING_DIRECTOR(상무)   ← 신규
DIRECTOR(이사)
GENERAL_MANAGER(부장)
DEPUTY_GENERAL_MANAGER(차장)
MANAGER(과장)
ASSISTANT_MANAGER(대리)
STAFF(사원)
```

→ **결정 필요**: 영문 enum 이름 (`MANAGING_DIRECTOR` 권장) + 표시 위치

---

## D-10. 전 사원 공용 폴더 — 데이터 모델

옵션 A: 기존 `folders` 테이블에 `type ENUM('PERSONAL','SHARED_PUBLIC')` 컬럼 추가 + SHARED_PUBLIC type=1행 시드
옵션 B: 별도 테이블 `shared_documents` (folder 개념 없이 평면)

권장: **A** — 기존 폴더 모델 재사용. owner_user_id는 NULL 또는 ADMIN bot user 가리키게. 권한 분기는 type 으로.

→ **결정 필요**: A / B

---

## D-11. 계정 비활성화 — 기존 UserStatus 재사용

UserStatus 에 이미 `DISABLED` 있을 가능성 높음. 그걸 그대로 사용:
- ADMIN이 직원 상세에서 [비활성화] 클릭 → users.status = DISABLED
- 비활성화 시 즉시 세션 무효화 (Spring Security `SessionRegistry`).
- 다시 [활성화] 클릭 → ACTIVE로 복귀.

→ **결정 필요**: 기존 DISABLED 재사용 OK? / 즉시 세션 무효화 OK? / 비활성 사용자가 가지고 있던 permissions 행은 보존? (권장: 보존, 활성 시 자동 복귀)

---

## D-12. 5분 인증 타이머 — 시각 갱신 정책

5분 타이머 화면:
- 서버: `email_verification_tokens.expired_at` = 발급 시점 + 5분 → 단순 변경
- 화면: `04:59 / 04:58 / ...` JS countdown
- 만료 후: "재발송" 버튼 활성

→ **결정 필요**:
1. 만료 직전(예: 1분) 색깔 빨간색으로 경고? (권장 ✅)
2. 만료 후 자동으로 재발송? (권장 ❌ — 사용자가 재발송 버튼 눌러야 함)
3. 동시 발급된 이전 토큰은 무효화? (권장 ✅)
