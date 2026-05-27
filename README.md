# eActive Resource Hub

회사 내부 직원 문서 관리 포털. 직원별 이력서·경력기술서·졸업증명서·자격증·기타 증빙서류를 한 곳에서 관리하고, 관리자가 검토·승인·만료 관리까지 수행하는 사내 전용 시스템입니다.

---

## 목차

1. [기술 스택](#기술-스택)
2. [프로젝트 구조](#프로젝트-구조)
3. [빠른 시작 (로컬)](#빠른-시작-로컬)
4. [환경변수 상세 설명](#환경변수-상세-설명)
5. [데이터베이스 설정](#데이터베이스-설정)
6. [파일 스토리지 설정](#파일-스토리지-설정)
7. [이메일(SMTP) 설정](#이메일smtp-설정)
8. [Docker 전체 스택 실행](#docker-전체-스택-실행)
9. [운영 서버 배포](#운영-서버-배포)
10. [역할 구조](#역할-구조)
11. [사용법 문서](#사용법-문서)
12. [보안 주의사항](#보안-주의사항)

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.5.x |
| 빌드 | Gradle 8.x |
| 데이터베이스 | PostgreSQL 18 |
| ORM | Spring Data JPA (Hibernate 6) |
| 스키마 마이그레이션 | Flyway |
| 보안 | Spring Security 6 (세션 기반, JWT 미사용, CSRF 활성화) |
| 템플릿 엔진 | Thymeleaf + thymeleaf-extras-springsecurity6 |
| 유효성 검사 | Spring Validation (jakarta.validation) |
| PDF 썸네일 | Apache PDFBox 3.0.3 |
| 이미지 리사이즈 | Thumbnailator 0.4.20 |
| 엑셀 내보내기 | Apache POI 5.3.0 |
| 이메일 | Spring Mail (SMTP) |
| 클라우드 스토리지 | AWS SDK v2 (Cloudflare R2 / AWS S3 호환) |
| UI | Bootstrap 5.3, Bootstrap Icons |
| 기타 | Lombok |

---

## 프로젝트 구조

```
eactive-resource-hub/
├── src/
│   └── main/
│       ├── java/com/eactive/resourcehub/
│       │   ├── ResourceHubApplication.java      # 진입점
│       │   ├── common/
│       │   │   ├── config/                      # StorageConfig, AsyncConfig 등
│       │   │   ├── exception/                   # GlobalExceptionHandler (403/404/500)
│       │   │   ├── security/                    # SecurityConfig, CustomUserDetails
│       │   │   └── util/                        # FileUtils
│       │   ├── user/                            # 사용자, 인증, 회원가입, 비밀번호 재설정
│       │   ├── team/                            # 팀 관리
│       │   ├── document/                        # 문서, 폴더, 업로드, 검색, 썸네일
│       │   ├── permission/                      # 폴더 접근 권한
│       │   ├── audit/                           # 감사 로그, 다운로드 통계
│       │   └── template/                        # 이력서 템플릿
│       └── resources/
│           ├── application.yml                  # 전체 설정
│           ├── db/migration/                    # Flyway SQL 마이그레이션
│           └── templates/                       # Thymeleaf HTML 템플릿
├── docs/                                        # 스펙·보안 정책 문서
├── 사용법/                                       # 역할별 사용 가이드
│   ├── 사원_사용법.md
│   ├── 영업_사용법.md
│   └── 관리자_사용법.md
├── scripts/
│   └── security-lint.sh                         # 보안 린트 스크립트 (15개 규칙)
├── docker-compose.yml                           # 로컬 개발용
├── docker-compose.prod.yml                      # 운영 배포용
├── Dockerfile
├── .env.example                                 # 환경변수 템플릿 (복사해서 .env로 사용)
└── build.gradle
```

---

## 빠른 시작 (로컬)

### 사전 요구사항

- **Java 21** JDK 설치 (`java -version`으로 확인)
- **Docker Desktop** 설치 및 실행 중
- Git

### 1단계 — 저장소 복제

```bash
git clone <저장소_URL>
cd eactive-resource-hub
```

### 2단계 — 환경변수 파일 생성

```bash
cp .env.example .env
```

`.env` 파일을 열어 아래 필수 항목을 수정합니다:

```env
# DB 비밀번호 (두 항목을 동일한 값으로 설정)
POSTGRES_PASSWORD=my_secure_db_password
SPRING_DATASOURCE_PASSWORD=my_secure_db_password

# 최초 관리자 계정 비밀번호 (앱 최초 실행 시 자동 생성)
RESOURCEHUB_ADMIN_PASSWORD=Admin@2024!
```

### 3단계 — PostgreSQL 컨테이너 실행

```bash
docker compose up -d postgres
```

정상 기동 확인:

```bash
docker compose ps
# NAME                    STATUS
# resourcehub-postgres    Up (healthy)
```

`healthy` 상태가 될 때까지 최대 30초 대기합니다.

### 4단계 — 애플리케이션 실행

```bash
# macOS / Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

최초 실행 시 Flyway가 자동으로 모든 마이그레이션을 적용하고, 관리자 계정과 시드 데이터를 생성합니다. 로그에 `Started ResourceHubApplication`이 출력되면 준비 완료입니다.

### 5단계 — 접속

브라우저에서 `http://localhost:8080` 접속.

최초 관리자 계정으로 로그인:

- **이메일**: `admin@eactive.co.kr` (또는 `RESOURCEHUB_ADMIN_EMAIL` 값)
- **비밀번호**: `.env`의 `RESOURCEHUB_ADMIN_PASSWORD` 값

헬스체크 확인:

```bash
curl http://localhost:8080/health
# OK
```

---

## 환경변수 상세 설명

`.env` 파일에 작성하며, `docker compose` 및 `./gradlew bootRun` 모두에서 자동으로 읽힙니다.

### 필수 항목 (반드시 설정)

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `POSTGRES_PASSWORD` | PostgreSQL 컨테이너 비밀번호 | `StrongPass!123` |
| `SPRING_DATASOURCE_PASSWORD` | Spring이 DB 접속 시 사용하는 비밀번호 (위와 동일한 값) | `StrongPass!123` |
| `RESOURCEHUB_ADMIN_PASSWORD` | 최초 관리자 계정 비밀번호 (8자 이상 권장) | `Admin@2024!` |

### 선택 항목 (기본값 있음)

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/resourcehub` | DB 접속 URL |
| `SPRING_DATASOURCE_USERNAME` | `resourcehub` | DB 사용자명 |
| `RESOURCEHUB_UPLOAD_BASE_DIR` | `./storage/uploads` | 업로드 파일 저장 경로 |
| `RESOURCEHUB_ADMIN_EMAIL` | `admin@eactive.co.kr` | 최초 관리자 이메일 |
| `APP_PORT` | `8080` | 호스트 포트 |

### 스토리지 관련 (S3 사용 시에만 설정)

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `RESOURCEHUB_STORAGE_TYPE` | `local` 또는 `s3` | `s3` |
| `RESOURCEHUB_S3_ENDPOINT` | S3 엔드포인트 URL | `https://xxxx.r2.cloudflarestorage.com` |
| `RESOURCEHUB_S3_ACCESS_KEY` | S3 액세스 키 ID | `abc123...` |
| `RESOURCEHUB_S3_SECRET_KEY` | S3 시크릿 액세스 키 | `xyz987...` |
| `RESOURCEHUB_S3_BUCKET` | 버킷 이름 | `eactive-uploads` |
| `RESOURCEHUB_S3_REGION` | 리전 | `auto` (R2) / `ap-northeast-2` (AWS) |

### 이메일(SMTP) 관련

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `SPRING_MAIL_HOST` | SMTP 서버 주소 | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | SMTP 포트 | `587` |
| `SPRING_MAIL_USERNAME` | SMTP 계정 | `noreply@eactive.co.kr` |
| `SPRING_MAIL_PASSWORD` | SMTP 비밀번호 또는 앱 비밀번호 | `abcd efgh ijkl mnop` |

### 개발용 테스트 계정

| 변수명 | 설명 |
|--------|------|
| `RESOURCEHUB_SEED_TEST_PASSWORD` | 영업(SALES) 테스트 계정 `test@eactive.co.kr`의 비밀번호. **운영 환경에서는 절대 설정 금지** |

---

## 데이터베이스 설정

### 로컬 개발 (Docker 사용)

```bash
# PostgreSQL 컨테이너만 실행
docker compose up -d postgres

# 컨테이너 내부에서 직접 접속 (선택사항)
docker exec -it resourcehub-postgres psql -U resourcehub -d resourcehub

# 테이블 목록 확인
\dt

# 종료
\q
```

앱 시작 시 Flyway가 자동으로 마이그레이션을 실행합니다. 수동 SQL 실행이나 DDL 작업은 필요하지 않습니다.

### 직접 설치된 PostgreSQL 사용 시 (Docker 없이)

```bash
# PostgreSQL 접속 후 DB 및 사용자 생성
psql -U postgres

CREATE USER resourcehub WITH PASSWORD 'your_password';
CREATE DATABASE resourcehub OWNER resourcehub;
GRANT ALL PRIVILEGES ON DATABASE resourcehub TO resourcehub;
\q
```

`.env`에서 아래 항목 수정:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/resourcehub
SPRING_DATASOURCE_USERNAME=resourcehub
SPRING_DATASOURCE_PASSWORD=your_password
# POSTGRES_PASSWORD는 Docker 사용 시에만 필요, 직접 설치 시 무시됨
```

### 마이그레이션 관리

- 파일 위치: `src/main/resources/db/migration/`
- 네이밍: `V{숫자}__{설명}.sql` (예: `V210__add_department_column.sql`)
- 번호 체계: V1~V6 (MVP1), V100~V2xx (MVP2), V300~ (MVP3)
- **한 번 적용된 마이그레이션 파일은 절대 수정하지 마세요.** 변경이 필요하면 새 번호로 추가합니다.

```bash
# 다음 마이그레이션 번호 확인
ls src/main/resources/db/migration/ | sort -V | tail -3

# 새 마이그레이션 파일 생성
touch src/main/resources/db/migration/V210__my_change.sql
```

---

## 파일 스토리지 설정

### 로컬 디스크 (기본값, 별도 설정 불필요)

```env
RESOURCEHUB_STORAGE_TYPE=local
RESOURCEHUB_UPLOAD_BASE_DIR=./storage/uploads
```

`./storage/uploads/` 디렉터리에 UUID 파일명으로 저장됩니다. 디렉터리가 없으면 자동 생성됩니다.

### Cloudflare R2 (S3 호환 클라우드 스토리지)

**R2 버킷 생성 및 API 토큰 발급 절차:**

1. [Cloudflare 대시보드](https://dash.cloudflare.com) → 좌측 **R2 Object Storage** → **버킷 만들기**
2. 버킷 이름 입력 (예: `eactive-uploads`) → 생성
3. 대시보드 우측 사이드바에서 **계정 ID** 복사 (엔드포인트 URL 구성에 사용)
4. R2 페이지 → **R2 API 토큰 관리** → **API 토큰 만들기**
   - 권한: `객체 읽기 및 쓰기`
   - 버킷: 위에서 만든 버킷 선택
   - 생성 후 표시되는 **액세스 키 ID**와 **시크릿 액세스 키** 복사 (다시 볼 수 없으므로 즉시 저장)

`.env` 설정:

```env
RESOURCEHUB_STORAGE_TYPE=s3
RESOURCEHUB_S3_ENDPOINT=https://<계정_ID>.r2.cloudflarestorage.com
RESOURCEHUB_S3_ACCESS_KEY=<액세스_키_ID>
RESOURCEHUB_S3_SECRET_KEY=<시크릿_액세스_키>
RESOURCEHUB_S3_BUCKET=eactive-uploads
RESOURCEHUB_S3_REGION=auto
```

### AWS S3 사용 시

1. AWS 콘솔 → **S3** → **버킷 만들기** → 버킷 이름, 리전 선택
2. **IAM** → **사용자** → 사용자 생성 → **액세스 키** 탭 → 액세스 키 만들기
3. 해당 사용자에 S3 버킷 접근 정책 연결 (최소 권한: `s3:GetObject`, `s3:PutObject`, `s3:DeleteObject`)

```env
RESOURCEHUB_STORAGE_TYPE=s3
RESOURCEHUB_S3_ENDPOINT=           # 비워둠 (AWS 기본 엔드포인트 자동 사용)
RESOURCEHUB_S3_ACCESS_KEY=AKIA...
RESOURCEHUB_S3_SECRET_KEY=wJalrXUt...
RESOURCEHUB_S3_BUCKET=eactive-uploads
RESOURCEHUB_S3_REGION=ap-northeast-2
```

---

## 이메일(SMTP) 설정

이메일은 선택 사항입니다. **미설정 시 이메일 내용이 서버 로그(콘솔)에 출력**되므로 기능 동작에는 지장이 없습니다.

이메일이 발송되는 시점:
- 회원가입 시 이메일 인증 코드
- 비밀번호 재설정 코드
- 문서 승인 또는 반려 알림

### Gmail 앱 비밀번호 발급

Gmail은 일반 비밀번호로 SMTP 접속이 불가합니다. **앱 비밀번호**를 발급해야 합니다.

1. Google 계정(myaccount.google.com) → **보안** 탭
2. **2단계 인증** 활성화 (필수 선행)
3. **앱 비밀번호** 검색 → 앱: `메일`, 기기: `기타(직접 입력)` → 이름 입력 후 생성
4. 표시되는 **16자리 비밀번호** 복사 (공백 포함 또는 제외 모두 사용 가능)

```env
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=abcdefghijklmnop
```

### 회사 SMTP 서버

```env
SPRING_MAIL_HOST=mail.eactive.co.kr
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=noreply@eactive.co.kr
SPRING_MAIL_PASSWORD=smtp_password
```

---

## Docker 전체 스택 실행

PostgreSQL + 앱을 모두 컨테이너로 실행하는 방법입니다.

```bash
# 전체 스택 빌드 및 실행 (--profile full 필요)
docker compose --profile full up -d --build

# 로그 스트리밍
docker compose logs -f app

# 앱만 재빌드
docker compose --profile full up -d --build app

# 전체 종료
docker compose down

# 데이터까지 초기화 (DB 볼륨 포함 삭제)
docker compose down -v
```

> 로컬 개발 중 Gradle bootRun을 사용한다면 `docker compose up -d postgres`만으로 충분합니다.

---

## 운영 서버 배포

> ⚠️ **`docker-compose.yml`은 개발 전용입니다.** 운영 배포는 반드시 아래 절차를 따르세요.

### 1. `.env` 파일 작성

```bash
# 서버에서 .env 생성
cp .env.example .env   # 없으면 직접 생성
vim .env
```

운영용 필수 환경변수:

```dotenv
# DB
POSTGRES_PASSWORD=<openssl rand -base64 32 결과>

# 관리자 계정
RESOURCEHUB_ADMIN_EMAIL=admin@example.com
RESOURCEHUB_ADMIN_PASSWORD=<강력한 비밀번호>

# 도메인 (Caddy HTTPS 자동 인증서)
CADDY_DOMAIN=hub.example.com

# 회사 이메일 도메인
RESOURCEHUB_COMPANY_EMAIL_DOMAIN=example.com

# SMTP (미설정 시 이메일 알림 비활성)
SPRING_MAIL_HOST=smtp.example.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=noreply@example.com
SPRING_MAIL_PASSWORD=<SMTP 비밀번호>

# ⚠️ 운영 환경에서는 반드시 주석 처리 또는 삭제
# RESOURCEHUB_SEED_TEST_PASSWORD=
```

### 2. 배포 실행

```bash
bash scripts/deploy.sh
```

스크립트가 자동으로:
- 필수 환경변수 누락 여부 검사
- `RESOURCEHUB_SEED_TEST_PASSWORD` 설정 시 경고 및 중단
- `docker-compose.prod.yml`로 빌드·기동
- 헬스체크 통과 확인

### 3. 확인

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f app
```

---

## 역할 구조

| 역할 | 표시명 | 접근 범위 |
|------|--------|-----------|
| `ADMIN` | 관리자 | 전체 관리 (직원·팀 관리, 문서 검토·삭제, 통계, 권한 부여, 시스템 설정) |
| `SALES` | 영업 | 전 직원 프로필·문서 열람, 다운로드, 경력 계산기, 엑셀 내보내기 |
| `EMPLOYEE` | 사원 | 본인 폴더만 접근 (업로드, 다운로드, 만료일 설정) |

역할은 관리자가 직원 상세 페이지에서 변경할 수 있습니다.

---

## 사용법 문서

역할별 상세 사용법은 `사용법/` 폴더를 참고하세요:

- [사원 사용법](사용법/사원_사용법.md) — 회원가입, 이메일 인증, 문서 업로드, 내 폴더 관리
- [영업 사용법](사용법/영업_사용법.md) — 직원 검색, 문서 열람·다운로드, 경력 계산기
- [관리자 사용법](사용법/관리자_사용법.md) — 직원 관리, 문서 검토·승인, 팀·권한 관리, 통계

---

## 보안 주의사항

- **JWT 사용 금지** — Spring Security 세션 기반 인증만 사용합니다.
- **CSRF 항상 활성화** — 모든 상태 변경 요청에 CSRF 토큰이 자동으로 포함됩니다.
- **파일 직접 노출 금지** — 업로드 파일은 컨트롤러 경유로만 제공되며, UUID 파일명으로 저장합니다.
- `.env`, `storage/`, `logs/` 는 `.gitignore`에 포함되어 있으며 Git에 커밋하지 마세요.
- 운영 환경에서는 반드시 HTTPS를 적용하고 `cookie.secure: true`로 설정하세요.

보안 린트 실행:

```bash
bash scripts/security-lint.sh
# 보안 린트 통과 — 위반 없음
```
