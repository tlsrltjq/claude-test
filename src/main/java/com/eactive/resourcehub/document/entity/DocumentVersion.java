package com.eactive.resourcehub.document.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import com.eactive.resourcehub.common.util.FileUtils;
import com.eactive.resourcehub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_versions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private Integer versionNo;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 255)
    private String storedFileName;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(length = 255)
    private String previewFileName;

    @Column(length = 500)
    private String previewStoragePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 100)
    private String contentType;

    @Column(length = 64)
    private String checksum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    public static DocumentVersion create(Document document, int versionNo,
                                          String originalFileName, String storedFileName,
                                          String storagePath, Long fileSize,
                                          String contentType, String checksum,
                                          User uploadedBy) {
        DocumentVersion version = new DocumentVersion();
        version.document = document;
        version.versionNo = versionNo;
        version.originalFileName = originalFileName;
        version.storedFileName = storedFileName;
        version.storagePath = storagePath;
        version.fileSize = fileSize;
        version.contentType = contentType;
        version.checksum = checksum;
        version.uploadedBy = uploadedBy;
        return version;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentReviewStatus reviewStatus = DocumentReviewStatus.PENDING_REVIEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private com.eactive.resourcehub.user.entity.User reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 500)
    private String rejectReason;

    @Column(length = 255)
    private String thumbnailFileName;

    @Column(length = 500)
    private String thumbnailStoragePath;

    @Column(length = 100)
    private String thumbnailContentType;

    private LocalDateTime thumbnailGeneratedAt;

    /**
     * 미리보기 가능 여부 — 템플릿에서 th:if="${version.previewSupported}"로 사용.
     * hwp·zip 등은 false 반환해 미리보기 버튼을 숨긴다.
     */
    public boolean isPreviewSupported() {
        String ext = FileUtils.extension(originalFileName);
        return switch (ext) {
            case "pdf", "jpg", "jpeg", "png" -> true;
            case "docx", "hwpx", "pptx", "ppt", "xlsx", "xls" -> previewStoragePath != null;
            default -> false;
        };
    }

    public void setPreview(String previewFileName, String previewStoragePath) {
        this.previewFileName = previewFileName;
        this.previewStoragePath = previewStoragePath;
    }

    public void setThumbnail(String thumbnailFileName, String thumbnailStoragePath,
                             String thumbnailContentType) {
        this.thumbnailFileName = thumbnailFileName;
        this.thumbnailStoragePath = thumbnailStoragePath;
        this.thumbnailContentType = thumbnailContentType;
        this.thumbnailGeneratedAt = LocalDateTime.now();
    }

    public void autoApprove() {
        this.reviewStatus = DocumentReviewStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void approve(com.eactive.resourcehub.user.entity.User reviewer) {
        this.reviewStatus = DocumentReviewStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(com.eactive.resourcehub.user.entity.User reviewer, String reason) {
        this.reviewStatus = DocumentReviewStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.rejectReason = reason;
    }

    public void clearThumbnail() {
        this.thumbnailFileName = null;
        this.thumbnailStoragePath = null;
        this.thumbnailContentType = null;
        this.thumbnailGeneratedAt = null;
    }
}
