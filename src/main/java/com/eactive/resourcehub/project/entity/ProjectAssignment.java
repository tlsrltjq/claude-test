package com.eactive.resourcehub.project.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import com.eactive.resourcehub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "project_assignments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(length = 100)
    private String role;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status;

    public static ProjectAssignment createForProject(Project project, User user,
                                                      String role,
                                                      LocalDate startDate, LocalDate endDate) {
        ProjectAssignment pa = new ProjectAssignment();
        pa.project   = project;
        pa.user      = user;
        pa.role      = role;
        pa.startDate = startDate;
        pa.endDate   = endDate;
        pa.status    = LocalDate.now().isBefore(startDate)
                       ? AssignmentStatus.PLANNED : AssignmentStatus.ACTIVE;
        return pa;
    }

    /** 멤버 개인 기간·역할 수정 (ProjectService 전용). */
    public void updateMember(String role, LocalDate startDate, LocalDate endDate,
                              AssignmentStatus status) {
        this.role      = role;
        this.startDate = startDate;
        this.endDate   = endDate;
        if (status != null) this.status = status;
    }

    public void cancel() {
        this.status = AssignmentStatus.CANCELLED;
    }

    /** 오늘 기준 잔여 일수. 종료됐으면 0. */
    public long remainingDays() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) return 0;
        return ChronoUnit.DAYS.between(today, endDate);
    }

    /** 특정 날짜에 이 배정이 기간 내에 있는지 (status 무관). */
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
