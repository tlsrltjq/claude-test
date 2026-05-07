# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 워크스페이스 구조

```
ai_eactive_hub/
├─ eactive-resource-hub/   # Spring Boot 소스 (이 폴더)
│  ├─ docs/                # 스펙·마이그레이션·보안 정책 (색인 아래 참조)
│  └─ harness/mvp1~3/      # 단계별 자동 검증 스크립트
├─ mvp1~3/docs/            # 각 MVP 원본 스펙·결정사항
```

## 빌드 / 실행

```bash
./gradlew bootRun                                    # 로컬 (PostgreSQL localhost:5432)
docker compose up -d --build                         # Docker 전체 스택

bash harness/mvp3/scripts/verify.sh M3-01            # 단계 검증
bash harness/mvp3/scripts/verify.sh all --with-mvp2  # MVP3 + MVP2 회귀
bash harness/mvp3/scripts/status.sh                  # 진행 상태
```

## 기술 스택

- Java 21, Spring Boot 3.5.x, Gradle, PostgreSQL 18 + Flyway
- Thymeleaf + Bootstrap 5, `thymeleaf-extras-springsecurity6`
- Spring Security — 세션 기반, `RESOURCEHUB_SESSION` 쿠키, 30분 타임아웃
- Flyway 번호: V1–V6 = MVP1 / V100–V2xx = MVP2 / V300~ = MVP3

## 핵심 설계 규칙

보안·권한 상세 → `docs/security-policy.md`

- **JWT 사용 금지** — Spring Security 세션만, Remember-me 금지, CSRF 항상 활성화
- **파일 직접 노출 금지** — 컨트롤러 경유, UUID 파일명, DB에 원본명 보관
- **권한 검사는 Service** — `DocumentAccessService` / `FolderAccessService` 경유
- `ddl-auto: validate`, 스키마 변경은 Flyway만, 감사 로그는 `REQUIRES_NEW`

## 역할 구조

| 역할 | 접근 범위 |
|------|----------|
| `ADMIN` | 전체 관리 + 파일 삭제 + 바로 다운로드 |
| `SALES` | 전사 인력 표 조회 + 다운로드 + 경력 계산기 |
| `EMPLOYEE` | 본인 폴더만 |
| `TEAM_LEADER` | `@Deprecated` — 신규 부여 차단 |

## 환경변수

| 변수 | 설명 |
|------|------|
| `POSTGRES_PASSWORD` | DB 비밀번호 |
| `RESOURCEHUB_COMPANY_EMAIL_DOMAIN` | 허용 도메인 (기본: `eactive.co.kr`) |
| `RESOURCEHUB_ADMIN_EMAIL` / `_PASSWORD` | 초기 관리자 계정 |
| `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD` | SMTP (미설정 시 콘솔 출력) |

## 테스트 계정 (로컬)

| 이메일 | 비밀번호 | 역할 |
|--------|---------|------|
| `admin@eactive.co.kr` | `Admin1234!` | ADMIN |
| `test@eactive.co.kr` | `Test1234!` | SALES |
| `user2@eactive.co.kr` | `User1234!` | EMPLOYEE |

## docs/ 색인

- `mvp1-spec.md` — MVP1 전체 스펙
- `mvp2-spec.md` / `mvp2-migration.md` — MVP2 스펙·마이그레이션
- `mvp3-spec.md` / `mvp3-migration.md` — MVP3 4축 요약·마이그레이션
- `mvp3-decisions.md` — D-01~D-12 결정 사항
- `mvp3-requirements.md` / `mvp3-stage-plan.md` — 상세 요구사항·단계 계획
- `security-policy.md` — 파일·권한·로그 보안 정책 + 미구현 TODO
