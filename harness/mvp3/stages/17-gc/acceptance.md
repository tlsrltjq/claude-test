# M3-17 합격 기준 (Acceptance Criteria)

## 필수 (FAIL 없음)

- [ ] V210 마이그레이션(`files_purged_at TIMESTAMP` 컬럼) 존재
- [ ] `Document.java`에 `filesPurgedAt` 필드 및 `markFilesPurged()` 메서드 존재
- [ ] `DocumentRepository.findPurgeCandidates(LocalDateTime threshold)` 쿼리 존재
- [ ] `DocumentVersionRepository.findByDocumentIdIn(List<Long>)` 쿼리 존재
- [ ] `DocumentFileGcService.java` 존재
- [ ] `@Scheduled(cron = "0 0 2 * * *")` 새벽 2시 자동 실행 설정
- [ ] `AdminController`에 `POST /admin/gc/run` 엔드포인트 + `DocumentFileGcService` 주입
- [ ] `templates/admin/gc.html` 존재
- [ ] `application.yml`에 `gc.retention-days` 설정 존재

## DB 런타임 (연결 가능 시)

- [ ] `documents.files_purged_at` 컬럼 실제 존재
- [ ] `idx_documents_gc_candidates` 부분 인덱스 존재

## 동작 검증

- [ ] `/admin/gc` 페이지 정상 로드 (보존 기간 표시)
- [ ] "지금 GC 실행" 버튼 클릭 시 결과 메시지 표시
- [ ] GC 실행 후 `files_purged_at`이 NULL이 아닌 값으로 업데이트됨
- [ ] GC 대상 없으면 "정리 대상 없음" 메시지 표시

## 허용 기준 (WARN)

- GC 처리 대기 건수 > 0: WARN (정보성, FAIL 아님)
- DB 연결 불가: 정적 분석 PASS 시 전체 PASS 처리 가능
