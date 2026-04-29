package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.DocumentReviewStatus;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocumentIdOrderByVersionNoDesc(Long documentId);

    Optional<DocumentVersion> findByDocumentIdAndVersionNo(Long documentId, Integer versionNo);

    int countByDocumentId(Long documentId);

    Optional<DocumentVersion> findFirstByDocumentIdOrderByVersionNoDesc(Long documentId);

    @Query("SELECT dv FROM DocumentVersion dv " +
           "JOIN FETCH dv.document d " +
           "JOIN FETCH d.folder f " +
           "JOIN FETCH f.owner " +
           "WHERE dv.id = :id")
    Optional<DocumentVersion> findByIdWithDocumentAndFolder(@Param("id") Long id);

    @Query("SELECT dv FROM DocumentVersion dv " +
           "JOIN FETCH dv.document d " +
           "JOIN FETCH d.folder f " +
           "JOIN FETCH f.owner u " +
           "LEFT JOIN FETCH u.team " +
           "WHERE dv.reviewStatus = :status " +
           "ORDER BY dv.createdAt ASC")
    List<DocumentVersion> findByReviewStatusWithDetails(@Param("status") DocumentReviewStatus status);

    @Query("SELECT dv FROM DocumentVersion dv " +
           "JOIN FETCH dv.document d " +
           "JOIN FETCH d.folder f " +
           "JOIN FETCH f.owner u " +
           "LEFT JOIN FETCH u.team " +
           "LEFT JOIN FETCH dv.reviewedBy " +
           "WHERE dv.id = :id")
    Optional<DocumentVersion> findByIdForReviewDetail(@Param("id") Long id);

    @Query("SELECT dv FROM DocumentVersion dv " +
           "JOIN FETCH dv.document d " +
           "JOIN FETCH d.folder f " +
           "JOIN FETCH f.owner u " +
           "LEFT JOIN FETCH u.team " +
           "WHERE dv.id IN :ids")
    List<DocumentVersion> findByIdInWithOwnerAndTeam(@Param("ids") List<Long> ids);
}
