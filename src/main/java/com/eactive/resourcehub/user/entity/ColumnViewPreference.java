package com.eactive.resourcehub.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "column_view_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ColumnViewPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "columns_json", nullable = false, columnDefinition = "TEXT")
    private String columnsJson;

    @Column(name = "sort_json", columnDefinition = "TEXT")
    private String sortJson;

    @Column(name = "career_display", nullable = false, length = 8)
    private String careerDisplay = "ymd";

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public static ColumnViewPreference create(User user, String name,
                                               String columnsJson, String sortJson,
                                               String careerDisplay, boolean isDefault) {
        ColumnViewPreference p = new ColumnViewPreference();
        p.user = user;
        p.name = name;
        p.columnsJson = columnsJson;
        p.sortJson = sortJson != null ? sortJson : "{}";
        p.careerDisplay = careerDisplay != null ? careerDisplay : "ymd";
        p.isDefault = isDefault;
        return p;
    }

    public void update(String columnsJson, String sortJson, String careerDisplay) {
        this.columnsJson = columnsJson;
        this.sortJson = sortJson != null ? sortJson : "{}";
        this.careerDisplay = careerDisplay != null ? careerDisplay : "ymd";
        this.updatedAt = OffsetDateTime.now();
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
