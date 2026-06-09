# 현재 작업 컨텍스트

## 건드리면 안 되는 파일
- `src/main/resources/db/migration/V1~V230.sql` — 기존 마이그레이션 절대 수정 금지
- `src/main/java/com/eactive/resourcehub/common/security/SecurityConfig.java`
- `.env`, `.env.example`
- `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml`
- `harness/archive/legacy/**`

## 완료 기준 (달성)
- `bash scripts/security-lint.sh` 21/21 PASS ✓
- `./gradlew build` BUILD SUCCESSFUL ✓
- 531개 테스트 전 통과 ✓

## 이전 세션에서 멈춘 곳
2026-06-09: 날짜 입력 분리 필드 + 자동 탭 전체 적용 완료. type="date" → 연/월/일 분리 텍스트 필드(date-split) 교체. static/js/date-utils.js 신설(initDateSplits, setDateSplitValue). 적용 파일 7개: settings, search, project-new, project-detail(모달 3곳), my/document-detail, my/upload, sales/career-calculator(정적+addRow 동적 행). career-calculator autofill·toggleCertDate JS setDateSplitValue 전환. 일반사원 대시보드 프로젝트 없는 경우 빈 상태 UI 추가(dashboard.html EMPLOYEE 조건 확장). 빌드 BUILD SUCCESSFUL, Docker 재기동 정상.

2026-06-09: 인력표 전 사원(100명) 문서 시드 데이터 생성 완료. scripts/seed_documents.js 신설 — PDFKit+malgun.ttf(한글) PDF 생성, @aws-sdk/client-s3로 Cloudflare R2 직접 업로드, {팀명}/{이름}/UUID.pdf 저장 경로. 586개 문서·버전 DB 삽입, 577 APPROVED·6 PENDING_REVIEW·3 REJECTED, 만료 61·임박 64·유효 133. scripts/package.json(pdfkit, @aws-sdk/client-s3, dotenv) 추가. .gitignore에 scripts/node_modules 추가. 인력표 팀 미배정자 포함 여부 정책 미결(다음 세션 결정 필요).

2026-06-09: 내폴더·공용폴더 500 에러 긴급 수정 완료. 원인: 이전 CSP 수정 세션에서 img 태그에 class 속성 중복 추가 + 태그 미닫힘 → Thymeleaf ParseException. 수정 파일: public-folder.html, my/folder.html, admin/employee-documents.html, admin/employee-detail.html, sales/member-documents.html (class="doc-thumb doc-thumb-img" 병합, > 닫힘 추가).

2026-06-09: 대시보드 KPI↔캘린더 인력현황 숫자 불일치 수정 — getDeployStats()의 totalNonAdmin(팀 무관)을 findAssignableUsers()(isProjectTeam() 소속자)로 교체. 이번달 신규투입(류재호, 6/5~) 및 종료임박(전성민, ~6/20) 배정 시드. 종료 프로젝트 3개 추가(국세청·카카오뱅크·한국전력, 총 8개). 회원가입 주소 필수화(SignupRequest @NotBlank + JS 검증). 인력표 주소 컬럼 추가(이름-나이 사이, data-col="address", 클릭 팝업+클립보드 복사), 기타자료 컬럼 제거(templates+SalesProfileExporter 동기화).

완료 항목:
- feat: V230__add_expiry_notice_tracking.sql — documents.expiry_warn_sent_at, expired_notice_sent_at 추가 + 배포 백필(기존 임박·만료 문서 발송 처리)
- feat: Document.expiryWarnSentAt/expiredNoticeSentAt 필드 + mark 메서드, updateExpiresAt 시 이력 초기화
- feat: DocumentRepository.findExpiringSoonNeedingWarn/findExpiredNeedingNotice 알림 전용 쿼리 추가 (관리자 목록용 findExpired/findExpiringSoon은 유지)
- feat: DocumentExpiryService.sendExpiryNotifications — 임박 1회+만료 1회 고정, 발송 성공 시 마킹·실패 시 재시도, @Transactional(쓰기)로 변경 (ADR-045)
- test: DocumentExpiryServiceTest — 새 쿼리 모킹 전환 + 발송 성공/실패 시 마킹 검증 추가
- fix: 스케줄러·기동 초기화 예외 가드 — DocumentFileGcService(독립 try-catch)·ProjectStatusScheduler·AdminInitializer에 try-catch + log.error
- refactor: DocumentFileGcScheduler 신설 — 파일 GC 정기실행 분리로 self-invocation @Transactional 우회 해소, runGc/runOrphanScan 시그니처 무변경 (ADR-046)
- docs: HARNESS·data-model·spec·architecture·decisions(ADR-045·046)·CHANGELOG·tasks 최신화
- chore: 미추적 파일 정리 (AGENTS.md, scripts/seed-demo-*.sql 2개 삭제)
- 빌드 BUILD SUCCESSFUL, 보안 린트 21/21 PASS, 테스트 531개 전 통과

리팩터링 미진행 후보(사용자에게 보고만 함): AdminController 분리(god controller), Flash 메시지 헬퍼, userDetails 추출 — 필요 시 재논의

## 다음 작업 (백로그)

| 순서 | 작업 | 비고 |
|------|------|------|
| 1 | 고정 도메인 구매·적용 | 도메인 확정 후 Caddyfile·application-prod.yml·HSTS 설정 연동 |
| 2 | NAS 저장소 연동 | 현재 LocalFileStorage → NAS 마운트 경로 또는 SMB/NFS 연동으로 교체 (`docs/archive/storage-nas-migration.md` 참고) |
