package com.eactive.resourcehub.document.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Column(nullable = false, length = 255)
    private String title;

    // circular FK: documents.current_version_id → document_versions.id (DEFERRABLE INITIALLY DEFERRED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private DocumentVersion currentVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentStatus status;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "degree_type", length = 50)
    private String degreeType;

    @Column(name = "cert_type_meta", length = 50)
    private String certTypeMeta;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "files_purged_at")
    private LocalDateTime filesPurgedAt;

    @Column(name = "expiry_warn_sent_at")
    private LocalDateTime expiryWarnSentAt;

    @Column(name = "expired_notice_sent_at")
    private LocalDateTime expiredNoticeSentAt;

    public static Document create(Folder folder, DocumentType documentType, String title) {
        Document document = new Document();
        document.folder = folder;
        document.documentType = documentType;
        document.title = title;
        document.status = DocumentStatus.ACTIVE;
        return document;
    }

    public void setCurrentVersion(DocumentVersion version) {
        this.currentVersion = version;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void delete(Long deletedByUserId) {
        this.status = DocumentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUserId;
    }

    public void markFilesPurged() {
        if (this.filesPurgedAt == null) {
            this.filesPurgedAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(java.time.LocalDate.now());
    }

    public void updateExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
        // 만료일이 바뀌면 다시 알림 대상이 되도록 발송 이력 초기화
        this.expiryWarnSentAt = null;
        this.expiredNoticeSentAt = null;
    }

    public void markExpiryWarnSent() {
        this.expiryWarnSentAt = LocalDateTime.now();
    }

    public void markExpiredNoticeSent() {
        this.expiredNoticeSentAt = LocalDateTime.now();
    }

    public void updateIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }
    public void updateDegreeType(String degreeType)    { this.degreeType = degreeType; }
    public void updateCertTypeMeta(String certTypeMeta){ this.certTypeMeta = certTypeMeta; }

}
