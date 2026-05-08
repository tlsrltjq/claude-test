package com.eactive.resourcehub.document.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "document_tags",
               joinColumns = @JoinColumn(name = "document_id"),
               inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

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

    public void moveToTrash() {
        this.status = DocumentStatus.IN_TRASH;
    }

    public void restore() {
        this.status = DocumentStatus.ACTIVE;
    }

    public void delete(Long deletedByUserId) {
        this.status = DocumentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUserId;
    }

    public void updateExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }
}
