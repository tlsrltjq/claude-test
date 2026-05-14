package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.DocumentReviewStatus;
import com.eactive.resourcehub.document.entity.DocumentStatus;
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
           "JOIN FETCH dv.uploadedBy " +
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

    // GC: 특정 문서들의 모든 버전 경로 조회 (파일 삭제용)
    @Query("SELECT dv FROM DocumentVersion dv WHERE dv.document.id IN :documentIds")
    List<DocumentVersion> findByDocumentIdIn(@Param("documentIds") List<Long> documentIds);

    // 고아 파일 GC: 스토리지에 알려진 모든 경로 수집
    @Query("SELECT v.storagePath FROM DocumentVersion v WHERE v.storagePath IS NOT NULL")
    List<String> findAllStoragePaths();

    @Query("SELECT v.previewStoragePath FROM DocumentVersion v WHERE v.previewStoragePath IS NOT NULL")
    List<String> findAllPreviewPaths();

    @Query("SELECT v.thumbnailStoragePath FROM DocumentVersion v WHERE v.thumbnailStoragePath IS NOT NULL")
    List<String> findAllThumbnailPaths();

    // 중복 파일 탐지: 동일 폴더 내 같은 체크섬이 존재하는지 확인
    @Query("SELECT dv FROM DocumentVersion dv " +
           "JOIN FETCH dv.document d " +
           "WHERE dv.checksum = :checksum " +
           "AND d.folder.id = :folderId " +
           "AND d.status <> :excludedStatus")
    Optional<DocumentVersion> findFirstByChecksumInFolder(
            @Param("checksum") String checksum,
            @Param("folderId") Long folderId,
            @Param("excludedStatus") DocumentStatus excludedStatus);
}
