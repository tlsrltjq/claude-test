# MVP2 Stage 03 — Prompt

MVP2 3단계 작업을 진행해줘.

이전 단계까지: 권한 단순화, 다운로드 정책 변경, 관리자 파일 삭제 완료.

이번 단계 목표는 **회원가입/프로필 입력을 mvp2.pdf §회원가입 입력 정보 에 맞게 확장**하는 것이야.

요구사항:

1. Flyway 마이그레이션 `V102__add_profile_fields_to_users.sql` 추가
   - `users` 테이블에 컬럼 추가:
     - `birth_date DATE NOT NULL DEFAULT '1970-01-01'` (기존 데이터를 위한 기본값, 나중에 NOT NULL 조이는 건 별도 마이그레이션으로)
     - `phone VARCHAR(20) NOT NULL DEFAULT ''`
   - 기존 `position` 컬럼이 자유 텍스트면 enum string에 맞춰 정형화하도록 ALTER + UPDATE.
     - 기존 NULL이거나 매핑 안 되는 값은 'STAFF'로 통일.
   - 인덱스: `users(name)`, `users(position)` (이후 단계 검색 대비)

2. `Position` enum 신설 — `user/entity/Position.java`
   ```
   REPRESENTATIVE("대표"),
   EXECUTIVE_DIRECTOR("전무"),
   DIRECTOR("이사"),
   GENERAL_MANAGER("부장"),
   DEPUTY_GENERAL_MANAGER("차장"),
   MANAGER("과장"),
   ASSISTANT_MANAGER("대리"),
   STAFF("사원");
   ```
   각 enum 값에 displayName(한글) 보유.

3. `User` entity 갱신
   - `position` 타입을 `Position` enum 으로 변경 (`@Enumerated(STRING)`)
   - `LocalDate birthDate`
   - `String phone`
   - 나이는 `@Transient` 게터 또는 DTO 단계에서 계산 (DB 컬럼 없음).

4. 회원가입 화면 (`/signup`)
   - 입력 필드: 이름, 직급(셀렉트), 생년월일(date), 연락처, 이메일 앞부분(텍스트, suffix 표시), 비밀번호, 비밀번호 확인
   - 이메일 앞부분만 받고 서버에서 `${resourcehub.company-email-domain}` 으로 조합 → 저장 시 풀 이메일.
   - 클라이언트단에 suffix `@eactive.co.kr` 정적 표시 (Thymeleaf로 설정값 가져옴).

5. 비밀번호 정책 (서버 검증 + 클라이언트 힌트)
   - 영문/숫자/특수문자 중 **3종류 이상** 포함, **8자 이상**.
   - 서버 검증 실패 시 한글 에러 메시지.

6. 이메일 인증 단계 (`/signup/verify`)
   - **이메일 입력 필드 제거.** 이메일은 서버 세션 또는 가입 시점에 발급된 토큰으로 자동 식별.
   - 사용자는 6자리 코드만 입력.

7. 기본 관리자 자동 생성 시에도 모든 필드를 채움 (생년월일/연락처는 합리적 기본값, 직급은 ADMIN이 아니라 enum 값 중 하나, 예: REPRESENTATIVE).

8. EmployeeProfile 도메인은 이번 단계에서 건드리지 마 (개발자 등급은 04, 경력 계산은 04 이후).

9. NOT-DOING
   - /sales/profiles 표
   - 검색/필터/정렬
   - employee_profiles 컬럼 추가

검증:
- 회원가입 폼이 새 필드들 표시
- 회사 도메인 외 이메일 가입 시도 → 거부 (이메일 앞부분만 받으니 자동으로 회사 도메인 강제됨)
- 비밀번호 정책 위반 시 한글 메시지
- 인증 단계에 이메일 입력란 없음
- DB users 테이블에 birth_date/phone 컬럼 존재 + 기존 사용자에 기본값 채움
- 기존 ADMIN 계정 그대로 로그인 가능
