ALTER TABLE documents
    ADD COLUMN expires_at DATE;

CREATE INDEX idx_documents_expires_at ON documents(expires_at)
    WHERE expires_at IS NOT NULL;
