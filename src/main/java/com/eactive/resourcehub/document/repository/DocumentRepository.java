package com.eactive.resourcehub.document.repository;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByFolderId(Long folderId);

    List<Document> findByFolderIdAndStatus(Long folderId, DocumentStatus status);

    List<Document> findByFolderIdAndDocumentType(Long folderId, DocumentType documentType);

    Optional<Document> findByFolderIdAndDocumentTypeAndTitle(
            Long folderId, DocumentType documentType, String title);

    @Query("SELECT d FROM Document d LEFT JOIN FETCH d.currentVersion v LEFT JOIN FETCH v.uploadedBy WHERE d.folder.id = :folderId AND d.status = :status")
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

    // 본인 개인 폴더 문서 검색 — 제목·파일명 키워드, 종류·업로더·날짜 필터 (DB 처리)
    // from/to: null 불가 — 호출부에서 sentinel(LocalDateTime.MIN/MAX) 사용
    @Query("SELECT DISTINCT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner " +
           "LEFT JOIN FETCH d.currentVersion cv " +
           "LEFT JOIN FETCH cv.uploadedBy uploader " +
           "WHERE f.owner.id = :ownerId AND d.status = 'ACTIVE' " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE :keyword " +
           "     OR (cv IS NOT NULL AND LOWER(cv.originalFileName) LIKE :keyword)) " +
           "AND (:typeFilter IS NULL OR d.documentType = :typeFilter) " +
           "AND (:uploaderKw IS NULL OR (cv IS NOT NULL AND LOWER(uploader.name) LIKE :uploaderKw)) " +
           "AND d.createdAt >= :from " +
           "AND d.createdAt <= :to " +
           "ORDER BY d.createdAt DESC")
    List<Document> searchOwn(@Param("ownerId") Long ownerId,
                             @Param("keyword") String keyword,
                             @Param("typeFilter") DocumentType typeFilter,
                             @Param("uploaderKw") String uploaderKw,
                             @Param("from") java.time.LocalDateTime from,
                             @Param("to") java.time.LocalDateTime to);

    // 특정 폴더 목록 문서 검색 (공유·공용 폴더)
    // from/to: null 불가 — 호출부에서 sentinel(LocalDateTime.MIN/MAX) 사용
    @Query("SELECT DISTINCT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner " +
           "LEFT JOIN FETCH d.currentVersion cv " +
           "LEFT JOIN FETCH cv.uploadedBy uploader " +
           "WHERE f.id IN :folderIds AND d.status = 'ACTIVE' " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE :keyword " +
           "     OR (cv IS NOT NULL AND LOWER(cv.originalFileName) LIKE :keyword)) " +
           "AND (:typeFilter IS NULL OR d.documentType = :typeFilter) " +
           "AND (:uploaderKw IS NULL OR (cv IS NOT NULL AND LOWER(uploader.name) LIKE :uploaderKw)) " +
           "AND d.createdAt >= :from " +
           "AND d.createdAt <= :to " +
           "ORDER BY d.createdAt DESC")
    List<Document> searchInFolders(@Param("folderIds") List<Long> folderIds,
                                   @Param("keyword") String keyword,
                                   @Param("typeFilter") DocumentType typeFilter,
                                   @Param("uploaderKw") String uploaderKw,
                                   @Param("from") java.time.LocalDateTime from,
                                   @Param("to") java.time.LocalDateTime to);

    // 관리자: 전체 문서 검색
    // from/to: null 불가 — 호출부에서 sentinel(LocalDateTime.MIN/MAX) 사용
    @Query("SELECT DISTINCT d FROM Document d " +
           "JOIN FETCH d.folder f JOIN FETCH f.owner u " +
           "LEFT JOIN FETCH u.team " +
           "LEFT JOIN FETCH d.currentVersion cv " +
           "LEFT JOIN FETCH cv.uploadedBy uploader " +
           "WHERE d.status = 'ACTIVE' " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE :keyword " +
           "     OR (cv IS NOT NULL AND LOWER(cv.originalFileName) LIKE :keyword)) " +
           "AND (:typeFilter IS NULL OR d.documentType = :typeFilter) " +
           "AND (:uploaderKw IS NULL OR (cv IS NOT NULL AND LOWER(uploader.name) LIKE :uploaderKw)) " +
           "AND d.createdAt >= :from " +
           "AND d.createdAt <= :to " +
           "ORDER BY d.createdAt DESC")
    List<Document> searchAll(@Param("keyword") String keyword,
                             @Param("typeFilter") DocumentType typeFilter,
                             @Param("uploaderKw") String uploaderKw,
                             @Param("from") java.time.LocalDateTime from,
                             @Param("to") java.time.LocalDateTime to);

    // 프로필 표용: folder_ids 배치 조회, ACTIVE 문서 + currentVersion (reviewStatus 무관)
    @Query("SELECT d FROM Document d JOIN FETCH d.currentVersion " +
           "WHERE d.folder.id IN :folderIds AND d.status = 'ACTIVE'")
    List<Document> findActiveWithVersionByFolderIds(@Param("folderIds") List<Long> folderIds);

    // GC: DELETED + 보존 기간 경과 + 파일 미정리 문서 조회
    @Query("SELECT d FROM Document d WHERE d.status = 'DELETED' " +
           "AND d.deletedAt < :threshold AND d.filesPurgedAt IS NULL")
    List<Document> findPurgeCandidates(@Param("threshold") LocalDateTime threshold);

}
