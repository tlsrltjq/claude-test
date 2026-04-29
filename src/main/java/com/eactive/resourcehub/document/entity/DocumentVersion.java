package com.eactive.resourcehub.document.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
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

    @Column(length = 255)
    private String thumbnailFileName;

    @Column(length = 500)
    private String thumbnailStoragePath;

    @Column(length = 100)
    private String thumbnailContentType;

    private LocalDateTime thumbnailGeneratedAt;

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

    public void clearThumbnail() {
        this.thumbnailFileName = null;
        this.thumbnailStoragePath = null;
        this.thumbnailContentType = null;
        this.thumbnailGeneratedAt = null;
    }
}
