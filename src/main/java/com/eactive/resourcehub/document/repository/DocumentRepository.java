package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByFolderId(Long folderId);

    List<Document> findByFolderIdAndStatus(Long folderId, DocumentStatus status);

    List<Document> findByFolderIdAndDocumentType(Long folderId, DocumentType documentType);

    Optional<Document> findByFolderIdAndDocumentTypeAndTitle(
            Long folderId, DocumentType documentType, String title);

    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.currentVersion WHERE d.folder.id = :folderId AND d.status = :status")
    List<Document> findByFolderIdAndStatusWithVersion(
            @Param("folderId") Long folderId, @Param("status") DocumentStatus status);
}
