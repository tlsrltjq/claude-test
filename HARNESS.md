# eActive Resource Hub — 작업 가이드

## 프로젝트 목적

이액티브 사내 직원·문서·인력 관리 포털. 회원가입·문서 업로드/검토·공용 폴더·영업 인력표·프로젝트 투입 관리·경력 계산기·재직증명서 자동 발급을 한 곳에서 처리.

---

## 현재 구현 범위 (V229 기준)

| 도메인 | 주요 기능 |
|--------|----------|
| 인증 | 회원가입(이메일 인증), 로그인(세션), 비밀번호 찾기, 설정 |
| 가입 제어 | `allowed_emails` 사전등록 방식 (`/admin/allowed-emails`) |
| 내 폴더 | 개인 문서 CRUD, magic bytes 이중 검증, 썸네일 비동기 생성 |
| 공용 폴더 | SHARED_PUBLIC — 전 사원 업로드, 업로더·ADMIN만 삭제 |
| 문서 검토 | PENDING_REVIEW → APPROVED/REJECTED, 파일 GC(cron 02:00) |
| 영업 | 인력표(필터·컬럼·프리셋·투입정보), 경력 계산기, 엑셀/번들 다운로드 |
| 투입 관리 | 캘린더(`/sales/calendar`), 프로젝트 CRUD·멤버 관리, 배정 삭제, 대시보드 통계, 프로젝트 바(bar) 렌더링, 하단 프로젝트 리스트 |
| 관리자 | 직원·팀·팀 프로젝트 설정·문서 검토·만료 문서·통계·재직증명서·파일 GC·이메일 허용 목록 |

기술 스택: Java 21 / Spring Boot 3.5 / Gradle / PostgreSQL 18 + Flyway V1~V229 / Thymeleaf + Bootstrap 5.3.3 / Spring Security 세션. 운영: Caddy(HTTPS) + Docker Compose.

**현재 상태 (2026-06-02):** Flyway V229. BUILD SUCCESSFUL. security-lint 21/21 PASS. 531개 테스트 전 통과. 최근 완료: 하네스 정비 — ADR 번호 동기화(ADR-044), data-model.md Flyway 이력 섹션 제거(377→332줄), CALENDAR_REDESIGN.md archive 이동. 다음 예정: 고정 도메인 구매·적용, NAS 저장소 연동.

상세: `docs/architecture.md` (패키지·라우트), `docs/spec.md` (기능 SSOT), `docs/decisions.md` (ADR-001~044).

---

## 작업 원칙

1. **계획 먼저** — 파일 목록 확인 후 수정. 불확실한 부분은 가정하지 말고 질문
2. **최소 변경** — 새 기능 추가, UI 재설계, 대규모 리팩터링 금지
3. **테스트 유지** — 변경 후 `./gradlew build` + `bash scripts/security-lint.sh` 필수
4. **문서 동기화** — 코드 변경 시 관련 문서도 같이 수정
5. **완료 기준 달성 시 멈추기** — 지시 범위를 벗어난 추가 구현 금지

---

## 절대 금지 사항

| 대상 | 이유 |
|------|------|
| `src/main/resources/db/migration/V*.sql` 기존 번호 수정 | 운영 DB 파괴 위험 |
| `SecurityConfig.java` 수정 | 전체 보안 정책 변경 위험 |
| `.env`, `.env.example` | 시크릿 노출 |
| `Caddyfile`, `docker-compose.prod.yml`, `application-prod.yml` | 운영 인프라 |
| `harness/archive/legacy/**` | 옛 하네스 보존 (읽기 전용) |
| JWT 도입, Remember-Me 활성화, CSRF 비활성화 | ADR-001~003 |
| 컨트롤러에서 Repository 직접 주입 | ADR-006, ADR-022 |
| 컨트롤러에서 `role` 직접 비교 | ADR-006 |
| `ddl-auto: create/create-drop` | ADR-007 |

---

## 기능 추가 절차

```
1. 관련 파일 목록 파악 (Controller·Service·Repository·Template·Migration)
2. Flyway 마이그레이션 작성 (V230부터, 기존 번호 수정 금지)
3. JPA 엔티티 → Service → Controller 순으로 구현
4. 테스트 작성 (순수 로직은 단위 테스트, Service는 Mockito, Controller는 @WebMvcTest)
5. 보안 정적 분석: bash scripts/security-lint.sh (0 FAIL 유지)
6. 전체 빌드: ./gradlew build (BUILD SUCCESSFUL 유지)
7. 관련 문서 업데이트 (spec.md, architecture.md, data-model.md, frontend.md)
8. 결과 보고 (아래 형식 참조)
```

---

## 문서 수정 기준

- 실제 구현된 코드와 일치해야 함
- 미구현 기능은 "향후 계획"으로 분리
- API 경로·DTO 필드명·화면 경로는 소스 기준
- Flyway 버전: 현재 V229. 다음은 **V230**부터
- ADR 번호: 현재 ADR-044까지. 다음은 ADR-045부터

---

## 테스트 기준

| 구분 | 도구 | 기준 |
|------|------|------|
| 단위 테스트 | JUnit 5 + Mockito | 순수 로직, 외부 의존 없음 |
| 서비스 통합 | Mockito + @ExtendWith | Repository·외부 서비스 Mock |
| 컨트롤러 슬라이스 | @WebMvcTest + MockMvc | HTTP 상태·CSRF·리다이렉트 |
| 보안 정적 분석 | scripts/security-lint.sh | 21개 항목, 0 FAIL 유지 |
| 전체 빌드 | ./gradlew build | BUILD SUCCESSFUL 유지 |

- 테스트가 깨지면 무시하지 말고 원인 분석
- 실패하는 테스트 커밋 금지

---

## 권한·보안 주의사항

→ `docs/SECURITY_AND_PERMISSION.md` 참조

---

## AI 작업 결과 보고 형식

작업 완료 후 다음 항목을 보고:

1. 수정한 파일 목록 (경로 + 한 줄 요약)
2. 주요 변경 내용
3. 삭제한 코드/파일
4. 검토 필요로 남긴 항목
5. 실행한 검증 명령어 + 결과
6. 남은 문제점 또는 다음 단계

---

## 참고 파일

| 파일 | 내용 |
|------|------|
| `docs/architecture.md` | 패키지 구조·라우트 맵·DB 스키마·운영 인프라 |
| `docs/spec.md` | 기능 SSOT (전체 API·규칙) |
| `docs/decisions.md` | ADR — 기술 결정 근거 |
| `docs/data-model.md` | 테이블 상세·ERD·Flyway 이력 |
| `docs/testing.md` | 테스트 전략·테스트 파일 목록 |
| `docs/SECURITY_AND_PERMISSION.md` | 역할·접근 규칙·보안 코딩 규칙 |
| `tasks/current.md` | 현재 단계 작업 컨텍스트 |
| `CHANGELOG.md` | 변경 이력 한 줄 누적 |
