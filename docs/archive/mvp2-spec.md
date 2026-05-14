# eActive Resource Hub — MVP2 스펙 (SSOT)

> 이 문서는 mvp2.pdf 기획서를 정리한 MVP2의 단일 진실 원천(SSOT)입니다.
> MVP1 SSOT(`mvp1/docs/PROJECT_SPEC.md`)와 함께 읽으세요. 충돌 시 MVP2의 변경 사항이 우선합니다.

---

## 1. 최종 목표

영업부가 프로젝트 투입인력서를 만들 때 필요한 인력 정보를 **표 한 장으로 보고**, 직원 폴더·문서·양식 이력서를 빠르게 활용할 수 있는 내부 인력 관리 웹.

## 2. 권한 구조 (단순화)

MVP1의 `TEAM_LEADER`는 제거 또는 비활성, 새 역할 `SALES`를 추가하여 3개 역할로 단순화.

| 역할 | 가능 기능 |
|------|----------|
| `ADMIN` | 전체 인력 조회, 전체 폴더 접근, 바로 다운로드, 파일 삭제, 사용자/권한/문서 관리, 경력 계산기 |
| `SALES` | 전체 인력 프로필 표 조회, 직원 폴더 열람, 바로 다운로드, 경력 계산기, 양식 이력서 활용 |
| `EMPLOYEE` | 본인 폴더 열람, 본인 문서 업로드/수정, 양식 이력서 다운로드/작성/재업로드 |

**라우팅 변경**: `/team/**` → `/sales/**`. 기존 `/team/members`, `/team/members/{id}/documents`는 deprecated.

## 3. 다운로드 정책 변경

- **사유 입력 화면 제거.** `GET /documents/{id}/download/reason` 제거.
- **관리자**: 사유 없이 바로 다운로드.
- **영업부**: 사유 없이 바로 다운로드.
- **일반 직원**: 본인 문서만 다운로드.
- **다운로드 로그는 그대로 저장.** `audit_logs.reason`은 nullable.
  - 저장 항목: 사용자 ID, 대상 직원 ID, 문서 종류, 파일명, IP, User-Agent, 다운로드 시간.

## 4. 관리자 파일 삭제 (신규)

- `DELETE /admin/documents/{documentId}` — 관리자만.
- 디스크 파일 + DB 메타데이터 + 썸네일 삭제. `audit_logs.action_type=DELETE_DOCUMENT` 기록.

## 5. 회원가입 / 프로필 입력 확장

전부 필수 입력.

| 항목 | 방식 |
|------|------|
| 직급 | enum: `REPRESENTATIVE / EXECUTIVE_DIRECTOR / DIRECTOR / GENERAL_MANAGER / DEPUTY_GENERAL_MANAGER / MANAGER / ASSISTANT_MANAGER / STAFF` (대표/전무/이사/부장/차장/과장/대리/사원) |
| 이름 | 텍스트 |
| 생년월일 | DATE |
| 나이 | 생년월일 기준 자동 계산 (DB 컬럼 X, 응답 시 계산) |
| 연락처 | 텍스트 (`010-1234-5678` 형식 권장) |
| 이메일 | **앞부분만 입력**, 뒤는 `@${resourcehub.company-email-domain}` 자동 조합 |
| 비밀번호 | 영문/숫자/특수문자 3개 조합 + 8자 이상 |

**이메일 인증 단계에서 이메일 재입력 제거** — 가입 때 조합된 회사 이메일을 그대로 사용.

## 6. 인력 프로필 표 (`/sales/profiles`)

영업부/관리자가 보는 핵심 화면.

### 기본 컬럼

| # | 컬럼 | 출처 |
|---|------|------|
| 1 | 직급 | `users.position` |
| 2 | 이름 | `users.name` |
| 3 | 나이 | 생년월일에서 계산 |
| 4 | 생년월일 | `users.birth_date` |
| 5 | 연락처 | `users.phone` |
| 6 | 이메일 | `users.email` |
| 7 | 개발자 등급 | `employee_profiles.developer_grade` |
| 8 | 경력 | `employee_profiles.career_months` (N년 N개월 표시) |
| 9 | 이력서 | RESUME 최신 APPROVED 버전 |
| 10 | 경력기술서 | CAREER_DESCRIPTION 최신 APPROVED |
| 11 | 정보처리기사/자격증 | LICENSE 최신 APPROVED |
| 12 | 졸업증명서 | GRADUATION_CERTIFICATE 최신 APPROVED |
| 13 | 건강보험자격득실확인서 | HEALTH_INSURANCE_PROOF (신규 DocumentType) |
| 14 | 재직증명서 | EMPLOYMENT_CERTIFICATE 최신 APPROVED |
| 15 | 기타자료 | ETC 최신 APPROVED |
| 16 | 업데이트 날짜 | 최근 문서 업로드일 또는 프로필 수정일 |

### 동작

- **이름 클릭** → 직원 폴더 (ADMIN: `/admin/employees/{id}/documents`, SALES: `/sales/employees/{id}/documents`)
- **문서 칸** — 있으면 `[보기 / 다운로드]` 버튼, 없으면 `없음` 표시
- 여러 버전 있으면 **최신 APPROVED 우선**
- **관리자만 파일 삭제 가능**, 영업부는 삭제 불가

### DocumentType 추가

MVP1의 enum: `RESUME, CAREER_DESCRIPTION, GRADUATION_CERTIFICATE, LICENSE, EMPLOYMENT_CERTIFICATE, ETC`

MVP2 추가: `HEALTH_INSURANCE_PROOF`

## 7. 검색 / 필터 / 정렬

### 1차 (M2-05에서 구현)
- **검색**: 이름, 직급, 개발자 등급
- **필터**: 문서 보유 여부 (각 종류별)
- **정렬**: 이름, 직급, 개발자 등급, 경력
- **선택 컬럼만 보기** (체크박스 토글)

### 2차 (이후)
- 표 컬럼 순서 변경(드래그)

## 8. 양식 이력서 기능 (M2-06)

- `resume_templates` 테이블 — 단일 active 행만 유지 (이전 active는 archived).
- ADMIN/SALES만 업로드 가능.
- 직원: `/my/folder` 화면에 "양식 다운로드" 버튼.
- 직원이 작성 후 RESUME 유형으로 재업로드 → 인력 프로필 표의 이력서 칸에 자동 반영.

권장 흐름:
1. 관리자가 표준 이력서 양식 등록
2. 직원이 `/my/folder`에서 양식 다운로드
3. 직원이 작성
4. 직원이 RESUME 유형으로 재업로드
5. 영업부 표에서 바로 다운로드

## 9. 경력 계산기 (M2-07, M2-09)

- **M2-07 (1차, 단독 도구)**: `/sales/career-calculator` (ADMIN/SALES).
  - 시작일/종료일 N개 입력
  - 중복 기간 제거 옵션
  - 결과: `N년 N개월`
- **M2-09 (2차)**: 계산 결과를 `employee_profiles.career_months`에 반영.

## 10. 엑셀 내보내기 (M2-08, 2차)

- 영업부/관리자용 인력 표 엑셀 export.
- 현재 검색/필터 결과만, 체크된 컬럼만.
- 문서 파일 자체는 포함하지 않고 **있음/없음** 또는 **파일명**만 표시.
- audit_logs에 `EXPORT` 기록.

## 11. 단계 분할

### 1차 MVP2 (실제 영업부가 쓸 수 있는 화면)

| 단계 | 핵심 변경 | 새 마이그레이션 |
|------|----------|----------------|
| 01-permissions | TEAM_LEADER → SALES, 라우팅 `/team` → `/sales` | V100 |
| 02-download-policy | 사유 화면 제거, 바로 다운로드, 관리자 삭제 | V101 (옵션) |
| 03-profile-fields | position enum, birth_date, phone, 이메일 자동 조합, 비밀번호 정책 | V102 |
| 04-sales-profiles | `/sales/profiles` 14컬럼 표 (검색/정렬 X) | V103 |
| 05-search-filter-sort | 검색·필터·정렬·컬럼 토글 | (없음) |
| 06-resume-template | resume_templates 테이블, 양식 다운로드/재업로드 | V104 |
| 07-career-calculator | `/sales/career-calculator` 단독 도구 | (없음) |

### 2차 MVP2

| 단계 | 핵심 변경 | 새 마이그레이션 |
|------|----------|----------------|
| 08-excel-export | 엑셀 내보내기 + audit | (없음) |
| 09-career-save | 계산기 → 프로필 저장 연동 | V105 (옵션) |
| 10-bundle-template | 투입인력서 묶음 / 동적 컬럼 | V106~ |

## 12. 모든 단계 공통 보안 제약 (MVP1과 동일)

- JWT 사용 금지 (Spring Security 세션)
- Remember-me 금지, CSRF 활성화
- 세션 30분, 쿠키 `RESOURCEHUB_SESSION` (httpOnly + sameSite=strict)
- 파일 폴더 정적 노출 금지 — 모든 파일 접근은 컨트롤러 경유
- 파일은 UUID 파일명, DB는 메타데이터만
- 권한 검사는 Service로 분리

## 13. 회귀 보장

각 MVP2 단계 종료 시 **MVP1 acceptance가 깨지지 않아야 한다.**
- `mvp2/harness/scripts/verify.sh` 는 옵션 `--with-mvp1`을 지원하여, 실행 시 `mvp1/harness/stages/*/verify.sh`를 모두 함께 돌리고 회귀를 점검한다.
