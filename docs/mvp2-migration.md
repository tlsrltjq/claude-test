# MVP1 → MVP2 마이그레이션 가이드

> MVP1 코드 위에 MVP2 변경을 얹을 때 주의해야 할 횡단(cross-cutting) 변경 사항.
> 각 MVP2 단계 prompt에서 이 문서를 참조한다.

---

## 1. 권한 모델

### 변경 전 (MVP1)
```java
public enum UserRole {
    ADMIN, TEAM_LEADER, EMPLOYEE
}
```

### 변경 후 (MVP2 — 권장: deprecated 유지)
```java
public enum UserRole {
    ADMIN,
    SALES,                // 신규
    EMPLOYEE,
    @Deprecated
    TEAM_LEADER           // enum 값은 남기고 신규 부여 차단 (DB 제약 회피)
}
```

### 데이터 마이그레이션 (V100)
```sql
UPDATE users SET role = 'SALES' WHERE role = 'TEAM_LEADER';
```

이후 부여 시 코드에서 TEAM_LEADER 입력을 거부:
```java
if (newRole == UserRole.TEAM_LEADER) {
    throw new BusinessException("TEAM_LEADER는 더 이상 사용되지 않습니다. SALES를 사용하세요.");
}
```

## 2. 라우팅 변경

| MVP1 | MVP2 | 처리 |
|------|------|------|
| `GET /team/members` | `GET /sales/profiles` | 컨트롤러 통째로 이동 (인력 프로필 표 형태로 갈아엎음) |
| `GET /team/members/{id}/documents` | `GET /sales/employees/{id}/documents` | 컨트롤러 이동 |
| `GET /shared/folders` | (그대로 유지) | permissions 기반은 MVP2에서 사실상 안 씀 — 폐기 검토 가능 |

기존 `/team/**` 라우트는 즉시 제거 가능 (MVP1 사용자 데이터에는 영향 없음).

## 3. SecurityConfig

### 변경 전
```java
.requestMatchers("/team/**").hasAnyRole("ADMIN", "TEAM_LEADER")
```

### 변경 후
```java
.requestMatchers("/sales/**").hasAnyRole("ADMIN", "SALES")
```

기존 `/admin/**`는 그대로 유지. `/team/**`는 SecurityConfig에서 제거.

## 4. 다운로드 흐름

### 변경 전 (MVP1)
```
GET /documents/{versionId}/download/reason → 사유 입력 화면
POST /documents/{versionId}/download (form, reason 필수)
audit_logs.reason NOT NULL
```

### 변경 후 (MVP2)
```
GET /documents/{versionId}/download → 권한 확인 후 즉시 스트리밍
audit_logs.reason 은 nullable (V101)
```

ADMIN/SALES는 모두 사유 없이 다운로드. EMPLOYEE는 본인 문서만.

## 5. DB 마이그레이션 (V100~)

| 버전 | 내용 |
|------|------|
| V100 | TEAM_LEADER → SALES 데이터 갱신 |
| V101 | `audit_logs.reason` NOT NULL 제약 제거 (있을 경우) |
| V102 | `users` 에 `birth_date DATE`, `phone VARCHAR(20)` 컬럼 추가. position 컬럼은 enum string으로 정형화 (체크 제약 또는 enum 매핑) |
| V103 | `employee_profiles` 에 `developer_grade VARCHAR(20)`, `career_months INT DEFAULT 0` 컬럼 추가 |
| V104 | `resume_templates(id, file_name, stored_path, content_type, file_size, status, uploaded_by, created_at)` |
| V105 (M2-09) | (옵션) employee_profiles 에 `career_calc_input JSONB` 같은 보조 컬럼 |
| V106~ (M2-10) | 투입인력서 묶음/템플릿 |

## 6. DocumentType 추가

```java
public enum DocumentType {
    RESUME,
    CAREER_DESCRIPTION,
    GRADUATION_CERTIFICATE,
    LICENSE,
    EMPLOYMENT_CERTIFICATE,
    HEALTH_INSURANCE_PROOF,   // 신규 (MVP2)
    ETC
}
```

DB enum 컬럼이 VARCHAR + 체크 제약으로 정의된 경우, 체크 제약을 갱신해야 함.

## 7. Position enum

`users.position` 은 MVP1에서 자유 텍스트였으나 MVP2에서 enum으로 정형화:

```java
public enum Position {
    REPRESENTATIVE("대표"),
    EXECUTIVE_DIRECTOR("전무"),
    DIRECTOR("이사"),
    GENERAL_MANAGER("부장"),
    DEPUTY_GENERAL_MANAGER("차장"),
    MANAGER("과장"),
    ASSISTANT_MANAGER("대리"),
    STAFF("사원");

    private final String displayName;
    // ...
}
```

V102 마이그레이션 시 기존 한글 값을 enum 값으로 매핑하는 데이터 정리 필요.

## 8. 회귀 점검 — MVP1 acceptance가 깨지지 않아야 함

각 MVP2 단계 verify.sh는 가능하면 mvp1의 verify.sh 일부도 호출해서 회귀 검사를 한다.

또는 마지막에:
```bash
bash mvp1/harness/scripts/verify.sh all
```
을 돌려 MVP1 1~10단계 acceptance 전부 통과를 확인한다 (일부 단계는 SALES 추가로 영향 받을 수 있으니 mvp1 verify를 살짝 손볼 수도 있음 — 그 경우엔 mvp1 동결본을 깨는 게 아니라 mvp2 측에서 별도 wrapper로 처리).

## 9. NOT-DOING — 이 마이그레이션에서 건드리지 말 것

- MVP1의 1~10단계 acceptance 항목을 약화시키지 마라 (보안·로그·UUID 파일명 등)
- MVP1의 `/admin/**`, `/my/folder/**`, 인증 흐름은 그대로 유지
- 썸네일·문서 검토(승인/반려)·세션 정책은 MVP2에서 변경하지 않음
