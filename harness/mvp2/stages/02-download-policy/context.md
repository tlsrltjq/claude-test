# MVP2 Stage 02 — Context

## SSOT
- `mvp2/docs/PROJECT_SPEC_MVP2.md` §3 다운로드 정책, §4 관리자 파일 삭제
- `mvp2/docs/MIGRATION_FROM_MVP1.md` §4 다운로드 흐름

## 이전 단계 결과 (MVP2 01)
- ADMIN/SALES/EMPLOYEE 권한 단순화 완료
- DocumentAccessService / FolderAccessService에 SALES 분기 들어옴

## MVP1 영향
- MVP1 6단계 다운로드 흐름은 GET reason → POST download 였음
- audit_logs 에 DOWNLOAD 행이 reason과 함께 저장됨

## 이번 단계 핵심 제약
- audit_logs 의 DOWNLOAD 기록은 그대로 남음 (reason만 nullable로)
- 관리자 삭제는 hard delete (휴지통 안 만듦)
- 트랜잭션 안에서 디스크 + DB 함께 정리. 디스크 실패 시 DB 롤백.
- 정적 리소스 직노출 금지

## 코드가 들어갈 위치
- 마이그레이션: `V101__make_audit_reason_nullable.sql` (필요 시)
- 컨트롤러: `document/controller/DocumentDownloadController` (사유 GET 제거 + POST → GET)
- 컨트롤러: `document/controller/AdminDocumentController` 또는 신규 `AdminDocumentDeleteController`
- 서비스: `document/service/DocumentDeleteService`
- enum: `audit/entity/AuditActionType.DELETE_DOCUMENT`
- 템플릿: 다운로드 버튼이 있는 모든 화면에서 사유 입력 단계 제거
