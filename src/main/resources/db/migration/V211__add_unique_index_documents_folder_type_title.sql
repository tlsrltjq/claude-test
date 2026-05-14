-- 동일 폴더에서 같은 (document_type, title) 문서의 동시 중복 생성 방지
-- DELETED 상태는 제외하여 삭제 후 동일 이름으로 재업로드가 가능하도록 한다.
CREATE UNIQUE INDEX IF NOT EXISTS uk_documents_folder_type_title_active
    ON documents (folder_id, document_type, title)
    WHERE status <> 'DELETED';
