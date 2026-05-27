# 프로젝트 투입 관리 — 테스트 문서

> 관련 문서: `PROJECT_ASSIGNMENT.md` (기능 스펙) | `PROJECT_ASSIGNMENT_API.md` (API 스펙)

---

## 테스트 전략

| 계층 | 도구 | 범위 |
|------|------|------|
| 엔티티 순수 로직 | JUnit 5 (Spring 없음) | 날짜 계산, 상태 결정, 경계값 |
| DTO 검증 | JUnit 5 (Spring 없음) | `validate()` 5가지 오류 규칙 |
| 그리드 빌더 | JUnit 5 (package-private 접근) | 요일 오프셋, 클리핑, CANCELLED 제외 |
| 서비스 통합 | JUnit 5 + Mockito | CRUD 흐름, 권한, 통계, 선택 로직 |
| 컨트롤러 슬라이스 | @WebMvcTest + MockMvc | HTTP 상태, CSRF, 리다이렉트, flash |
| 보안 정적 분석 | security-lint.sh | 역할 비교 위치, CSRF 활성화 |
| 수동 QA | qa-checklist.md | UI 동작, 역할 기반 접근, 브라우저 |

**@WebMvcTest 역할 기반 접근 제한 사항**: `user()` 포스트프로세서는 SecurityFilterChain
역할 제한을 우회한다. `/sales/**` EMPLOYEE 접근 거부는 수동 QA로 보완 (SecurityAccessTest 주석 참조).

---

## 단위 테스트 목록

### ProjectAssignmentTest
파일: `src/test/.../project/entity/ProjectAssignmentTest.java`

| 테스트 메서드 | 스펙 | 검증 내용 |
|-------------|------|----------|
| 시작일이_오늘보다_미래이면_PLANNED | PA-002 | create() 상태 자동 결정 |
| 시작일이_오늘이면_ACTIVE | PA-002 | 시작일 당일 경계값 |
| 시작일이_과거이면_ACTIVE | PA-002 | 과거 시작일 처리 |
| cancel_호출_후_CANCELLED | PA-002 | 취소 상태 전환 |
| update_호출_시_모든_필드_반영 | PA-006 | 8개 필드 동시 검증 |
| 잔여일_endDate가_오늘이면_0 | PA-002 | remainingDays() 당일 경계 |
| 잔여일_endDate가_내일이면_1 | PA-002 | remainingDays() 정상 계산 |
| 잔여일_endDate가_이미_지나면_0 | PA-002 | 음수 방지 |
| 잔여일_14일이내_강조_대상 | PA-002 | 강조 조건 진입 |
| 잔여일_14일_초과이면_강조_비대상 | PA-002 | 강조 조건 미진입 |
| isActiveOn_시작일_전날_false | PA-002 | 날짜 포함 범위 경계 |
| isActiveOn_시작일_당일_true | PA-002 | startDate 포함 |
| isActiveOn_기간_중간_true | PA-002 | 기간 중간 확인 |
| isActiveOn_종료일_당일_true | PA-002 | endDate 포함 |
| isActiveOn_종료일_다음날_false | PA-002 | endDate 다음날 제외 |

### ProjectAssignmentRequestTest
파일: `src/test/.../project/dto/ProjectAssignmentRequestTest.java`

| 테스트 메서드 | 스펙 | 검증 내용 |
|-------------|------|----------|
| 정상_입력이면_예외없음 | PA-007 | 정상 케이스 |
| allocationRate_경계값_0_허용 | PA-007 | 최솟값 허용 |
| allocationRate_경계값_100_허용 | PA-007 | 최댓값 허용 |
| startDate와_endDate가_같은날이면_허용 | PA-007 | 당일 배정 허용 |
| userId_null이면_예외 | PA-007 | 필수 필드 검증 |
| projectName_null이면_예외 | PA-007 | 필수 필드 검증 |
| projectName_공백이면_예외 | PA-007 | 공백 문자열 검증 |
| startDate_null이면_예외 | PA-007 | 날짜 필수 검증 |
| endDate_null이면_예외 | PA-007 | 날짜 필수 검증 |
| endDate가_startDate보다_이전이면_예외 | PA-007 | 날짜 순서 검증 |
| allocationRate_음수이면_예외 | PA-007 | 하한값 초과 |
| allocationRate_101이면_예외 | PA-007 | 상한값 초과 |

---

## 통합 테스트 목록

### ProjectAssignmentServiceTest
파일: `src/test/.../project/service/ProjectAssignmentServiceTest.java`

| 테스트 메서드 | 스펙 | 검증 내용 |
|-------------|------|----------|
| ADMIN이_배정_등록하면_저장되고_감사_로그_기록 | PA-001 | 정상 등록 흐름 |
| ADMIN이_아니면_배정_등록_시_403 | PA-009 | 권한 검증 |
| 비활성화_직원에게_배정_등록_시_예외 | PA-001 | 예외 케이스 |
| 존재하지_않는_직원에게_배정_등록_시_예외 | PA-001 | 예외 케이스 |
| 직원_투입이력_전체_조회 | PA-005 | findByUserId 위임 |
| 월간_투입목록_필터없이_조회 | PA-003 | getMonthlyAssignments |
| 월간_투입목록_직원명_필터_적용 | PA-003 | 필터 로직 검증 |
| 종료_임박_배정_조회_14일_기준 | PA-004 | findEndingSoon 위임 확인 |
| ADMIN이_배정_수정하면_감사_로그_기록 | PA-001 | 수정 + 감사 로그 |
| ADMIN이_아니면_배정_수정_시_403 | PA-009 | 권한 검증 |
| 존재하지_않는_배정_수정_시_예외 | PA-001 | 예외 케이스 |
| ADMIN이_배정_삭제하면_감사_로그_기록 | PA-001 | 삭제 + 감사 로그 |
| ADMIN이_아니면_배정_삭제_시_403 | PA-009 | 권한 검증 |
| 존재하지_않는_배정_삭제_시_예외 | PA-001 | 예외 케이스 |
| 통계_ADMIN_직원은_미투입_계산에서_제외 | PA-004 | notDeployed 계산 |
| 통계_미투입은_음수가_되지_않음 | PA-004 | max(0, ...) 보호 |
| 통계_현재_투입_중_직원_수는_distinct_처리 | PA-004 | distinct userId |
| 현재_투입이_있으면_현재_배정_우선_선택 | PA-005 | 투입 정보 선택 로직 |
| 현재_투입이_없고_예정이_있으면_가장_빠른_예정_선택 | PA-005 | ASC 정렬 후 첫 번째 |
| 현재_투입도_예정도_없으면_빈_맵_반환 | PA-005 | 미투입 처리 |
| 여러_직원의_배정이_각_직원별로_한_건씩_선택됨 | PA-005 | 다중 직원 시나리오 |
| 겹치는_배정_확인_요청이_repository로_위임됨 | PA-008 | checkOverlap 위임 |

---

## API 테스트 목록

### CalendarGridBuilderTest
파일: `src/test/.../project/controller/CalendarGridBuilderTest.java`

| 테스트 메서드 | 스펙 | 검증 내용 |
|-------------|------|----------|
| 월_1일이_일요일이면_오프셋_0 | PA-003 | 오프셋 계산 (2025-06-01=일) |
| 월_1일이_월요일이면_오프셋_1 | PA-003 | 오프셋 계산 (2025-09-01=월) |
| 월_1일이_토요일이면_오프셋_6 | PA-003 | 오프셋 계산 (2025-02-01=토) |
| 각_주는_항상_7개_셀 | PA-003 | 주 크기 불변 조건 |
| 이십팔일_달_일요일_시작이면_정확히_4주 | PA-003 | 최소 주 수 (2015-02) |
| 월의_모든_날짜가_정확히_한_번_포함됨 | PA-003 | 날짜 누락·중복 없음 |
| CANCELLED_배정은_dayMap에_포함되지_않음 | PA-003 | CANCELLED 제외 규칙 |
| 배정_시작일이_월_이전이면_1일부터_클리핑 | PA-003 | 시작일 클리핑 |
| 배정_종료일이_월_이후이면_말일까지_클리핑 | PA-003 | 종료일 클리핑 |
| 배정이_월_전체를_감싸면_모든_날짜에_포함 | PA-003 | 전체 월 포함 |
| ENDED_배정은_dayMap에_포함됨 | PA-003 | ENDED 포함 규칙 |

### ProjectAssignmentControllerTest
파일: `src/test/.../project/controller/ProjectAssignmentControllerTest.java`

| 테스트 메서드 | 스펙 | 검증 내용 |
|-------------|------|----------|
| ADMIN_캘린더_페이지_접근_성공 | PA-003 | 200 OK |
| SALES_캘린더_페이지_접근_성공 | PA-003 | 200 OK |
| 미인증_캘린더_페이지_접근_거부 | PA-010 | 4xx |
| 캘린더_년월_파라미터로_특정_월_조회 | PA-003 | year/month 파라미터 처리 |
| CSRF_토큰_없이_배정_등록_요청_거부 | PA-010 | 403 Forbidden |
| CSRF_토큰_없이_배정_삭제_요청_거부 | PA-010 | 403 Forbidden |
| CSRF_토큰_없이_배정_수정_요청_거부 | PA-010 | 403 Forbidden |
| ADMIN_CSRF_포함_배정_등록_성공_후_리다이렉트 | PA-001 | 302 + redirectedUrl |
| SALES_CSRF_포함_배정_등록_시_서비스가_403_반환 | PA-009 | 403 Forbidden |
| 겹치는_배정이_있으면_등록_후_경고_플래시_속성_설정 | PA-008 | overlapWarning flash |
| 입력_오류이면_에러_플래시_속성_설정 | PA-007 | error flash |
| ADMIN_CSRF_포함_배정_수정_성공 | PA-001 | 302 redirect |
| ADMIN_CSRF_포함_배정_삭제_성공 | PA-001 | 302 redirect |

---

## 수동 테스트 체크리스트

### 캘린더 페이지

- [ ] SALES 계정 로그인 → `/sales/calendar` 접근 성공, 배정 등록·수정·삭제 버튼 미표시
- [ ] ADMIN 계정 로그인 → 등록·수정·삭제 버튼 모두 표시
- [ ] EMPLOYEE 계정 → `/sales/calendar` 접근 시 403 또는 로그인 리다이렉트
- [ ] 등록 모달 — 직원 선택 없이 제출 시 브라우저 유효성 검사 오류
- [ ] 수정 모달 — 칩/목록 수정 버튼 클릭 시 기존 값 pre-populated 확인
- [ ] 삭제 폼 — confirm 창 취소 시 삭제 실행 안 됨
- [ ] 겹치는 배정 등록 시 경고 배너 표시, 배정은 저장됨
- [ ] 월 이동 버튼으로 이전·다음 월 조회, 필터 파라미터 유지됨
- [ ] 칩 클릭 → 목록으로 스크롤 + 2초 하이라이트

### 인력표

- [ ] "투입 정보" 컬럼 기본 숨김 상태 확인
- [ ] 컬럼 설정에서 체크 → 투입 정보 컬럼 표시
- [ ] 투입 중 직원: "투입 중" 배지 + 프로젝트명 + 종료일 표시
- [ ] 잔여일 ≤ 14일: 날짜 텍스트가 빨간색으로 강조
- [ ] 투입 예정 직원: "투입 예정" 배지 + 시작일 표시
- [ ] 미투입 직원: "미투입" 배지 표시
- [ ] 프리셋 저장 → 적용 시 assignmentInfo 컬럼도 포함 확인

### 대시보드

- [ ] "현재 투입 중" 숫자 클릭 → 캘린더 ACTIVE 필터 페이지 이동
- [ ] "이번달 투입 예정" 숫자 클릭 → 캘린더 PLANNED 필터 페이지 이동
- [ ] 종료 임박 목록 최대 5건 표시, 6건 이상이면 "+N건 더" 레이블
- [ ] 종료 임박 잔여일 ≤ 7일: 빨간 배지, 8~14일: 주황 배지
- [ ] "캘린더에서 전체 보기 →" 링크 클릭 → `/sales/calendar` 이동
- [ ] EMPLOYEE 계정: 투입 요약 카드 섹션 미표시 (SALES/ADMIN만 표시)

### 보안

- [ ] CSRF 토큰 없는 POST 요청 → 403 (curl 또는 브라우저 DevTools)
- [ ] 비ADMIN으로 배정 등록 API 직접 호출 → 403

---

## 스펙-테스트 연결표

| 스펙 ID | 기능 | 검증 방법 | 테스트 파일 | 상태 |
|--------|------|----------|------------|------|
| PA-001 | 투입 등록·수정·삭제 (CRUD) | 통합 테스트 | ProjectAssignmentServiceTest | ✅ 완료 |
| PA-002 | 투입 상태 계산 + 날짜 로직 | 단위 테스트 | ProjectAssignmentTest | ✅ 완료 |
| PA-003 | 캘린더 조회 + 그리드 렌더링 | API + 그리드 테스트 | ProjectAssignmentControllerTest, CalendarGridBuilderTest | ✅ 완료 |
| PA-004 | 대시보드 요약 집계 | 통합 테스트 | ProjectAssignmentServiceTest | ✅ 완료 |
| PA-005 | 인력표 투입 정보 선택 로직 | 통합 테스트 | ProjectAssignmentServiceTest | ✅ 완료 |
| PA-006 | 배정 수정 (update) | 단위 테스트 | ProjectAssignmentTest | ✅ 완료 |
| PA-007 | 입력 유효성 검증 | 단위 테스트 | ProjectAssignmentRequestTest | ✅ 완료 |
| PA-008 | 중복 배정 경고 처리 | 통합 + API 테스트 | ProjectAssignmentServiceTest, ProjectAssignmentControllerTest | ✅ 완료 |
| PA-009 | ADMIN 권한 검증 | 통합 + API 테스트 | ProjectAssignmentServiceTest, ProjectAssignmentControllerTest | ✅ 완료 |
| PA-010 | CSRF + 인증 보안 | API 테스트 + 보안 린트 | ProjectAssignmentControllerTest, security-lint.sh | ✅ 완료 |
| PA-011 | EMPLOYEE 역할 접근 거부 (/sales/**) | 수동 QA | qa-checklist.md | 🔲 수동 |

---

## 완료 기준

| 항목 | 기준 |
|------|------|
| 전체 테스트 | `./gradlew test` BUILD SUCCESSFUL |
| 보안 정적 분석 | `bash scripts/security-lint.sh` 15/15 PASS |
| 날짜 계산 | remainingDays, isActiveOn, create 상태 결정 커버 |
| 역할 검증 | ADMIN/비ADMIN 분기 서비스 테스트 커버 |
| 그리드 로직 | 요일 오프셋 3가지 + 월 경계 클리핑 커버 |
| CSRF | 컨트롤러 슬라이스에서 POST 무토큰 → 403 확인 |
| 수동 QA | EMPLOYEE 역할 접근 거부 + UI 동작 실기 확인 |

---

## 테스트 실행 명령어

```bash
# 투입 관리 테스트 전체
./gradlew test --tests "com.eactive.resourcehub.project.*"

# 엔티티 단위 테스트
./gradlew test --tests "com.eactive.resourcehub.project.entity.ProjectAssignmentTest"

# DTO 검증 테스트
./gradlew test --tests "com.eactive.resourcehub.project.dto.ProjectAssignmentRequestTest"

# 그리드 빌더 테스트
./gradlew test --tests "com.eactive.resourcehub.project.controller.CalendarGridBuilderTest"

# 서비스 통합 테스트
./gradlew test --tests "com.eactive.resourcehub.project.service.ProjectAssignmentServiceTest"

# 컨트롤러 슬라이스 테스트
./gradlew test --tests "com.eactive.resourcehub.project.controller.ProjectAssignmentControllerTest"

# 전체 테스트 + 보안 린트
./gradlew test && bash scripts/security-lint.sh
```
