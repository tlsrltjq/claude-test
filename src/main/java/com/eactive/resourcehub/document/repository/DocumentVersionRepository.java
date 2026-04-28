package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocumentIdOrderByVersionNoDesc(Long documentId);

    Optional<DocumentVersion> findByDocumentIdAndVersionNo(Long documentId, Integer versionNo);

    int countByDocumentId(Long documentId);
}
