-- 건강보험료납부확인서(HEALTH_INSURANCE_PROOF) 데이터 완전 삭제
-- 순서: current_version_id 해제 → document_versions 삭제 → documents 삭제
-- (tags는 ON DELETE CASCADE로 자동 삭제, 물리 파일은 /admin/gc 로 정리)

UPDATE documents
   SET current_version_id = NULL
 WHERE document_type = 'HEALTH_INSURANCE_PROOF';

DELETE FROM document_versions
 WHERE document_id IN (
     SELECT id FROM documents WHERE document_type = 'HEALTH_INSURANCE_PROOF'
 );

DELETE FROM documents
 WHERE document_type = 'HEALTH_INSURANCE_PROOF';
