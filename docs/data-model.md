# 데이터 모델

> Flyway V1~V215 기준. 엔티티·테이블·관계·주요 제약조건.
> 참고 소스: `src/main/resources/db/migration/V*.sql`, `src/main/java/**/entity/*.java`

---

## ERD (텍스트)

```
teams ──< users >── employee_profiles
                │
                └──< folders >── documents >── document_versions
                                     │
                          document_tags >── tags (미사용)

permissions ──> users
permissions ──> folders (target_type = FOLDER)

audit_logs ──> users

resume_templates (독립)
password_reset_tokens ──> users
email_verification_tokens ──> users (확인 필요: 별도 테이블)
column_view_preferences ──> users
allowed_emails (독립 — 회원가입 사전 허용 이메일 목록)
```

---

## 테이블 상세

### teams

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | |
| name | VARCHAR(100) | NOT NULL | 팀명 |
| description | VARCHAR(500) | | 팀 설명 |
| project_team | BOOLEAN | NOT NULL DEFAULT TRUE | 영업 인력표 노출 여부 (V212) |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

> V212: `영업본부`, `경영본부`는 `project_team = FALSE`로 초기화.

---

### users

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | |
| login_id | VARCHAR(100) | NOT NULL, UNIQUE | 전체 이메일 주소 (로그인 아이디로 사용) |
| password | VARCHAR(255) | NOT NULL | BCrypt 해시 |
| name | VARCHAR(100) | NOT NULL | |
| email | VARCHAR(255) | NOT NULL, UNIQUE | 회사 이메일 full (login_id와 동일) |
| address | VARCHAR(255) | | 주소 (V214 추가) |
| team_id | BIGINT | FK → teams.id | NULL 허용 |
| position | VARCHAR(50) | NOT NULL DEFAULT 'STAFF' | Position enum (V102에서 정규화) |
| birth_date | DATE | NOT NULL DEFAULT '1970-01-01' | (V102 추가) |
| phone | VARCHAR(20) | NOT NULL DEFAULT '' | (V102 추가) |
| role | VARCHAR(50) | NOT NULL DEFAULT 'EMPLOYEE' | UserRole enum |
| status | VARCHAR(50) | NOT NULL DEFAULT 'PENDING' | UserStatus enum |
| email_verified | BOOLEAN | NOT NULL DEFAULT FALSE | |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

인덱스: `idx_users_email`, `idx_users_team_id`, `idx_users_name`, `idx_users_position`

---

### employee_profiles

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | |
| user_id | BIGINT | NOT NULL, FK → users.id | |
| job_title | VARCHAR(100) | | 직무명 |
| career_summary | TEXT | | 경력 요약 |
| skills | TEXT | | 기술 스택 |
| developer_grade | VARCHAR(20) | | 기술자 등급 (V103 추가) |
| career_months | INT | NOT NULL DEFAULT 0 | 경력 총 개월 수 (V103 추가) |
| career_total_days | INT | NOT NULL DEFAULT 0 | 경력 총 일수 (V106 추가, V204 백필) |
| available_status | VARCHAR(50) | NOT NULL DEFAULT 'AVAILABLE' | AvailableStatus enum |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

> User 1:1 관계. 회원가입 완료 시 자동 생성.

---

### folders

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | |
| owner_user_id | BIGINT | NOT NULL, FK → users.id | |
| folder_name | VARCHAR(255) | NOT NULL | |
| type | VARCHAR(20) | NOT NULL DEFAULT 'PERSONAL' | FolderType enum (V203 추가) |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

> `SHARED_PUBLIC` 폴더는 V207에서 시스템 계정 소유로 시드. owner는 admin 계정.

---

### documents

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | |
| folder_id | BIGINT | NOT NULL, FK → folders.id | |
| document_type | VARCHAR(50) | NOT NULL | DocumentType enum |
| title | VARCHAR(255) | NOT NULL | |
| current_version_id | BIGINT | FK → document_versions.id, DEFERRABLE | 순환 FK |
| status | VARCHAR(50) | NOT NULL DEFAULT 'ACTIVE' | DocumentStatus enum |
| expires_at | DATE | | 만료일 (V5 추가) |
| issued_date | DATE | | 발급일 (V208 추가) |
| degree_type | VARCHAR(50) | | 학위 종류 (V208 추가) |
| cert_type_meta | VARCHAR(50) | | 자격증 메타 — 현재 `ENGINEER` 고정 (V208 추가) |
| deleted_at | TIMESTAMP | | soft-delete 시각 (V206 추가) |
| deleted_by | BIGINT | | soft-delete 수행자 (V206 추가) |
| files_purged_at | TIMESTAMP | | GC 완료 시각 (V210 추가) |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

인덱스: `idx_documents_folder_id`

Partial Unique Index (V211):
```sql
CREATE UNIQUE INDEX uk_documents_folder_type_title_active
    ON documents (folder_id, document_type, title)
    WHERE status <> 'DELETED';
```
> 같은 폴더에서 동일 (document_type, title) 중복 방지. DELETED 상태는 제외.

---

### document_versions

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | |
| document_id | BIGINT | NOT NULL, FK → documents.id | |
| version_no | INT | NOT NULL DEFAULT 1 | |
| original_file_name | VARCHAR(255) | NOT NULL | 원본 파일명 (DB 보관) |
| stored_file_name | VARCHAR(255) | NOT NULL | UUID 기반 저장 파일명 |
| storage_path | VARCHAR(500) | NOT NULL | 스토리지 경로 |
| preview_file_name | VARCHAR(255) | | PDF 미리보기 파일명 (V3) |
| preview_storage_path | VARCHAR(500) | | PDF 미리보기 경로 (V3) |
| file_size | BIGINT | NOT NULL | bytes |
| content_type | VARCHAR(100) | | MIME type |
| checksum | VARCHAR(64) | | SHA-256 (중복 업로드 방지) |
| uploaded_by | BIGINT | NOT NULL, FK → users.id | |
| review_status | VARCHAR(50) | NOT NULL DEFAULT 'PENDING_REVIEW' | DocumentReviewStatus (V4) |
| reviewed_by | BIGINT | FK → users.id | (V4) |
| reviewed_at | TIMESTAMP | | (V4) |
| reject_reason | VARCHAR(500) | | (V4) |
| thumbnail_file_name | VARCHAR(255) | | (V3) |
| thumbnail_storage_path | VARCHAR(500) | | (V3) |
| thumbnail_content_type | VARCHAR(100) | | (V3) |
| thumbnail_generated_at | TIMESTAMP | | (V3) |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

인덱스: `idx_doc_versions_document_id`

---

### permissions

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | |
| user_id | BIGINT | NOT NULL, FK → users.id | 권한 대상 사용자 |
| permission_type | VARCHAR(50) | NOT NULL | 현재 `FOLDER_ACCESS`만 사용 |
| target_type | VARCHAR(50) | NOT NULL | 현재 `FOLDER`만 사용 |
| target_id | BIGINT | NOT NULL | 폴더 id |
| granted_by | BIGINT | NOT NULL, FK → users.id | 부여한 관리자 |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

인덱스: `idx_permissions_user_target(user_id, target_type, target_id)`

---

### audit_logs

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGSERIAL | PK | |
| user_id | BIGINT | NOT NULL, FK → users.id | 행위자 |
| action_type | VARCHAR(50) | NOT NULL | AuditActionType enum |
| target_type | VARCHAR(50) | NOT NULL | AuditTargetType enum |
| target_id | BIGINT | NOT NULL | |
| reason | VARCHAR(500) | | |
| ip_address | VARCHAR(50) | | |
| user_agent | VARCHAR(500) | | |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

인덱스: `idx_audit_logs_user_created`, `idx_audit_logs_target`

주요 AuditActionType: `LOGIN`, `UPLOAD`, `DOWNLOAD`, `DELETE_DOCUMENT`, `DELETE_DOCUMENT_SELF`, `RESET_PASSWORD`, `CHANGE_USER_STATUS`, `EXPORT_PROFILES`, `ENABLE_USER`, `DISABLE_USER`

---

### email_verification_tokens

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL PK | |
| (상세는 V2 마이그레이션 참고) | | 회원가입 이메일 인증 코드 |

---

### password_reset_tokens

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users.id | |
| email | VARCHAR(255) | |
| token | VARCHAR(10) | 6자리 코드 |
| expired_at | TIMESTAMPTZ | 5분 TTL |
| verified_at | TIMESTAMPTZ | 코드 인증 시각 |
| consumed_at | TIMESTAMPTZ | 비밀번호 변경 완료 시각 |
| created_at | TIMESTAMPTZ | |

인덱스: `idx_prt_user`, `idx_prt_email`

---

### resume_templates

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL PK | |
| file_name | VARCHAR(255) | 원본 파일명 |
| stored_file_name | VARCHAR(255) | UUID 기반 저장명 |
| storage_path | VARCHAR(500) | |
| content_type | VARCHAR(100) | |
| file_size | BIGINT | |
| checksum | VARCHAR(128) | |
| status | VARCHAR(20) DEFAULT 'ACTIVE' | ResumeTemplateStatus enum |
| uploaded_by | BIGINT FK → users.id | |
| created_at | TIMESTAMPTZ | |

---

### column_view_preferences

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users.id | |
| name | VARCHAR(80) | 프리셋 이름 |
| columns_json | TEXT | 선택 컬럼 배열 JSON |
| sort_json | TEXT | 정렬 조건 JSON |
| career_display | VARCHAR(8) DEFAULT 'ymd' | 경력 표시 형식 |
| is_default | BOOLEAN DEFAULT false | |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

UNIQUE: `(user_id, name)`

---

### allowed_emails

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL PK | |
| email | VARCHAR(255) NOT NULL UNIQUE | 가입 허용 이메일 주소 |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

> V215 신설. 이 목록에 등록된 이메일만 회원가입 가능. 관리자가 `/admin/allowed-emails`에서 추가·삭제.

---

## Flyway 마이그레이션 이력 요약

| 버전 | 주요 내용 |
|------|----------|
| V1 | 기본 테이블 (teams, users, employee_profiles, folders, documents, document_versions, permissions, audit_logs) |
| V2 | email_verification_tokens |
| V3 | document_versions 썸네일·미리보기 컬럼 |
| V4 | document_versions 검토 컬럼 (review_status, reviewed_by, reviewed_at, reject_reason) |
| V5 | documents.expires_at |
| V6 | tags, document_tags |
| V100 | users.role 'TEAM_LEADER' → 'SALES' 마이그레이션 |
| V102 | users.birth_date, phone 추가. position 정규화 |
| V103 | employee_profiles.developer_grade, career_months 추가 |
| V104 | resume_templates 테이블 |
| V106 | employee_profiles.career_total_days 추가 |
| V200 | Position MANAGING_DIRECTOR(상무) 추가 |
| V201 | password_reset_tokens 테이블 |
| V202 | DocumentType PROFILE_PHOTO 추가 (enum 신규 값) |
| V203 | folders.type 컬럼 추가 (PERSONAL/SHARED_PUBLIC) |
| V204 | career_total_days 백필 |
| V205 | column_view_preferences 테이블 |
| V206 | documents soft-delete (deleted_at, deleted_by) |
| V207 | SHARED_PUBLIC 공용 폴더 시드 |
| V208 | documents 메타 컬럼 (issued_date, degree_type, cert_type_meta) |
| V209 | 학위·자격증 초기 문서 시드 |
| V210 | documents.files_purged_at (GC 완료 추적) |
| V211 | partial unique index (folder_id, document_type, title) WHERE status <> 'DELETED' |
| V212 | teams.project_team 컬럼. 영업본부·경영본부 = FALSE |
| V213 | 기존 HEALTH_INSURANCE_PROOF 문서 일괄 soft-delete |
| V214 | users.address 컬럼 추가 |
| V215 | allowed_emails 테이블 신설 (이메일 사전등록 방식) |

> 다음 마이그레이션은 **V216**부터.
