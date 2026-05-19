# 변경 이력

> 형식: `YYYY-MM-DD | 단계 | feat/fix/chore/docs/refactor/test: 내용`
> 한 줄씩 누적. 옛 상세 이력은 `docs/archive/CHANGELOG-detailed.md` 참조.

---

## 하네스 개편

2026-05-19 | 정합성 감사 | chore: 소스 기준 하네스·문서 정합성 감사 — templates/team/ 빈 폴더 삭제, security-lint 15/15 PASS 확인, architecture.md 누락 클래스 식별(SalesMemberService·ProfileRow·audit/dto)
2026-05-18 | 하네스 개편 | chore: 범용 양식 도입 — CLAUDE.md / HARNESS.md / tasks/current.md / docs/architecture.md / docs/decisions.md / start_session.sh / end_session.sh 신설, 옛 자산은 harness/archive/legacy/ 와 docs/archive/ 로 이동

## 21단계 — 재직증명서 자동 발급

2026-05-18 | 운영 마감 | feat: 관리자 대시보드 빠른 메뉴에 파일 GC 카드 추가 (93cb444)
2026-05-18 | 운영 마감 | docs: 역할별 사용 가이드 업데이트 (f632159)
2026-05-18 | 운영 마감 | fix: 경력 계산기 직원 선택 시 학력 정보 초기화 (86592dc)
2026-05-18 | 운영 마감 | docs: 역할별 사용 가이드 추가 — 직원·영업·관리자 (fb3f8cf)
2026-05-18 | 운영 마감 | feat: 인력표 개선 및 문서 유형 확장 (d80e4be)
2026-05-15 | 21-certificate | chore: 재직증명서 시스템 하네스·문서 업데이트 + 전체 관리자 nav 링크 추가 (28129b5)
2026-05-15 | 21-certificate | docs: 관리자 사용가이드에 재직증명서 발급 섹션 추가 (cedacfb)
2026-05-15 | 21-certificate | fix: certificate 서비스 개발 포트 5000→5001 변경 — macOS Control Center 충돌 (73e3788)
2026-05-15 | 21-certificate | feat: 재직증명서 자동 발급 시스템 추가 — Python+Flask+LibreOffice 컨테이너 (73f758f)

## 20단계 — 운영 배포 준비

2026-05-15 | 20-ops-readiness | docs: 시놀로지 NAS 스토리지 전환 가이드 추가 — MinIO / NFS 두 가지 방법 (a89ad2a)
2026-05-15 | 20-ops-readiness | docs: 사용가이드 업데이트 — 업로드 취소·중복탐지·역할변경·파일GC (5eb1fce)
2026-05-15 | 20-ops-readiness | config: 운영 환경 앱 로그 레벨 WARN → INFO 상향 (6306f65)
2026-05-15 | 20-ops-readiness | docs: 하네스 스테이지 19·20 추가 + 문서 업데이트 (9f08867)
2026-05-15 | 20-ops-readiness | ops: 2단계 운영 준비 — 로그 파일 저장, 백업 크론 등록, 체크리스트 완료 (7c4bca3)
2026-05-15 | 20-ops-readiness | infra: 운영 배포 스크립트 추가 및 compose 파일 혼용 방지 — scripts/deploy.sh (0360807)
2026-05-15 | 20-ops-readiness | infra: Caddy 리버스 프록시 + HTTPS 설정 추가 — Let's Encrypt 자동 인증서 (2127594)
2026-05-15 | 20-ops-readiness | docs: 배포 체크리스트 작성 — 3단계 분류(필수/권장/선택) (78f98be)

## 19단계 — 업로드 안전성 강화

2026-05-15 | 19-upload-hardening | fix: 고아 파일 방지 및 주기적 orphan 스캔 GC 추가 — DocumentFileGcService.runOrphanScan (50ff30f)
2026-05-15 | 19-upload-hardening | ux: 업로드 중 취소 기능 추가 — XHR abort 기반 (c137014)
2026-05-14 | 19-upload-hardening | feat: 중복 파일 탐지 — SHA-256 체크섬 비교로 동일 파일 재업로드 차단 (15aaa05)
2026-05-14 | 19-upload-hardening | security: 파일 magic bytes 검증 + 동시 업로드 race condition 방지 (V211 partial unique index) (4dae09f)

## post-MVP3 마감 (UX / 보안 픽스)

2026-05-14 | post-MVP3 | ux: 경력계산기 2컬럼 레이아웃 — 결과·저장을 오른쪽 sticky 패널로 이동 (6b87acb)
2026-05-14 | post-MVP3 | fix: 경력 저장 시 실제 총 경력일수로 교정 (86b7b7b)
2026-05-14 | post-MVP3 | feat: 인력표 엑셀 문서 컬럼 파일명 → O/X 표시로 변경 (d6276bb)
2026-05-14 | post-MVP3 | fix: 업로드 XHR → JSON 응답 방식 전환으로 오류 메시지 오판 해소 (6f963ca)
2026-05-14 | post-MVP3 | fix: 업로드 XHR 오류 처리 개선 — 서버 실패 시 오류 메시지 직접 표시 (41948e5)
2026-05-14 | post-MVP3 | fix: 업로드 500·검색 403 버그 수정 (d5fc9ea)
2026-05-14 | post-MVP3 | docs: mvp3-spec·security-policy 문서 현행화 (d28878f)
2026-05-14 | post-MVP3 | feat: /sales/profiles ZIP 묶음 다운로드 — bundle-template 흡수 (a97ba6b)
2026-05-13 | post-MVP3 | ux: 업로드 진행률 바 추가 — XHR 방식 전환 (ed2c47e)
2026-05-13 | post-MVP3 | fix: 역할 변경 시 대상 사용자 세션 즉시 만료 (93bb648)
2026-05-12 | post-MVP3 | fix: 본인 업로드 파일 검토 상태 무관 접근 허용 (c006715)
2026-05-12 | post-MVP3 | fix: 전 사원 공용 폴더 EMPLOYEE 접근 시 403 수정 (26e6295)
2026-05-09 | post-MVP3 | ux: 업로드 종류별 제목 placeholder + 다운로드 이력 파일명 표시 (0fd83ad)
2026-05-09 | post-MVP3 | feat: 직급 변경 권한 관리자 전용으로 제한 (3a93907)
2026-05-09 | post-MVP3 | docs: 사용법 문서 3개 사실 오류 수정 (febde25)

## 17단계 — 파일 GC

2026-05-09 | 17-gc | feat: 파일 GC — 소프트 삭제 문서 물리 파일 자동 정리, @Scheduled(cron 0 0 2 * * *), V210, /admin/gc (4843234)

## 16단계 — 데이터 무결성 + 아키텍처 정리

2026-05-09 | 16-data-integrity | refactor: 컨트롤러 Repository 직접 주입 제거 + 문서·사용법 가이드 추가 (8da9eaa)
2026-05-08 | 16-data-integrity | perf: 직원 목록·영업 인력 조회 DB 레벨 필터링·페이지네이션 전환 (001e4cc)
2026-05-08 | 16-data-integrity | fix: SalesProfileQueryService toMap 중복 키 IllegalStateException 수정 (6d0d849)

## post-MVP3 / 설정 페이지 + UI 리디자인

2026-05-08 | post-MVP3-settings | feat: 설정 페이지 추가 + 자격증 종류 필드 제거 — /settings (9e7e54f)
2026-05-08 | post-MVP3-settings | feat: 경력계산기 자격증을 정보처리기사 단일로 고정 (d7afe13)
2026-05-08 | post-MVP3-ui | feat(ui): 미개선 화면 전체 UI 개선 — 19개 파일 (febc70d)
2026-05-08 | post-MVP3-ui | feat(ui): 관리자 패널·통계 UI 전면 개선 (f58f619)
2026-05-08 | post-MVP3-ui | feat(ui): 경력 계산기 UI 개선 (a62e684)
2026-05-08 | post-MVP3-ui | feat(ui): 전사 인력 목록·인력표 UI 개선 (91b3915)
2026-05-08 | post-MVP3-ui | feat(ui): 문서 검색·다운로드 이력 UI 개선 (c8ba3bc)
2026-05-08 | post-MVP3-ui | feat(ui): 공유 폴더·전사 공용 폴더 UI 개선 (a9c8764)
2026-05-08 | post-MVP3-ui | feat: 문서 메타 필드(certType·issuedDate·degreeType) + 경력계산기 자동채우기 + UI 개선 (8458637)
2026-05-08 | post-MVP3-ui | feat(dashboard): UI 전면 리디자인 (aef751d)
2026-05-08 | post-MVP3-ui | fix: 팀 자동 시딩 제거 + 로그인 UI 개선 (e0cf095)
2026-05-08 | post-MVP3-ui | feat(signup): UI 리디자인 + 전화번호·생년월일 자동 포맷 (762b67d)
2026-05-08 | post-MVP3 | fix: 경력저장 초기화·이메일 중복발송·인증시간·폴더자동생성·업로드 5건 (5456d40)
2026-05-08 | post-MVP3 | fix(career-calculator): 합계 일자 SpEL 오류 수정 → 저장 버튼 미표시 해결 (6f0bde9)
2026-05-08 | post-MVP3 | fix(V207): folders 테이블 컬럼명 오류 수정 — owner_id → owner_user_id (3b861ba)

## MVP3 M3-01 ~ M3-14 (보안·성능·UX 마감)

2026-05-08 | M3-14 | feat(ux): 업로드 버튼 중복 클릭 방지 + 경력 계산기 설명 문구 수정 (dd86e16)
2026-05-08 | M3-14 | feat: GlobalExceptionHandler — ResponseStatusException·제네릭 예외 처리 (90cd19a)
2026-05-08 | M3-14 | feat(perf): ThumbnailService @Async 비동기화 — 업로드 응답 지연 제거 (2d0e06e)
2026-05-08 | M3-14 | fix(security): 로그 보안 — 파일 절대 경로·비밀번호 재설정 코드 로그 제거 (e3264f4)
2026-05-08 | M3-14 | fix(security): 로그인 무한 리다이렉트 수정 + HTTP 보안 헤더 추가 (c7f3559)
2026-05-08 | M3-14 | chore(lint): 보안 린터 검사 13~15 추가 — HTTP 헤더·코드 로그·env 기본값 (e1ff807)
2026-05-08 | M3-14 | refactor: FileUtils.extension() 공통 유틸로 extension() 중복 제거 (362eb4a)
2026-05-08 | M3-14 | feat: S3/Cloudflare R2 스토리지 지원 + 환경변수 기본값 보안 강화 (5c4012d)
2026-05-08 | M3-14 | feat(mvp1-10): 배포 파일 + 운영 보안 체크리스트 추가 (ebb5655)
2026-05-08 | M3-14 | feat(mvp2-14): 문서 소프트 삭제 구현 — deletedAt/deletedBy 필드 (V206) (ef0db84)
2026-05-08 | M3-04 | feat(M3-04): dashboard 마이프로필에 연락처 행 추가 (0f2df0e)
2026-05-08 | M3-05 | fix(M3-05): EMPLOYMENT_CERTIFICATE @Deprecated 마킹 + V202 (9487689)
2026-05-08 | M3-07 | fix(M3-07): V207 SHARED_PUBLIC 마이그레이션 생성 + 검사 범위 확장 (554b1e2)
2026-05-08 | M3-02 | fix(M3-02): PasswordResetToken에 TOKEN_TTL 상수 추출 (a766c6a)
2026-05-07 | MVP3 | feat: MVP2 stages 08-14 + MVP3 stages M3-01~M3-13 구현 (3648b3f)
2026-05-07 | MVP3 | refactor(auth): 회원가입 관리자 승인 단계 제거 — 이메일 인증 후 즉시 활성화 (98c4bfc)

## MVP2 — TEAM_LEADER → SALES + 인력표 + 경력 계산기

2026-05-04 | MVP2 | feat: 이메일 발송 구현 — SmtpEmailSender 추가 (d3535ca)
2026-05-04 | MVP2-07 | feat: 경력 계산기 (124903d)
2026-05-04 | MVP2-06 | feat: 양식 이력서 기능 (1ab4eca)
2026-05-04 | MVP2-05 | feat: 인력 표 검색/필터/정렬/컬럼 토글 (c08ebf2)
2026-05-03 | MVP2-04 | feat: 영업부 인력 표 /sales/profiles + employee-documents (500de5e)
2026-05-03 | MVP2-03 | feat: 회원가입 프로필 필드 확장 + 비밀번호 복잡도 정책 (909b715)
2026-04-30 | MVP2-02 | feat: 다운로드 사유 제거 + 관리자 문서 삭제 기능 (7143886)
2026-04-30 | MVP2-01 | fix: Stage 01 완성도 보완 + 보안 린터 도입 — scripts/security-lint.sh (886e2a6)
2026-04-30 | MVP2-01 | feat: TEAM_LEADER → SALES 역할 재구조화 (V100) (243046f)

## MVP1 — 골격부터 13단계 + 배포 산출물

2026-04-29 | MVP1-13 | feat: tags + global document search — /search (9229e13)
2026-04-29 | MVP1-12 | feat: document expiry date management (defa6fc)
2026-04-29 | MVP1-deploy | feat: deployment artifacts + 13-stage test harness (ccd4a32)
2026-04-29 | MVP1-11 | feat: document approval/rejection email notifications (47bb6d4)
2026-04-29 | MVP1-10 | feat: download history + statistics (8a25c9d)
2026-04-29 | MVP1-09 | feat: document approval/review process (3a95b4c)
2026-04-29 | MVP1-08 | feat: thumbnail generation + card view (ae528cf)
2026-04-29 | MVP1-07 | feat: team leader permissions + individual folder access (3826f67)
2026-04-29 | MVP1-06 | feat: preview, download with reason, access control, audit VIEW/DOWNLOAD (38a60dd)
2026-04-28 | MVP1-05 | feat: file upload + FileStorage + versioning (8cfe409)
2026-04-28 | MVP1-04 | feat: admin approval flow + team CRUD + employee management + personal folder auto-creation (7b0e98c)
2026-04-28 | MVP1-03 | feat: signup + email verification + session login (1e0dec0)
2026-04-28 | MVP1-01·02 | feat: project skeleton + DB schema + JPA entities (21fc360)
