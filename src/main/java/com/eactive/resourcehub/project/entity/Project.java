package com.eactive.resourcehub.project.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String clientName;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status;

    public static Project create(String name, String clientName,
                                 LocalDate startDate, LocalDate endDate,
                                 String memo) {
        Project p = new Project();
        p.name       = name;
        p.clientName = clientName;
        p.startDate  = startDate;
        p.endDate    = endDate;
        p.memo       = memo;
        p.status     = LocalDate.now().isBefore(startDate)
                       ? ProjectStatus.PLANNED : ProjectStatus.ACTIVE;
        return p;
    }

    public void update(String name, String clientName,
                       LocalDate startDate, LocalDate endDate,
                       String memo, ProjectStatus status) {
        this.name       = name;
        this.clientName = clientName;
        this.startDate  = startDate;
        this.endDate    = endDate;
        this.memo       = memo;
        this.status     = status;
    }

    public void cancel() {
        this.status = ProjectStatus.CANCELLED;
    }

    /** 특정 날짜에 이 프로젝트가 기간 내에 있는지. */
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
