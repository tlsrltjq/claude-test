ALTER TABLE document_versions
    ADD COLUMN review_status  VARCHAR(50)  NOT NULL DEFAULT 'PENDING_REVIEW',
    ADD COLUMN reviewed_by    BIGINT,
    ADD COLUMN reviewed_at    TIMESTAMP,
    ADD COLUMN reject_reason  VARCHAR(500);

ALTER TABLE document_versions
    ADD CONSTRAINT fk_document_versions_reviewed_by
        FOREIGN KEY (reviewed_by) REFERENCES users(id);

CREATE INDEX idx_document_versions_review_status ON document_versions(review_status);
