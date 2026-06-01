# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V227.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 18/18 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓

## 현재 작업: 오피스 파일 미리보기 (LibreOffice PDF 변환)

### 목표
docx·hwpx·pptx·ppt·xlsx·xls 업로드 시 LibreOffice로 PDF 변환 후 미리보기 제공.
HWP는 변환 품질 문제로 미리보기 버튼 숨김(미지원).

### 구현 범위
1. **Dockerfile** — Alpine → Debian 기반으로 전환, LibreOffice + 나눔폰트 설치
2. **`OfficePreviewService`** (신규) — soffice --headless로 PDF 변환, 비동기, 60초 타임아웃
3. **`DocumentUploadService`** — 오피스 파일 업로드 후 OfficePreviewService 호출
4. **`DocumentPreviewResolver`** — pptx·ppt·xlsx·xls 지원 추가, hwp 제외
5. **`ThumbnailService`** — OFFICE_EXTS 에서 hwp 제거, pptx·ppt·xlsx·xls 추가
6. **app.libreoffice.enabled** 환경변수 — false 시 변환 건너뜀 (개발환경 대응)

### 변환 대상 / 제외
| 확장자 | 처리 |
|--------|------|
| docx | LibreOffice 변환 |
| hwpx | LibreOffice 변환 |
| pptx | LibreOffice 변환 |
| ppt  | LibreOffice 변환 |
| xlsx | LibreOffice 변환 |
| xls  | LibreOffice 변환 |
| hwp  | 미지원 — 미리보기 버튼 숨김 |
| pdf·jpg·jpeg·png | 기존 로직 유지 |

### DB 마이그레이션
불필요 — V1에서 `preview_file_name`, `preview_storage_path` 컬럼 이미 존재

### 완료 기준
- 오피스 파일 업로드 후 미리보기 iframe에 PDF 표시
- hwp는 미리보기 버튼 미노출
- LibreOffice 미설치 환경에서 graceful skip (업로드 실패 없음)
- `./gradlew build` BUILD SUCCESSFUL
- security-lint 18/18 PASS

## 이전 세션에서 멈춘 곳
2026-06-01: 보안 린트 #16~18 추가, UserRole.TEAM_LEADER·TeamController 삭제, 문의 이메일 환경변수화 완료.
