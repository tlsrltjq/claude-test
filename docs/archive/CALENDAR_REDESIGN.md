# 투입 캘린더 개편 기획

> 작성일: 2026-05-27 | 상태: 계획 (미구현)
> 구현 전 이 문서를 SSOT로 사용. 변경 사항은 여기에 먼저 반영.

---

## 1. 현재 구조 (As-Is)

**모델**: 사람 중심 (person-centric)
- `project_assignments` 한 테이블에 project_name·client_name을 비정규화로 저장
- 사람 한 명 + 프로젝트 하나 = 행 1개 (Project 엔티티 없음)
- 캘린더: 날짜 칸마다 "이름·프로젝트명" 칩 표시

**문제**:
- 같은 프로젝트에 여러 명을 등록하려면 한 명씩 따로 등록해야 함
- 프로젝트 전체 정보(고객사, 기간, 메모)를 수정하려면 모든 행을 각각 수정해야 함
- 캘린더가 프로젝트 단위가 아니라 사람 단위로 표시됨

---

## 2. 목표 구조 (To-Be)

**모델**: 프로젝트 중심 (project-centric)
- `projects` 테이블: 프로젝트 메타(이름·고객사·기간·메모·상태)
- `project_assignments` 테이블: 프로젝트 ↔ 사람 연결 (개인별 기간·역할 포함)
- 캘린더: 프로젝트 바(bar) 형태로 기간 전체에 걸쳐 표시

---

## 3. 새 기능 요구사항

### 3-1. 프로젝트 등록
- **접근**: ADMIN 전용 (영업 권한 부여는 향후 결정)
- **입력 필드**:
  - 프로젝트명 (필수)
  - 고객사 (선택)
  - 시작일 / 종료일 (필수)
  - 메모 (선택, 나중에 채울 수 있음)
- **직원 선택**: 인력표 인원 체크박스 목록 → 선택된 인원 일괄 등록
  - 기본 투입 기간: 프로젝트 시작~종료일
  - 기본 역할: 빈칸 (나중에 채울 수 있음)

### 3-2. 캘린더 뷰 (재설계)
- 프로젝트를 가로 바(bar)로 표시 — 기간만큼 연속으로 이어짐
- 같은 날 여러 프로젝트 → 여러 바를 세로로 쌓아 표시
- 바 색상: 프로젝트마다 고유 색상(상태별 색상 보조)
- 바 클릭 → 프로젝트 상세 페이지로 이동
- 이번달 프로젝트 목록 테이블 하단 유지 (필터 포함)

### 3-3. 프로젝트 상세 페이지
- **경로**: `GET /sales/projects/{id}`
- **표시 정보**:
  - 프로젝트명, 고객사, 전체 기간, 메모, 상태
  - ADMIN: 수정 버튼 (인라인 편집 또는 수정 폼)
- **투입 직원 테이블**:

  | 이름 | 역할 | 투입 시작일 | 투입 종료일 | 상태 | 조작 |
  |------|------|------------|------------|------|------|
  | 홍길동 | (빈칸) | 2026-06-01 | 2026-08-31 | ACTIVE | 수정/삭제 |

  - 역할: 빈칸으로 시작, ADMIN이 수정 가능
  - 개인 시작일·종료일: 기본값은 프로젝트 기간, 개별 조정 가능
  - 수정·삭제: ADMIN 전용

- **직원 추가 버튼** (ADMIN): 현재 미투입 활성 직원 선택 → 프로젝트에 추가

### 3-4. 프로젝트 일정 조정 (ADMIN)
- 프로젝트 전체 기간 변경 (startDate / endDate)
- 개인별 투입 기간 개별 변경
- 프로젝트 상태 수동 변경 (PLANNED → ACTIVE → ENDED / CANCELLED)

### 3-5. 삭제
- 프로젝트 삭제: ADMIN 전용. 소속 모든 assignments 연쇄 삭제
- 개별 투입 해제: ADMIN 전용. 해당 assignment만 삭제

---

## 4. DB 스키마 변경

### 4-1. 신규 테이블: `projects`

```sql
-- V220
CREATE TABLE projects (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    client_name VARCHAR(200),
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    memo        TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PLANNED',
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT chk_proj_status CHECK (status IN ('PLANNED','ACTIVE','ENDED','CANCELLED')),
    CONSTRAINT chk_proj_dates  CHECK (end_date >= start_date)
);

CREATE INDEX idx_proj_status ON projects(status);
CREATE INDEX idx_proj_dates  ON projects(start_date, end_date);
```

### 4-2. `project_assignments` 컬럼 변경

```sql
-- V221: project_id 컬럼 추가
ALTER TABLE project_assignments
    ADD COLUMN project_id BIGINT REFERENCES projects(id);

-- V222: 기존 데이터 마이그레이션
-- 기존 rows의 project_name/client_name/start_date/end_date로 projects 행 생성 후 project_id 연결
INSERT INTO projects (name, client_name, start_date, end_date, status, memo)
SELECT DISTINCT project_name, client_name, start_date, end_date, status, memo
FROM project_assignments WHERE project_id IS NULL;

UPDATE project_assignments pa
SET project_id = (
    SELECT p.id FROM projects p
    WHERE p.name = pa.project_name
      AND COALESCE(p.client_name,'') = COALESCE(pa.client_name,'')
      AND p.start_date = pa.start_date
      AND p.end_date   = pa.end_date
    LIMIT 1
)
WHERE pa.project_id IS NULL;

-- V223: 비정규화 컬럼 제거 + project_id NOT NULL 적용
ALTER TABLE project_assignments
    ALTER COLUMN project_id SET NOT NULL,
    DROP COLUMN project_name,
    DROP COLUMN client_name,
    DROP COLUMN memo;   -- 메모는 projects 테이블로 이동
```

### 4-3. 최종 `project_assignments` 컬럼 (V223 이후)

| 컬럼 | 타입 | 변경 |
|------|------|------|
| id | BIGSERIAL | 유지 |
| project_id | BIGINT FK → projects | **신규** |
| user_id | BIGINT FK → users | 유지 |
| role | VARCHAR(100) | 유지 (빈칸 허용) |
| start_date | DATE | 유지 (개인별 기간) |
| end_date | DATE | 유지 |
| status | VARCHAR(20) | 유지 |
| created_at, updated_at, version | — | 유지 |
| ~~project_name~~ | — | **제거** |
| ~~client_name~~ | — | **제거** |
| ~~memo~~ | — | **제거** (projects로 이동) |

---

## 5. API / 라우트 설계

| 메서드 | 경로 | 역할 | 설명 |
|--------|------|------|------|
| GET | `/sales/calendar` | ADMIN·SALES | 캘린더 (재설계) |
| GET | `/sales/projects/{id}` | ADMIN·SALES | 프로젝트 상세 |
| POST | `/admin/projects` | ADMIN | 프로젝트 생성 + 인원 일괄 등록 |
| POST | `/admin/projects/{id}/update` | ADMIN | 프로젝트 정보 수정 |
| POST | `/admin/projects/{id}/delete` | ADMIN | 프로젝트 삭제 (cascade) |
| POST | `/admin/projects/{id}/members` | ADMIN | 직원 추가 투입 |
| POST | `/admin/projects/{id}/members/{aId}/update` | ADMIN | 개인 기간·역할 수정 |
| POST | `/admin/projects/{id}/members/{aId}/delete` | ADMIN | 개인 투입 해제 |

> 기존 `/sales/assignments/**` 경로는 V220~V223 마이그레이션 완료 후 제거 또는 내부 redirect 처리.

---

## 6. Java 컴포넌트 계획

### 신규
- `project/entity/Project.java` — 프로젝트 엔티티
- `project/repository/ProjectRepository.java`
- `project/service/ProjectService.java` — CRUD, 인원 일괄 등록, 권한 검증
- `project/controller/ProjectController.java` — `/admin/projects/**`, `/sales/projects/**`
- `project/dto/ProjectCreateRequest.java` — 이름·고객사·기간·selectedUserIds[]
- `project/dto/ProjectUpdateRequest.java`

### 수정
- `project/entity/ProjectAssignment.java` — `project` 필드(ManyToOne) 추가, project_name·client_name·memo 필드 제거
- `project/repository/ProjectAssignmentRepository.java` — findForMonth → project 기준 조회로 변경
- `project/service/ProjectAssignmentService.java` — ProjectService로 역할 분리
- `project/controller/ProjectAssignmentController.java` — calendar GET 유지, 나머지 ProjectController로 이관
- `project/controller/CalendarGridBuilder.java` — Project 기반 dayMap 생성으로 변경

### 삭제 (마이그레이션 완료 후)
- `project/controller/ProjectAssignmentController.java`의 POST `/sales/assignments/**` 핸들러

---

## 7. 프론트엔드 변경 계획

### 7-1. 캘린더 바(bar) 렌더링
- 현재: 날짜 칸 안에 칩(chip) 표시
- 변경: 주(week) 행 아래 프로젝트 바 행 추가
  ```
  [주 행: 1일 2일 3일 4일 5일 6일 7일]
  [프로젝트 A바: ━━━━━━━━━━━━━━━━━━━━]  (클릭 가능)
  [프로젝트 B바:      ━━━━━━━━━━━━━━]
  ```
- 바 너비: `(프로젝트 종료일 - 시작일 + 1) / 7 * 100%` (주 내 클리핑)
- 바 색상: 프로젝트 id 기반 색상 팔레트 (10가지 순환)

### 7-2. 신규 화면
- **프로젝트 등록 모달**: 이름·고객사·기간 + 직원 체크박스 목록
- **프로젝트 상세 페이지** (`sales/project-detail.html`): 프로젝트 정보 + 투입 직원 테이블 + 직원 추가 모달

### 7-3. 기존 화면 변경
- `sales/calendar.html`: 캘린더 그리드 렌더링 방식 변경, 등록 버튼 → "프로젝트 등록"
- 하단 목록 테이블: 프로젝트 단위로 grouping (project_name 하나에 투입 인원 수 표시)

---

## 8. 구현 순서 (단계별)

| 단계 | 내용 | 마이그레이션 |
|------|------|------------|
| 1 | `projects` 테이블 + `Project` 엔티티 + Repository | V220 |
| 2 | `project_id` 컬럼 추가 + 기존 데이터 마이그레이션 | V221·V222 |
| 3 | `ProjectService` — 생성/수정/삭제/인원 관리 | — |
| 4 | `ProjectController` — CRUD API | — |
| 5 | `ProjectAssignment` 엔티티 정리 + 비정규화 컬럼 제거 | V223 |
| 6 | 캘린더 렌더링 재설계 (CalendarGridBuilder → Project 기반) | — |
| 7 | `project-detail.html` 신규 화면 | — |
| 8 | `calendar.html` 바(bar) 렌더링 적용 | — |
| 9 | 기존 `/sales/assignments/**` 핸들러 정리 | — |
| 10 | 테스트 (엔티티·서비스·컨트롤러) | — |

---

## 9. 확장성 고려사항

| 항목 | 현재 설계 | 향후 확장 |
|------|----------|----------|
| 직원 권한 | ADMIN 전용 | `ProjectService`에 권한 플래그 파라미터 추가로 SALES 권한 부여 가능 |
| 프로젝트 필드 | 이름·고객사·기간·메모 | `projects` 테이블에 컬럼 추가 (계약금액, 계약번호, 담당 PM 등) |
| 개인 필드 | 역할·기간 | `project_assignments`에 컬럼 추가 (단가, 계약 형태 등) |
| 알림 | 없음 | 투입 시작/종료 임박 알림 (`@Scheduled` + 이메일) |
| 상태 자동화 | 없음 | 날짜 기반 Project.status 자동 갱신 스케줄러 |
| 캘린더 뷰 | 월별 | 주별·분기별 뷰 추가 가능 |

---

## 10. 권한 구조

| 액션 | ADMIN | SALES | EMPLOYEE |
|------|-------|-------|---------|
| 캘린더 조회 | ✅ | ✅ | ❌ |
| 프로젝트 상세 조회 | ✅ | ✅ | ❌ |
| 프로젝트 생성 | ✅ | ❌ | ❌ |
| 프로젝트 수정·삭제 | ✅ | ❌ | ❌ |
| 직원 추가·투입 해제 | ✅ | ❌ | ❌ |
| 개인 기간·역할 수정 | ✅ | ❌ | ❌ |

> 영업(SALES)에 쓰기 권한 부여가 필요하면 `ProjectService`의 `requireAdmin()` 호출 지점을 수정하면 됨.
