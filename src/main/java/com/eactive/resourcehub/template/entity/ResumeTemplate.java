package com.eactive.resourcehub.template.entity;

import com.eactive.resourcehub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resume_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(length = 128)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResumeTemplateStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ResumeTemplate create(String fileName, String storedFileName, String storagePath,
                                        String contentType, long fileSize, String checksum,
                                        User uploadedBy) {
        ResumeTemplate t = new ResumeTemplate();
        t.fileName = fileName;
        t.storedFileName = storedFileName;
        t.storagePath = storagePath;
        t.contentType = contentType;
        t.fileSize = fileSize;
        t.checksum = checksum;
        t.status = ResumeTemplateStatus.ACTIVE;
        t.uploadedBy = uploadedBy;
        return t;
    }

    public void archive() {
        this.status = ResumeTemplateStatus.ARCHIVED;
    }
}
