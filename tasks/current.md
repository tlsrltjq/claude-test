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

## 이전 세션에서 멈춘 곳
2026-06-01: 버그 수정 및 UX 개선 세션 완료.

완료 항목:
- fix: 문서 검토 UI — documents-review.html `members-card/members-table` → `table-card/assign-table`, card-hd 추가
         document-review-detail.html `<div class="card-body">` 래퍼 제거 (Bootstrap padding 충돌)
- fix: DocumentController preview/download/thumbnail — `catch (IOException)` → `catch (Exception)`
         S3 NoSuchKeyException(RuntimeException) 이 500 아닌 404로 처리되도록 수정
- feat: SampleDataFixRunner — 앱 기동 시 샘플 PDF 생성 후 566개 demo/* 경로 → sample/placeholder.pdf 일괄 교체
- fix: 직원 관리(/admin/employees) 정렬 버그 4개 수정
  · 이름: role 고정 1차 정렬 제거 → 전체 ㄱㄴㄷ순
  · 팀: ALLOWED_SORTS 추가, Sort.by("team.name"), 헤더 버튼 추가
  · 권한: role ASC prefix 제거 → DESC/ASC 전환 정상화
  · 직급: Position.ordinal() 기준 in-memory 정렬 (대표→사원)
- feat: 캘린더 인력 현황 전 컬럼(직원/직급/상태/투입프로젝트/날짜) 정렬 — 클라이언트 JS, 기존 필터와 연동
- feat: 캘린더 월 네비게이션 달력 그리드 바로 위로 이동, 프로젝트 등록 버튼 목록 카드 헤더로 이동
- 전체 빌드: BUILD SUCCESSFUL, security-lint 18/18 PASS

**다음 작업 없음 — 사용자 지시 대기**
