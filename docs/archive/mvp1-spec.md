# eActive Resource Hub — 프로젝트 스펙 (SSOT)

> 이 문서는 hub.pdf 기획서를 마크다운으로 정리한 단일 진실 원천(Single Source of Truth)입니다.
> 모든 단계의 AI 작업은 이 문서를 컨텍스트로 참조해야 합니다.
> 변경이 필요하면 이 문서를 먼저 갱신한 뒤, 영향 받는 단계의 prompt/acceptance를 함께 수정하세요.

---

## 1. 프로젝트 목적

eActive Resource Hub는 직원별 이력서, 경력기술서, 졸업증명서, 자격증, 기타 증빙자료를 한곳에서 관리하는 **내부 문서 관리 포털**이다.

기존에는 영업팀이나 관리자 쪽에서 직원에게 매번 연락해 자료를 받아야 했고, 최신본 여부도 확인하기 어려웠다. 이 시스템은 이런 불편을 줄이고 직원 자료를 권한에 따라 안전하게 조회·관리할 수 있도록 만드는 것이 목적이다.

| 사용자 | 가능한 행위 |
|--------|------------|
| 직원 | 본인 자료 업로드/수정 |
| 관리자 | 전체 직원 자료 열람/다운로드/권한 관리 |
| 팀장 | 본인 팀 자료 열람 |
| 개별 권한자 | 관리자가 허용한 특정 직원 자료 열람 |

---

## 2. 전체 방향

- 초기 개발: 로컬 PC
- 운영 전환: 회사 VM
- 접속 방식: 내부망 또는 VPN
- 파일 저장: 로컬 디스크 기반
- DB: PostgreSQL
- 웹 방식: Spring Boot 기반 내부 웹 포털

> 회사 홈페이지에는 "임직원 포털" 버튼만 두고 실제 시스템은 별도 내부 웹으로 운영한다.

---

## 3. 시스템 이름

**eActive Resource Hub** — 직원 자료, 인력 프로필, 증명서, 영업 지원 자료를 모아두는 내부 리소스 허브.

확장 시 영업자료실, 회사소개서, 솔루션 브로슈어, 제안서 템플릿 등도 붙일 수 있다.

---

## 4. 핵심 사용자

### 관리자 (ADMIN)
모든 팀과 모든 직원의 폴더에 접근 가능. 가능한 기능:
- 전체 직원 폴더 열람
- 전체 문서 미리보기
- 전체 문서 다운로드
- 사용자 권한 부여
- 팀장 권한 부여
- 개별 폴더 접근 권한 부여
- 문서 관리
- 로그 확인

### 팀장 (TEAM_LEADER)
관리자가 권한을 부여한 팀만 볼 수 있다. 다른 팀 폴더는 볼 수 없다.

### 일반 직원 (EMPLOYEE)
본인 개인 폴더만 접근 가능:
- 본인 문서 업로드
- 본인 문서 수정
- 본인 문서 삭제 요청 또는 휴지통 이동
- 본인 문서 미리보기

### 개별 권한 사용자 (CUSTOM_VIEWER)
관리자가 특정 직원 폴더에 대한 접근 권한을 따로 줄 수 있다.

---

## 5. 주요 기능

### 5.1 회사 이메일 회원가입
- 회사 도메인 이메일만 가입 가능 (예: `@eactive.co.kr`)
- 이메일 형식 검증만으로는 부족 — 6자리 인증 코드 메일 발송 필수
- 관리자 승인 후 사용 가능

### 5.2 로그인 / 권한 관리
권한은 1차에서 다음 4가지로 충분:
- `ADMIN` → 전체 관리자
- `TEAM_LEADER` → 팀장
- `EMPLOYEE` → 일반 직원
- `CUSTOM_VIEWER` → 개별 접근 권한 사용자 (역할이라기보다 EMPLOYEE + 개별 permission)

### 5.3 팀 카테고리
보기 편하게 팀별로 나누고, 권한 관리 기준으로도 사용. 초기 팀: 개발팀, 영업팀, 경영지원팀, 기술지원팀.

### 5.4 개인 폴더
직원마다 개인 폴더가 자동 생성. 폴더 내 문서 분류:
- 이력서, 경력기술서, 졸업증명서, 자격증, 재직증명서, 기타 증빙자료

### 5.5 파일 업로드
1차 허용 파일: PDF, JPG, PNG, DOCX, HWP, HWPX
- 웹 미리보기는 우선 PDF/이미지 중심
- DOCX/HWP/HWPX는 원본 저장은 가능하지만 미리보기는 별도로 PDF 업로드 권장

### 5.6 문서 미리보기
- PDF → 웹에서 바로 보기
- JPG/PNG → 웹에서 바로 보기
- DOCX/HWP/HWPX → 별도 미리보기 PDF 파일 사용

### 5.7 문서 썸네일
관리자가 직원 폴더 진입 시 문서가 한눈에 보이도록 카드 뷰 + 썸네일.

### 5.8 다운로드 + 사유 입력
관리자/권한자만 가능. 다운로드 사유 입력 필수, 로그 저장.

### 5.9 열람 / 다운로드 로그 (필수)
기록 항목: 사용자, 접근 시간, 접근 IP, 대상 직원, 대상 문서, 행위 유형(미리보기/다운로드/업로드/수정/삭제/권한 변경), 다운로드 사유.

### 5.10 관리자 권한 부여 (1차)
1. 전체 접근 권한
2. 팀 단위 접근 권한
3. 개인 폴더 접근 권한

> 문서 단위 권한은 1차에서 제외(2차로).

---

## 6. 1차 MVP 기능 목록

| # | 기능 | 우선순위 |
|---|------|----------|
| 1 | 회사 이메일 회원가입 | 필수 |
| 2 | 이메일 인증 | 필수 |
| 3 | 로그인/로그아웃 | 필수 |
| 4 | 사용자 권한 관리 | 필수 |
| 5 | 팀 카테고리 관리 | 필수 |
| 6 | 개인 폴더 자동 생성 | 필수 |
| 7 | 본인 문서 업로드 | 필수 |
| 8 | 본인 문서 수정 | 필수 |
| 9 | 관리자 전체 폴더 열람 | 필수 |
| 10 | 팀장 팀 단위 열람 | 필수 |
| 11 | 개별 폴더 접근 권한 부여 | 필수 |
| 12 | PDF/이미지 미리보기 | 필수 |
| 13 | 문서 다운로드 | 필수 |
| 14 | 다운로드 로그 | 필수 |
| 15 | 열람 로그 | 권장 |
| 16 | 문서 썸네일 | 권장 |
| 17 | 파일 버전 관리 | 권장 |
| 18 | 휴지통/복구 | 권장 |
| 19 | 관리자 승인/반려 | 권장 |

---

## 7. 1차에서 제외 기능

모바일 앱, React 분리, SSO, 전자결재, OCR, AI 이력서 자동 생성, HWP 자동 변환, DOCX 자동 PDF 변환, 외부 공유 링크, 복잡한 통계 대시보드, 문서 단위 상세 권한.

---

## 8. 기술 스택

| 구분 | 선택 |
|------|------|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.5.x |
| 빌드 | Gradle |
| DB | PostgreSQL |
| ORM | Spring Data JPA |
| DB 마이그레이션 | Flyway |
| 인증/권한 | Spring Security (세션 기반, JWT 미사용) |
| 화면 | Thymeleaf + Bootstrap |
| 파일 저장 | 로컬 디스크 (`./storage/uploads`) |
| 로컬 DB 실행 | Docker Compose |
| IDE | IntelliJ IDEA |
| DB 툴 | DBeaver |
| API 테스트 | Bruno 또는 Postman |
| 형상관리 | Git + GitHub private repo |

---

## 9. DB 선택 — PostgreSQL

이유: 관계형 데이터에 적합, 안정적, Spring Boot/JPA와 궁합 좋음, Docker 구성 쉬움.

---

## 10. 파일 저장 방식

- 초기에는 클라우드 저장소 미사용
- 로컬 개발: `./storage/uploads`
- 운영 VM: `/data/eactive-resource-hub/uploads`
- 파일은 DB에 직접 넣지 않고 메타데이터만 저장
- 파일명은 UUID로 저장, 원본 파일명은 DB에 별도 저장

```
원본 파일명: 신기섭_이력서.pdf
저장 파일명: 2f7e3c9d-92e2-4c27-9e18-0b4d8d212aaa.pdf
```

---

## 11. 파일 접근 방식

파일 폴더를 웹에 직접 열어두면 안 된다. 모든 접근은 컨트롤러를 통해:

```
/documents/{documentId}/preview
/documents/{documentId}/download
```

처리 흐름: 사용자 요청 → 로그인 확인 → 권한 확인 → 열람/다운로드 로그 저장 → 서버가 파일 스트림 반환.

---

## 12. 저장소 확장성

```java
public interface FileStorage {
    StoredFile store(FileUploadCommand command);
    Resource load(String storedFileName);
    void delete(String storedFileName);
}
```

1차 구현체: `LocalFileStorage`. 추후 `NasFileStorage`, `S3FileStorage`, `MinioFileStorage`.

---

## 13. 로컬 개발 구조

```
eactive-resource-hub/
├─ src/
├─ docs/
├─ scripts/
├─ docker-compose.yml
├─ .env.example
├─ README.md
└─ storage/
   └─ uploads/
```

`.gitignore`: `.env`, `storage/`, `logs/`, `*.log`.

---

## 14. Docker Compose (로컬)

PostgreSQL만 Docker로 띄우고, Spring Boot는 IntelliJ에서 실행.

```yaml
services:
  postgres:
    image: postgres:18
    container_name: resourcehub-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: resourcehub
      POSTGRES_USER: resourcehub
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - resourcehub_postgres_data:/var/lib/postgresql/data
volumes:
  resourcehub_postgres_data:
```

---

## 15. 설정 방식

경로/비밀번호를 코드에 박지 말고 application.yml + 환경변수.

```
resourcehub.upload.base-dir=${RESOURCEHUB_UPLOAD_BASE_DIR:./storage/uploads}
```

- 로컬: `RESOURCEHUB_UPLOAD_BASE_DIR=./storage/uploads`
- 운영 VM: `RESOURCEHUB_UPLOAD_BASE_DIR=/data/eactive-resource-hub/uploads`

---

## 16. 패키지 구조

```
com.eactive.resourcehub
├─ common (config, exception, security, file)
├─ user (controller, service, repository, entity, dto)
├─ team
├─ employee
├─ document
├─ permission
└─ audit
```

핵심 도메인:
- `user` — 로그인 사용자
- `team` — 팀 카테고리
- `employee` — 직원 프로필
- `document` — 문서/파일
- `permission` — 접근 권한
- `audit` — 열람/다운로드/권한 변경 로그

---

## 17. DB 테이블 (1차)

`users`, `teams`, `employee_profiles`, `folders`, `documents`, `document_versions`, `permissions`, `audit_logs`, `email_verification_tokens` (3단계에서 추가).

후속 마이그레이션:
- V3: `document_versions`에 썸네일 컬럼 추가 (8단계)
- V4: `document_versions`에 검토 컬럼 추가 (9단계)

> 상세 컬럼은 stages/02-db-flyway/prompt.md 참조.

---

## 18. 권한 정책

### Role 기반
`ADMIN`, `TEAM_LEADER`, `EMPLOYEE`.

### 개별 접근 권한
`permissions` 테이블로 특정 사용자에게 특정 폴더 접근 권한 부여.

### 접근 판단 순서
```
관리자인가? → 전체 접근 허용
본인 폴더인가? → 접근 허용
팀장이고 해당 직원이 본인 팀인가? → 접근 허용
관리자가 개별 접근 권한을 줬는가? → 접근 허용
그 외 → 접근 차단
```

---

## 19. 화면 구성

- **로그인/회원가입**: 로그인, 회사 이메일 회원가입, 이메일 인증, 관리자 승인 대기
- **메인 대시보드**: 내 폴더 바로가기, 최근 업로드/열람, 관리자 알림
- **팀별 직원 목록**: 개발팀, 영업팀, 경영지원팀, 기술지원팀
- **개인 폴더 화면**: 이력서, 경력기술서, 졸업증명서, 자격증, 기타 문서
- **문서 상세**: 문서 미리보기, 정보, 버전, 다운로드 버튼, 열람/다운로드 로그
- **관리자 화면**: 전체 직원 목록, 전체 팀 관리, 사용자 권한 관리, 개별 접근 권한 부여, 전체 로그 조회

---

## 20. 보안 기준 (필수)

- 로그인 필수
- 회사 이메일 가입 제한
- 이메일 인증
- 권한별 접근 제어
- 파일 직접 접근 차단
- 다운로드/열람 로그 저장
- 비밀번호 암호화(BCrypt)
- 업로드 파일 확장자/크기 제한
- Git에 업로드 파일 제외

운영 전환 시 추가: 내부망/VPN 접속 제한, HTTPS, 서버/DB/파일 백업, 퇴사자 계정 비활성화, 관리자 권한 변경 로그.

---

## 21. 백업 정책 (운영)

매일 새벽: PostgreSQL dump + uploads 폴더 백업 → 다른 서버/NAS에 복사. 같은 VM 안에만 백업하면 백업이 아니다.

---

## 22. 개발 순서 (10단계)

| 단계 | 내용 |
|------|------|
| 1 | 프로젝트 골격 (Spring Boot + Gradle + Postgres + Flyway) |
| 2 | DB/Flyway 기본 스키마 + JPA Entity |
| 3 | 회원가입/로그인 (회사 이메일, 이메일 인증, 관리자 승인, 세션 인증) |
| 4 | 팀/직원/폴더 (관리자 승인 처리, 팀 관리, 개인 폴더 자동 생성) |
| 5 | 파일 업로드 (FileStorage, 본인 폴더 업로드, 버전 관리) |
| 6 | 문서 미리보기/다운로드 + VIEW/DOWNLOAD 로그 |
| 7 | 권한 관리 (팀장 권한, 개별 폴더 접근 권한) |
| 8 | 썸네일 + 카드 뷰 + 필터/검색/정렬 |
| 9 | 문서 승인/반려 프로세스 |
| 10 | 보안/배포/백업 (환경 분리, 운영 Docker, 백업 스크립트, 운영 체크리스트) |

> 상세 프롬프트는 `harness/stages/NN-XXX/prompt.md` 참조.

---

## 23. 최종 1차 목표

- 사용자가 회사 이메일로 가입한다.
- 로그인하면 본인 폴더가 보인다.
- 본인 폴더에 문서를 업로드한다.
- PDF와 이미지는 웹에서 바로 볼 수 있다.
- 관리자는 모든 직원 폴더를 볼 수 있다.
- 관리자는 문서를 미리보고 다운로드할 수 있다.
- 팀장은 본인 팀 직원 폴더만 볼 수 있다.
- 관리자는 특정 사용자에게 특정 직원 폴더 접근 권한을 줄 수 있다.
- 문서 열람과 다운로드 기록이 남는다.

---

## 24. 한 줄 요약

처음에는 로컬에서 작게 만들고, 나중에 회사 VM으로 옮길 수 있게 파일 저장소와 설정을 분리한 내부 문서 관리 포털.

---

## 25. AI 작업 시 절대 원칙 (모든 단계 공통)

이 항목은 PDF에는 없지만, 모든 단계 프롬프트에서 반복되는 핵심 제약을 추출한 것이다.

1. **JWT 사용 금지** — 인증은 Spring Security 세션 기반
2. **Remember-me 사용 금지**
3. **CSRF 활성화**
4. **세션 timeout 30분, 쿠키명 `RESOURCEHUB_SESSION`, httpOnly+sameSite=strict**
5. **파일 폴더를 정적 리소스로 직접 공개 금지** — 모든 파일 접근은 컨트롤러 경유
6. **파일은 UUID로 저장**, 원본 파일명은 DB에 별도 저장
7. **DB에는 메타데이터만**, 실제 파일은 디스크에
8. **민감정보(비밀번호, 인증코드, 파일 경로)는 로그/응답에 노출 금지**
9. **권한 확인은 Service로 분리** — Controller에 흩뿌리지 말 것
10. **모든 단계는 PDF 검증 항목을 README/체크리스트로 옮길 것**
