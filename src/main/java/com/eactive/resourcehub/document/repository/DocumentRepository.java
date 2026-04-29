package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query("SELECT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner " +
           "LEFT JOIN FETCH d.currentVersion " +
           "WHERE d.id = :id")
    Optional<Document> findByIdForDetail(@Param("id") Long id);

    @Query("SELECT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner u " +
           "LEFT JOIN FETCH u.team " +
           "WHERE d.status = 'ACTIVE' AND d.expiresAt IS NOT NULL AND d.expiresAt < :today " +
           "ORDER BY d.expiresAt ASC")
    List<Document> findExpired(@Param("today") LocalDate today);

    @Query("SELECT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner u " +
           "LEFT JOIN FETCH u.team " +
           "WHERE d.status = 'ACTIVE' AND d.expiresAt IS NOT NULL " +
           "AND d.expiresAt >= :today AND d.expiresAt <= :threshold " +
           "ORDER BY d.expiresAt ASC")
    List<Document> findExpiringSoon(@Param("today") LocalDate today,
                                    @Param("threshold") LocalDate threshold);

    // 본인 문서 검색 — 태그 FETCH, 태그 필터는 EXISTS 서브쿼리
    @Query("SELECT DISTINCT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner " +
           "LEFT JOIN FETCH d.tags " +
           "WHERE f.owner.id = :ownerId AND d.status = 'ACTIVE' " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE :keyword) " +
           "AND (:typeFilter IS NULL OR d.documentType = :typeFilter) " +
           "AND (:tagName IS NULL OR EXISTS (SELECT t FROM d.tags t WHERE LOWER(t.name) = :tagName)) " +
           "ORDER BY d.createdAt DESC")
    List<Document> searchOwn(@Param("ownerId") Long ownerId,
                             @Param("keyword") String keyword,
                             @Param("typeFilter") DocumentType typeFilter,
                             @Param("tagName") String tagName);

    // 공유 폴더 문서 검색
    @Query("SELECT DISTINCT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner " +
           "LEFT JOIN FETCH d.tags " +
           "WHERE f.id IN :folderIds AND d.status = 'ACTIVE' AND d.currentVersion IS NOT NULL " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE :keyword) " +
           "AND (:typeFilter IS NULL OR d.documentType = :typeFilter) " +
           "AND (:tagName IS NULL OR EXISTS (SELECT t FROM d.tags t WHERE LOWER(t.name) = :tagName)) " +
           "ORDER BY d.createdAt DESC")
    List<Document> searchInFolders(@Param("folderIds") List<Long> folderIds,
                                   @Param("keyword") String keyword,
                                   @Param("typeFilter") DocumentType typeFilter,
                                   @Param("tagName") String tagName);

    // 관리자: 전체 문서 검색
    @Query("SELECT DISTINCT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner u " +
           "LEFT JOIN FETCH u.team " +
           "LEFT JOIN FETCH d.tags " +
           "WHERE d.status = 'ACTIVE' " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE :keyword) " +
           "AND (:typeFilter IS NULL OR d.documentType = :typeFilter) " +
           "AND (:tagName IS NULL OR EXISTS (SELECT t FROM d.tags t WHERE LOWER(t.name) = :tagName)) " +
           "ORDER BY d.createdAt DESC")
    List<Document> searchAll(@Param("keyword") String keyword,
                             @Param("typeFilter") DocumentType typeFilter,
                             @Param("tagName") String tagName);

    // 본인 문서 태그 포함 조회 (detail용)
    @Query("SELECT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner " +
           "LEFT JOIN FETCH d.currentVersion " +
           "LEFT JOIN FETCH d.tags " +
           "WHERE d.id = :id")
    Optional<Document> findByIdForDetailWithTags(@Param("id") Long id);
}
