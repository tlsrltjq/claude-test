# 도메인 용어집

> 코드·문서·화면에서 사용하는 용어의 단일 출처. 다른 문서에서 용어가 흔들릴 때 이 문서를 기준으로 한다.
> 참고 소스: `user/entity/*.java`, `document/entity/*.java`, `SecurityConfig.java`

---

## 역할 (UserRole)

| enum 값 | 화면 표시 | 설명 |
|---------|----------|------|
| `ADMIN` | 관리자 | 모든 기능 접근. 직원 관리·문서 검토·팀 관리·통계·재직증명서·파일 GC |
| `SALES` | 영업 | 영업 인력표·경력 계산기·프로필 조회·엑셀 내보내기 접근. 문서 검토 권한 없음 |
| `EMPLOYEE` | 사원 | 내 폴더·공용 폴더·검색·설정·이력서 템플릿 다운로드 접근 |
| `TEAM_LEADER` | (사용 중지) | MVP2에서 SALES로 대체. DB 호환을 위해 enum 유지. 신규 부여 불가 |

---

## 계정 상태 (UserStatus)

| enum 값 | 설명 |
|---------|------|
| `PENDING_EMAIL_VERIFICATION` | 회원가입 후 이메일 미인증 상태 |
| `PENDING_ADMIN_APPROVAL` | 이메일 인증 완료, 관리자 승인 대기 — **현재 미사용** (이메일 인증 즉시 ACTIVE로 전환) |
| `ACTIVE` | 정상 활성 계정 |
| `REJECTED` | 관리자가 계정 거절 — **현재 미사용** |
| `DISABLED` | 관리자가 비활성화. 로그인 불가, 기존 세션 즉시 만료 |

---

## 직급 (Position)

| enum 값 | 표시명 | 순서 |
|---------|--------|------|
| `REPRESENTATIVE` | 대표 | 1 |
| `EXECUTIVE_DIRECTOR` | 전무 | 2 |
| `MANAGING_DIRECTOR` | 상무 | 3 |
| `DIRECTOR` | 이사 | 4 |
| `GENERAL_MANAGER` | 부장 | 5 |
| `DEPUTY_GENERAL_MANAGER` | 차장 | 6 |
| `MANAGER` | 과장 | 7 |
| `ASSISTANT_MANAGER` | 대리 | 8 |
| `STAFF` | 사원 | 9 (기본값) |

---

## 문서 유형 (DocumentType)

| enum 값 | 표시명 | 활성 |
|---------|--------|------|
| `RESUME` | 이력서 | O |
| `CAREER_DESCRIPTION` | SW기술자 경력증명서 | O |
| `GRADUATION_CERTIFICATE` | 졸업증명서 | O |
| `LICENSE` | 정보처리기사 | O |
| `NATIONAL_PENSION_CERTIFICATE` | 국민연금가입증명서 | O |
| `HEALTH_INSURANCE_CERTIFICATE` | 건강보험가입증명서 | O |
| `HEALTH_INSURANCE_ELIGIBILITY` | 건강보험자격득실확인서 | O |
| `PROFILE_PHOTO` | 증명사진 | O |
| `SIGNATURE` | 서명 | O |
| `ETC` | 기타 | O |
| `HEALTH_INSURANCE_PROOF` | 건강보험료납부확인서 | X (deprecated, DB 보존용) |
| `EMPLOYMENT_CERTIFICATE` | 재직증명서 | X (deprecated, DB 보존용) |

---

## 문서 상태 (DocumentStatus)

| enum 값 | 설명 |
|---------|------|
| `ACTIVE` | 정상 활성 |
| `IN_TRASH` | 휴지통 상태 (미사용, 예약) |
| `DELETED` | soft-delete. `deleted_at`·`deleted_by` 기록. 파일은 GC 대상 |

---

## 문서 버전 검토 상태 (DocumentReviewStatus)

| enum 값 | 설명 |
|---------|------|
| `PENDING_REVIEW` | 업로드 직후 검토 대기 |
| `APPROVED` | 관리자 승인 완료 |
| `REJECTED` | 관리자 반려. `rejectReason` 기록 |

---

## 폴더 유형 (FolderType)

| enum 값 | 설명 |
|---------|------|
| `PERSONAL` | 개인 폴더. 본인·ADMIN·SALES·권한 부여된 사용자만 접근 |
| `SHARED_PUBLIC` | 전 사원 공용 폴더. 모든 인증 사용자 read. 업로드/삭제는 ADMIN만 |

---

## 권한 (Permission)

- `PermissionType.FOLDER_ACCESS` — 개인 폴더에 대한 read 접근 권한
- `PermissionTargetType.FOLDER` — 권한 대상이 폴더
- 관리자가 `/admin/users/{userId}/permissions/grant`로 부여, `/revoke`로 회수

---

## 직원 프로필 가용 상태 (AvailableStatus)

| enum 값 | 설명 |
|---------|------|
| `AVAILABLE` | 투입 가능 |
| `UNAVAILABLE` | 투입 불가 |
| `ON_PROJECT` | 프로젝트 진행 중 |

---

## 기타 핵심 용어

| 용어 | 정의 |
|------|------|
| **개인 폴더** | 회원가입 완료 시 자동 생성되는 본인 전용 폴더 |
| **공용 폴더** | `FolderType.SHARED_PUBLIC`. V207 시드로 생성. 전 사원 read |
| **번들 다운로드** | 영업이 체크한 여러 사원 프로필 문서를 ZIP으로 묶어 내려받는 기능 |
| **경력 계산기** | 날짜 구간 입력 → 중복 제거 → 총 경력 계산. 결과를 EmployeeProfile에 저장 |
| **재직증명서** | Python Flask 컨테이너(port 5001)가 DOCX·PDF 자동 생성 |
| **이력서 템플릿** | 관리자가 업로드한 DOCX 양식. 전 역할 다운로드 가능 |
| **파일 GC** | `DocumentFileGcService` 매일 02:00 cron. soft-delete 후 1시간 이상 지난 파일을 스토리지에서 삭제 |
| **고아 파일** | DB 참조는 없으나 스토리지에 남은 파일 (업로드 중단 등) |
| **썸네일** | PDF·이미지 문서의 미리보기 이미지. `ThumbnailService` 비동기 생성 |
| **프리셋** | 영업이 자주 쓰는 프로필 조회 컬럼 조합을 `column_view_preferences`에 저장한 것 |
| **프로젝트 팀** | `teams.project_team = true`인 팀. 영업 인력표에 표시 여부 관리 |
| **loginId** | 회사 이메일 앞부분(prefix) — `@{company-email-domain}` 제외 |
| **SSOT** | Single Source of Truth — 단일 출처 |
