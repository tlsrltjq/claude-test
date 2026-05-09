# MVP3 M3-11 — Context

이전: build.gradle 에 poi-ooxml 5.3.0 이미 들어가있음. M3-10 프리셋이 잡혀있음.

핵심: SXSSFWorkbook 으로 메모리 폭발 회피. 파일은 InputStreamResource 로 즉시 응답.

위치:
- `sales/controller/SalesProfileExportController` — POST /sales/profiles/export
- `sales/service/SalesProfileExporter`
- `audit/entity/AuditActionType.EXPORT_PROFILES`
- `templates/sales/profiles.html` 행 체크박스 + 다운로드 버튼
