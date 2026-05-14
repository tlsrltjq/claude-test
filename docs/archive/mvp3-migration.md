# MVP2 → MVP3 마이그레이션 가이드

## 1. Flyway 번호 정책

- MVP2 사용 중 V100~V199 (또는 V107 정도까지)
- **MVP3는 V200~ 부터 시작.** 충돌 회피 + 명확한 라운드 구분.

## 2. 예상 마이그레이션 목록

| 버전 | 단계 | 내용 |
|------|------|------|
| V200 | M3-01 | Position 체크 제약에 `MANAGING_DIRECTOR` 추가 |
| V201 | M3-02 | (선택) `password_reset_tokens` 신설 |
| V202 | M3-05 | DocumentType 체크 제약에 `PROFILE_PHOTO` 추가, EMPLOYMENT_CERTIFICATE 는 deprecated 표시(주석) |
| V203 | M3-07 | `folders.type ENUM('PERSONAL','SHARED_PUBLIC')` + 공용 폴더 1행 시드 |
| V204 | M3-10 | `column_view_preferences(id, user_id, name, columns_json, sort_json, default_flag, created_at, updated_at)` |

## 3. enum 호환성

### UserRole — 한글화는 화면만, enum 그대로
- ADMIN/SALES/EMPLOYEE 그대로
- DB 데이터 영향 없음

### Position — 추가만
```java
public enum Position {
    REPRESENTATIVE("대표"),
    EXECUTIVE_DIRECTOR("전무"),
    MANAGING_DIRECTOR("상무"),    // ← 신규
    DIRECTOR("이사"),
    GENERAL_MANAGER("부장"),
    DEPUTY_GENERAL_MANAGER("차장"),
    MANAGER("과장"),
    ASSISTANT_MANAGER("대리"),
    STAFF("사원");
}
```

기존 데이터 영향 없음. enum ordinal 이용한 코드 있으면 주의 (sort용 별도 sortOrder 메서드 권장).

### DocumentType — 추가 + 표시명 변경
```java
public enum DocumentType {
    RESUME("이력서"),
    CAREER_DESCRIPTION("경력기술서"),
    GRADUATION_CERTIFICATE("졸업증명서"),
    LICENSE("정보처리기사"),                   // ← displayName 변경
    @Deprecated
    EMPLOYMENT_CERTIFICATE("재직증명서"),       // ← deprecated, 신규 업로드 옵션 제외
    HEALTH_INSURANCE_PROOF("건강보험"),
    PROFILE_PHOTO("증명사진"),                  // ← 신규
    ETC("기타")
}
```

기존 EMPLOYMENT_CERTIFICATE 업로드 데이터는 보존.

## 4. 라우팅 변경 / 제거

| 화면 | MVP2 | MVP3 |
|------|------|------|
| 로그인 | `/login` | `/login`, `/login/forgot`, `/login/forgot/verify` 추가 |
| 회원가입 인증 | `/signup/verify`, `/signup/resend` | 5분 정책 + 타이머 (라우트 그대로) |
| 검색 | `/search` (현재 비어 있음) | `/search` (모든 권한 문서 + 필터) |
| 공용 폴더 | `/shared/folders` (개별 권한자 화면) | `/shared/folders/public` 추가 |
| sales | 그대로 | `/sales/profiles/export` 가 체크 선택형으로 |

## 5. 태그 기능

- 즉시 drop 하지 않음 (D-05 권장 A)
- 화면/필터에서만 제거. DB 컬럼/테이블은 다음 라운드 cleanup 마이그레이션으로.

## 6. 회귀 보장

각 mvp3 단계 verify.sh 는 옵션 `--with-mvp1`, `--with-mvp2` 둘 다 지원하도록 mvp3/harness/scripts/verify.sh 를 손본다 (mvp2 의 패턴 그대로 확장).

## 7. NOT-DOING

- 태그 테이블 drop (이번 라운드에서 X)
- 영업 본인 폴더의 동작 변경 (SALES도 EMPLOYEE 본인 폴더 흐름 그대로)
- mvp2 09-career-save 와 10-bundle-template 은 mvp3 끝나고 별도 라운드
- "선택한 사람 경력 불러오기" 는 사용자가 "나중에 할 기능"으로 명시 — 보류
