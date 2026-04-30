# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 워크스페이스 구조

```
ai_eactive_hub/
├─ eactive-resource-hub/   # Spring Boot 코드 (이 폴더)
├─ mvp1/                   # 1차 MVP 동결본 — 수정 금지
└─ mvp2/                   # 2차 MVP 작업 공간 (docs/, harness/)
```

- **MVP1** (stages 01~13): 회원가입·인증·문서 업로드·썸네일·검토·권한·검색·배포
- **MVP2** (stages 01~10): SALES 역할·다운로드 정책·프로필 확장·인력 표·검색·양식 이력서·경력 계산기·엑셀 export
- Flyway 마이그레이션: V1–V6 = MVP1, V100~ = MVP2

## 빌드 / 실행

```bash
# 로컬 개발 (PostgreSQL이 localhost:5432에 있어야 함)
./gradlew bootRun

# 테스트 없이 JAR 빌드
./gradlew bootJar -x test

# Docker 전체 스택 (postgres + app)
docker compose up -d --build

# 앱만 재빌드
docker compose up -d --build app

# 하네스 전체 검증
./harness/run_all.sh

# MVP2 단계별 검증
bash mvp2/harness/scripts/verify.sh 01   # 단계 번호로 지정
bash mvp2/harness/scripts/status.sh      # 전체 진행 상태
```

## 기술 스택

- Java 21, Spring Boot 3.5.x, Gradle
- PostgreSQL 18 + Flyway (DDL-auto: `validate` — Flyway만 스키마 변경)
- Thymeleaf + Bootstrap 5, `thymeleaf-extras-springsecurity6`
- Spring Security (세션 기반, `RESOURCEHUB_SESSION` 쿠키, 30분 타임아웃)
- `spring-boot-starter-mail` (SMTP 미설정 시 `ConsoleEmailSender` 폴백)

## 패키지 구조

```
com.eactive.resourcehub
├─ audit/          — AuditLog, AuditLogService, StatisticsService
├─ common/
│  ├─ config/      — JpaAuditingConfig
│  ├─ email/       — EmailSender (인터페이스), SmtpEmailSender, ConsoleEmailSender
│  ├─ file/        — FileStorage (인터페이스), LocalFileStorage
│  ├─ security/    — SecurityConfig, CustomUserDetails, CustomUserDetailsService
│  └─ service/     — AuditService (횡단 감사 로그)
├─ document/       — Document, DocumentVersion, Folder, Tag, 관련 서비스·컨트롤러
├─ employee/       — EmployeeProfile
├─ permission/     — Permission, FolderPermissionService
├─ team/           — Team, TeamService (MVP1 레거시)
└─ user/           — User, UserRole, 회원가입·인증·관리자 서비스
```

## 핵심 설계 규칙

### 보안 (모든 단계 공통 — 절대 변경 금지)
- **JWT 사용 금지** — Spring Security 세션만 사용
- **Remember-me 금지**, CSRF 항상 활성화
- **파일 직접 노출 금지** — 모든 파일 접근은 컨트롤러 경유
- 저장 파일명은 **UUID**, DB에 원본 파일명 별도 보관
- **권한 검사는 Service 레이어**에서 수행 (컨트롤러에서 직접 쿼리 금지)

### JPA / 트랜잭션
- `@Enumerated(EnumType.STRING)` — 모든 enum 컬럼
- LazyInitializationException 방지: `LEFT JOIN FETCH` 사용, 컬렉션 필터는 `EXISTS` 서브쿼리
- 감사 로그는 `Propagation.REQUIRES_NEW` (메인 트랜잭션 롤백과 독립)
- `ddl-auto: validate` — 스키마 변경은 반드시 Flyway 마이그레이션으로

### 이메일
- `EmailSender` 인터페이스 — `@ConditionalOnProperty(name = "spring.mail.host")`로 `SmtpEmailSender` 활성화, 없으면 `ConsoleEmailSender` 폴백
- 메일 발송 실패는 try/catch로 격리 — 비즈니스 트랜잭션 롤백 금지

### MVP2 추가 역할 구조
| 역할 | 접근 범위 |
|------|----------|
| `ADMIN` | 전체 관리 + 파일 삭제 + 바로 다운로드 |
| `SALES` | 전사 인력 표 조회 + 바로 다운로드 + 경력 계산기 |
| `EMPLOYEE` | 본인 폴더만 |
| `TEAM_LEADER` | `@Deprecated` — 신규 부여 차단, DB에만 잔존 |

### Flyway 마이그레이션 번호 정책
- V1–V6: MVP1
- V100–V106+: MVP2 (신규 마이그레이션은 V107부터)

## 환경변수 (.env)

| 변수 | 설명 |
|------|------|
| `POSTGRES_PASSWORD` | DB 비밀번호 |
| `RESOURCEHUB_COMPANY_EMAIL_DOMAIN` | 회원가입 허용 도메인 (기본: `eactive.co.kr`) |
| `RESOURCEHUB_ADMIN_EMAIL` / `RESOURCEHUB_ADMIN_PASSWORD` | 초기 관리자 계정 |
| `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD` | SMTP 설정 (미설정 시 콘솔 출력) |
| `APP_PORT` | 호스트 바인딩 포트 (기본: 8080) |

## 테스트 계정 (로컬)

| 이메일 | 비밀번호 | 역할 |
|--------|---------|------|
| `admin@eactive.co.kr` | `Admin1234!` | ADMIN |
| `test@eactive.co.kr` | `Test1234!` | TEAM_LEADER (→ SALES in MVP2) |
| `user2@eactive.co.kr` | `User1234!` | EMPLOYEE |

## 하네스

- `harness/lib/common.sh` — curl/psql 공용 헬퍼 (`check`, `login`, `get_status`, `get_body`, `post_form`, `post_form_status`, `db_query`, `wait_for_app`)
- `harness/stages/{01~13}/verify.sh` — MVP1 단계별 자동 검증
- `mvp2/harness/stages/{01~10}/` — MVP2 단계별 prompt/acceptance/deliverables/verify
- `mvp2/harness/state/progress.json` — MVP2 진행 상태 SSOT
