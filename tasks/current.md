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
2026-06-01: 만료 문서 필터링 완료.

완료 항목:
- fix: SalesProfileQueryService — findActiveWithVersionByFolderIds 결과에서 isExpired() 문서 제외,
  동일 타입 여러 개면 issuedDate 최신 우선(null이면 id 최신) 선택 (putIfAbsent → merge 교체)
- fix: SalesMemberService.getMemberAutofillData — GRADUATION_CERTIFICATE·LICENSE 필터에 !isExpired() 추가
- 빌드 BUILD SUCCESSFUL, 492개 테스트 전 통과

**다음 작업 없음 — 사용자 지시 대기**
