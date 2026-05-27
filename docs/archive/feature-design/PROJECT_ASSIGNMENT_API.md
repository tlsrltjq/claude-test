# 프로젝트 투입 관리 API

> 이 API는 Thymeleaf 폼 기반 웹 API다. REST JSON API가 아니므로 요청은 form-encoded, 응답은 HTML 또는 리다이렉트.
> 모든 POST 요청에 CSRF 토큰 필요 (`_csrf` 히든 필드).

---

## API 목록

| 메서드 | URL | 설명 | 접근 권한 |
|--------|-----|------|----------|
| GET | `/sales/calendar` | 캘린더 + 배정 목록 페이지 | ADMIN, SALES |
| POST | `/sales/assignments` | 배정 등록 | ADMIN만 (서비스 레이어 검증) |
| POST | `/sales/assignments/{id}` | 배정 수정 | ADMIN만 |
| POST | `/sales/assignments/{id}/delete` | 배정 삭제 | ADMIN만 |
| GET | `/sales/profiles` | 인력표 (투입 정보 컬럼 포함) | ADMIN, SALES |
| GET | `/dashboard` | 대시보드 (투입 요약 카드) | 전체 인증 사용자 |

---

## GET /sales/calendar

### 요청 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| year | Integer | 아니오 | 조회 연도 (없으면 현재 연도) |
| month | Integer | 아니오 | 조회 월 (없으면 현재 월) |
| q | String | 아니오 | 직원명 필터 |
| project | String | 아니오 | 프로젝트명 필터 |
| status | AssignmentStatus | 아니오 | 상태 필터 (PLANNED/ACTIVE/ENDED/CANCELLED) |

### 모델 속성 (Thymeleaf)

| 속성명 | 타입 | 설명 |
|--------|------|------|
| ym | YearMonth | 현재 조회 월 |
| prev | YearMonth | 이전 월 |
| next | YearMonth | 다음 월 |
| today | LocalDate | 오늘 날짜 |
| weeks | `List<List<LocalDate>>` | 7열 주 그리드 (null=해당 월 밖) |
| dayMap | `Map<LocalDate, List<PA>>` | 날짜별 배정 목록 (CANCELLED 제외) |
| allAssignments | `List<ProjectAssignment>` | 필터 적용 전체 목록 |
| allStatuses | `AssignmentStatus[]` | 상태 필터 드롭다운용 |
| assignableUsers | `List<User>` | ADMIN 등록 폼용 직원 목록 |
| currentUser | User | 현재 로그인 사용자 |

### HTTP 응답

- 성공: `200 OK` + HTML 렌더링
- 미인증: `302 → /login` (운영) / `401` (@WebMvcTest)
- EMPLOYEE 접근: `403 Forbidden`

---

## POST /sales/assignments (배정 등록)

### 요청 파라미터 (form-encoded)

| 필드 | 타입 | 필수 | 검증 |
|------|------|------|------|
| _csrf | String | **필수** | CSRF 토큰 |
| userId | Long | **필수** | 대상 직원 ID |
| projectName | String | **필수** | 최대 200자, 공백 불가 |
| clientName | String | 아니오 | 최대 200자 |
| role | String | 아니오 | 최대 100자 |
| startDate | LocalDate | **필수** | yyyy-MM-dd 형식 |
| endDate | LocalDate | **필수** | yyyy-MM-dd, startDate 이후 |
| allocationRate | Integer | 아니오 | 기본값 100, 범위 0~100 |
| status | AssignmentStatus | 아니오 | create 시 자동 계산되므로 무시됨 |
| memo | String | 아니오 | TEXT |

### 상태 자동 계산

등록 시 `status`는 폼 값이 아닌 `create()` 팩토리에서 자동 결정:
- `today < startDate` → PLANNED
- `today >= startDate` → ACTIVE

### HTTP 응답

| 상황 | 응답 |
|------|------|
| 성공 | `302 → /sales/calendar` + `success` flash |
| 겹치는 배정 존재 (저장은 됨) | `302 → /sales/calendar` + `overlapWarning` flash |
| 입력 오류 | `302 → /sales/calendar` + `error` flash |
| CSRF 없음 | `403 Forbidden` |
| 비ADMIN 호출 | `403 Forbidden` |

### 오류 케이스

| 케이스 | flash 속성 | 메시지 |
|--------|-----------|--------|
| userId 없음 | `error` | "대상 직원을 선택해야 합니다." |
| projectName 없음 | `error` | "프로젝트명은 필수입니다." |
| 날짜 없음 | `error` | "투입 기간을 입력해야 합니다." |
| endDate < startDate | `error` | "종료일은 시작일 이후여야 합니다." |
| allocationRate 범위 초과 | `error` | "투입률은 0~100 사이여야 합니다." |
| 비활성화 직원 | `error` | "비활성화된 직원에게는 배정을 생성할 수 없습니다." |

---

## POST /sales/assignments/{id} (배정 수정)

### 경로 파라미터

| 파라미터 | 설명 |
|---------|------|
| id | 수정할 배정 ID |

### 요청 파라미터

등록과 동일. 수정 시 `status` 필드는 폼 값이 반영됨 (`update()` 에 직접 전달).

### HTTP 응답

| 상황 | 응답 |
|------|------|
| 성공 | `302 → /sales/calendar` + `success` flash |
| 존재하지 않는 id | `302 → /sales/calendar` + `error` flash |
| CSRF 없음 | `403 Forbidden` |
| 비ADMIN 호출 | `403 Forbidden` |

---

## POST /sales/assignments/{id}/delete (배정 삭제)

### 경로 파라미터

| 파라미터 | 설명 |
|---------|------|
| id | 삭제할 배정 ID |

### HTTP 응답

| 상황 | 응답 |
|------|------|
| 성공 | `302 → /sales/calendar` + `success` flash |
| 존재하지 않는 id | `302 → /sales/calendar` + `error` flash |
| CSRF 없음 | `403 Forbidden` |
| 비ADMIN 호출 | `403 Forbidden` |

---

## GET /sales/profiles (인력표 — 투입 정보 포함)

### 추가 모델 속성 (투입 관련)

| 속성명 | 타입 | 설명 |
|--------|------|------|
| currentAssignments | `Map<Long, ProjectAssignment>` | userId → 현재 ACTIVE 배정 |
| nextAssignments | `Map<Long, ProjectAssignment>` | userId → 가장 빠른 PLANNED 배정 |

### 투입 정보 컬럼 (data-col="assignmentInfo") 표시 규칙

```
currentAssignments[userId] 있음 → "투입 중" 배지 + 프로젝트명 + 고객사 + 종료일 + 잔여일
                                   (잔여일 ≤ 14: 빨간 텍스트)
nextAssignments[userId] 있음    → "투입 예정" 배지 + 프로젝트명 + 시작일
둘 다 없음                      → "미투입" 배지
```

---

## GET /dashboard (대시보드 투입 요약)

### 추가 모델 속성 (투입 관련)

| 속성명 | 타입 | 설명 |
|--------|------|------|
| deployStats | DeployStats | 4개 숫자 요약 |
| endingSoon | `List<ProjectAssignment>` | 14일 이내 종료 배정 목록 |

### DeployStats 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| startingThisMonth | long | 이번달 투입 시작 건수 |
| endingThisMonth | long | 이번달 투입 종료 건수 |
| currentlyDeployed | long | 현재 투입 중 직원 수 (distinct) |
| notDeployed | long | 현재 미투입 직원 수 (≥ 0) |

---

## 예시 시나리오

### 배정 등록 (ADMIN)

```
POST /sales/assignments
Content-Type: application/x-www-form-urlencoded

_csrf=<token>
&userId=5
&projectName=스마트시티 플랫폼
&clientName=서울시
&role=백엔드 개발자
&startDate=2026-06-01
&endDate=2026-11-30
&allocationRate=100
&memo=API 서버 개발 담당

→ 302 /sales/calendar
→ flash: success = "배정이 등록되었습니다."
```

### 겹치는 배정 경고

```
POST /sales/assignments
(같은 직원, 겹치는 기간)

→ 302 /sales/calendar
→ flash: overlapWarning = "겹치는 배정이 1건 있습니다. 확인 후 조정하세요."
→ flash: success = "배정이 등록되었습니다."  (경고와 함께 저장 성공)
```

### 빈 결과 처리

```
GET /sales/calendar?q=없는직원이름

→ 200 OK
→ allAssignments = []  (빈 목록 — 빈 상태 안내 문구 표시)
```
