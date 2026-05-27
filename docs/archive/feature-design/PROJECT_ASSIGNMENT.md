# 프로젝트 투입 관리 기능

> 관련 API 문서: `PROJECT_ASSIGNMENT_API.md` | 테스트 문서: `PROJECT_ASSIGNMENT_TEST.md`

---

## 기능 목적

직원이 어떤 프로젝트에 투입되어 있는지, 언제 시작해서 언제 끝나는지를 한 곳에서 관리한다.
영업·관리자가 인력 투입 현황을 즉시 파악하고, 종료 임박 배정에 대응할 수 있도록 돕는다.

---

## 핵심 개념

| 개념 | 설명 |
|------|------|
| **ProjectAssignment** | 직원 1명과 프로젝트 1건의 투입 관계. 기간이 겹쳐도 저장 허용(분할 투입 지원). |
| **AssignmentStatus** | PLANNED / ACTIVE / ENDED / CANCELLED 4가지 상태 |
| **현재 투입** | `status = ACTIVE` AND `startDate ≤ today ≤ endDate` |
| **투입 예정** | `status = PLANNED` AND `startDate > today` |
| **잔여일** | `endDate - today`. 종료됐으면 0. |
| **종료 임박** | `잔여일 ≤ 14일` |

---

## 데이터 모델

### project_assignments 테이블 (V216)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL PK | NOT NULL | |
| user_id | BIGINT FK | NOT NULL | users.id 참조 |
| project_name | VARCHAR(200) | NOT NULL | 프로젝트명 |
| client_name | VARCHAR(200) | | 고객사명 |
| role | VARCHAR(100) | | 역할 (개발자, PM 등) |
| start_date | DATE | NOT NULL | 투입 시작일 |
| end_date | DATE | NOT NULL | 투입 종료일 |
| allocation_rate | INTEGER | NOT NULL, 0~100 | 투입률(%) |
| status | VARCHAR(20) | NOT NULL | PLANNED / ACTIVE / ENDED / CANCELLED |
| memo | TEXT | | 비고 |
| created_at | TIMESTAMPTZ | | BaseEntity 자동 관리 |
| updated_at | TIMESTAMPTZ | | BaseEntity 자동 관리 |

**인덱스:** `idx_pa_user_id`, `idx_pa_status`, `idx_pa_dates`, `idx_pa_user_dates`

**CHECK 제약:** `end_date >= start_date`, `allocation_rate BETWEEN 0 AND 100`, status 값 4개

---

## 상태값 정의

| 상태 | 한글 | 설명 | 색상(UI) |
|------|------|------|---------|
| `PLANNED` | 투입 예정 | 아직 시작 전 | 파란색 |
| `ACTIVE` | 투입 중 | 현재 투입 중 | 초록색 |
| `ENDED` | 종료 | 수동으로 종료 처리 | 회색 |
| `CANCELLED` | 취소 | 취소된 배정 | 빨간색 |

---

## 투입 상태 계산 기준

### 등록 시 (`ProjectAssignment.create()`)

```
today < startDate  →  PLANNED
today >= startDate →  ACTIVE
```

ENDED·CANCELLED는 등록 시 자동 설정 불가. `update()` 또는 `cancel()`로만 전환.

### 인력표 "투입 정보" 컬럼 표시 기준

```
currentAssignments[userId] 존재  →  투입 중 (프로젝트명 + 고객사 + 잔여일)
nextAssignments[userId] 존재     →  투입 예정 (프로젝트명 + 시작일)
둘 다 없음                       →  미투입
```

`currentAssignments` = `findActiveOn(today)` 기반 (ACTIVE + 날짜 포함).  
`nextAssignments` = `findPlannedFrom(today)` 기반 (PLANNED + startDate > today, ASC 정렬 후 첫 번째).

같은 직원에 복수 배정이 있으면 `(a, b) -> a` merge 함수로 첫 번째 선택.

---

## 캘린더 표시 기준

- **그리드 셀**: CANCELLED 배정 제외. PLANNED / ACTIVE / ENDED 모두 표시.
- **기간 클리핑**: 배정 기간이 조회 월 경계를 넘어가면 해당 월 범위만 표시.
- **주 시작**: 일요일. `DayOfWeek.getValue() % 7` 로 오프셋 계산 (SUN=0, MON=1, SAT=6).
- **칩 색상**: `.chip-planned` (파랑), `.chip-active` (초록), `.chip-ended` (회색), `.chip-cancelled` (빨강).

---

## 대시보드 요약 기준

| 카드 | 계산 방식 |
|------|----------|
| 현재 투입 중 | `findActiveOn(today)` → distinct userId 수 |
| 이번달 투입 예정 | `findStartingBetween(mStart, mEnd)` 건수 (CANCELLED 제외) |
| 이번달 종료 예정 | `findEndingBetween(mStart, mEnd)` 건수 (CANCELLED 제외) |
| 현재 미투입 | ACTIVE 비ADMIN 직원 수 − 투입 중 직원 수 (최소 0) |
| 종료 임박 목록 | ACTIVE 배정 중 `endDate BETWEEN today AND today+14` (최대 5건 표시) |

ADMIN 역할은 `notDeployed` 계산에서 제외 (`u.getRole() != UserRole.ADMIN`).

---

## 중복 투입 처리 정책

같은 직원이 같은 기간에 여러 프로젝트에 투입될 수 있다 (분할 투입, 부분 투입 허용).

- **저장**: 항상 허용.
- **경고**: 겹치는 배정이 있으면 `overlapWarning` flash 속성으로 경고 메시지 표시.
- **차단 안 함**: 비즈니스 상 한 직원이 두 프로젝트에 50%씩 투입되는 경우 존재.

> ADR-015 참조 (docs/decisions.md).

---

## 접근 권한

| 기능 | ADMIN | SALES | EMPLOYEE |
|------|-------|-------|---------|
| 캘린더 조회 | ✅ | ✅ | ❌ |
| 인력표 투입 정보 조회 | ✅ | ✅ | ❌ |
| 대시보드 요약 카드 조회 | ✅ | ✅ | ✅ (숫자만) |
| 배정 등록·수정·삭제 | ✅ | ❌ | ❌ |

CRUD 권한 검증은 서비스 레이어 `requireAdmin(UserRole)` 에서 수행 (컨트롤러 직접 비교 금지 — ADR-006).

---

## 향후 확장 계획

- 투입률 합계 100% 초과 경고 (현재는 합계 검증 없음)
- 투입 종료 D-7 자동 이메일 알림 (`@Scheduled`)
- 월간 투입 현황 Excel 출력 (`SalesProfileExporter` 확장)
- 투입 통계 차트 (팀별 투입률, 월별 추이)
