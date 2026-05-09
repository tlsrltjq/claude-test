-- MVP3 M3-05: DocumentType 정리
-- PROFILE_PHOTO 추가, EMPLOYMENT_CERTIFICATE deprecated 처리
-- Java enum 값은 코드에서 관리되므로 DB 수준 변경 사항 없음.
-- 기존 EMPLOYMENT_CERTIFICATE 레코드는 보존 (히스토리 유지).
-- 신규 업로드 화면에서는 isActive() = false 이므로 선택 옵션에서 제외됨.

-- 참고: 이 마이그레이션은 논리적 변경(코드 레벨)임을 문서화하는 마이그레이션입니다.
SELECT 1; -- no-op placeholder
