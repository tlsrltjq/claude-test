ALTER TABLE document_versions
    ADD COLUMN thumbnail_file_name      VARCHAR(255),
    ADD COLUMN thumbnail_storage_path   VARCHAR(500),
    ADD COLUMN thumbnail_content_type   VARCHAR(100),
    ADD COLUMN thumbnail_generated_at   TIMESTAMP;
