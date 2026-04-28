package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByFolderId(Long folderId);

    List<Document> findByFolderIdAndStatus(Long folderId, DocumentStatus status);

    List<Document> findByFolderIdAndDocumentType(Long folderId, DocumentType documentType);
}
