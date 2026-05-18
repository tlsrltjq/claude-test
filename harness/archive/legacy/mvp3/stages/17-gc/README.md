# M3-17: 파일 GC (Garbage Collection)

## 목적

소프트 삭제(`status = 'DELETED'`)된 문서의 물리 파일을 주기적으로 정리하는 가비지 컬렉션 기능을 구현하고 검증한다.

## 배경

문서 삭제 시 즉시 물리 파일을 제거하지 않고 소프트 삭제만 수행한다.
이후 일정 보존 기간이 지난 문서의 물리 파일을 별도 GC 프로세스가 정리한다.
이 방식으로 실수로 삭제된 문서를 보존 기간 내에 복구할 수 있다.

## GC 동작 조건

```
status = 'DELETED'
AND deleted_at < NOW() - INTERVAL '${retention-days} days'
AND files_purged_at IS NULL
```

- 기본 보존 기간: **7일**
- 환경변수 `RESOURCEHUB_GC_RETENTION_DAYS`로 조정 가능

## 자동 실행

`@Scheduled(cron = "0 0 2 * * *")` — 매일 새벽 02:00 자동 실행

## 수동 실행

관리자 → `/admin/gc` → "지금 GC 실행" 버튼

## 구현 파일

| 파일 | 역할 |
|------|------|
| `V210__add_files_purged_at_to_documents.sql` | `files_purged_at` 컬럼 + 부분 인덱스 추가 |
| `Document.java` | `filesPurgedAt` 필드 + `markFilesPurged()` |
| `DocumentRepository.java` | `findPurgeCandidates(threshold)` |
| `DocumentVersionRepository.java` | `findByDocumentIdIn(documentIds)` |
| `DocumentFileGcService.java` | GC 핵심 로직 + 스케줄러 |
| `AdminController.java` | `GET /admin/gc`, `POST /admin/gc/run` |
| `templates/admin/gc.html` | GC 대시보드 |

## 검증 실행

```bash
bash harness/mvp3/stages/17-gc/verify.sh
```
