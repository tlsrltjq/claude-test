# MVP3 M3-10 — Prompt

작업: /sales/profiles 본격 정비.

요구사항:

## 1. 개발자 등급 요약 위젯

- 화면 상단에 작은 카드/표
- 컬럼: 등급별 인원수
- 예: `JUNIOR 3 / INTERMEDIATE 5 / SENIOR 2 / EXPERT 1`
- 미설정 사용자는 "미설정 N" 별도 칸
- 현재 검색·필터 결과를 반영 (필터 변경 시 위젯도 갱신)

## 2. 경력 일 단위 저장

- Flyway `V204__add_career_total_days_to_employee_profiles.sql`
  ```sql
  ALTER TABLE employee_profiles ADD COLUMN career_total_days INT NOT NULL DEFAULT 0;
  -- 기존 career_months 값을 days 로 환산해 채움 (1개월=30일 가정)
  UPDATE employee_profiles SET career_total_days = career_months * 30 WHERE career_total_days = 0 AND career_months > 0;
  ```
- Entity: `careerTotalDays` 필드
- 기존 `careerMonths` 는 derived getter (`careerTotalDays / 30`)로 두거나 그대로 유지하되 SoT는 days

## 3. 경력 표시 3가지 토글 (D-03)

- 표 상단에 **셀렉트 박스**: `n년 n월 n일` (default) / `n개월` / `n일`
- 선택 값은 쿼리 파라미터 `careerDisplay` 로 (`ymd`/`m`/`d`)
- 표시 헬퍼 함수:
  - `ymd`: days → years/months/days 로 분해 (1년=365일, 1월=30일 가정)
  - `m`: days / 30
  - `d`: days

## 4. 사용자 정의 컬럼 프리셋 (D-02)

- Flyway `V205__create_column_view_preferences.sql`
  ```sql
  CREATE TABLE column_view_preferences (
      id BIGSERIAL PRIMARY KEY,
      user_id BIGINT NOT NULL REFERENCES users(id),
      name VARCHAR(80) NOT NULL,
      columns_json TEXT NOT NULL,           -- e.g. ["position","name","email","career"]
      sort_json TEXT,                        -- e.g. {"sort":"name","direction":"asc"}
      career_display VARCHAR(8) NOT NULL DEFAULT 'ymd',
      is_default BOOLEAN NOT NULL DEFAULT false,
      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      CONSTRAINT cvp_user_name_unique UNIQUE(user_id, name)
  );
  CREATE INDEX idx_cvp_user ON column_view_preferences(user_id);
  ```
- Entity / Repository / Service
- /sales/profiles 화면:
  - 상단 "프리셋" 셀렉트 — 사용자 본인 저장한 프리셋 목록 + "현재 시야 저장" 버튼 + 이름 입력 prompt
  - 프리셋 선택 시 컬럼 토글·정렬·career_display 모두 적용
  - 기본 프리셋 1개 자동 생성 가능 (선택)
  - "삭제" 버튼

## 5. 컬럼 — 14개 유지 (D-01)

- 기존 14컬럼 그대로
- 사용자 정의 프리셋으로 본인이 보고 싶은 컬럼만 토글

## 6. NOT-DOING
- 컬럼 순서 드래그앤드롭 (다음 라운드)
- 체크 후 엑셀 (M3-11)
- career-calculator 검색 (M3-12)

## 검증
- 부팅 후 V204, V205 적용
- /sales/profiles 상단 등급 위젯 표시
- 경력 표시 셀렉트 동작 (3가지)
- 프리셋 저장 → 다른 브라우저 세션에서도 같은 사용자면 프리셋 노출
- 본인 프리셋만 본인에게 노출 (타인 프리셋 격리)
- DB career_total_days 채워짐
