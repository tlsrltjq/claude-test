package com.eactive.resourcehub.employee.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import com.eactive.resourcehub.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String jobTitle;

    @Column(columnDefinition = "TEXT")
    private String careerSummary;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(length = 20)
    private String developerGrade;

    @Column(nullable = false)
    private int careerMonths;

    @Column(nullable = false)
    private int careerTotalDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AvailableStatus availableStatus;

    public static EmployeeProfile create(User user) {
        EmployeeProfile profile = new EmployeeProfile();
        profile.user = user;
        profile.availableStatus = AvailableStatus.AVAILABLE;
        return profile;
    }

    public void update(String jobTitle, String careerSummary, String skills) {
        this.jobTitle = jobTitle;
        this.careerSummary = careerSummary;
        this.skills = skills;
    }

    public void updateCareer(String developerGrade, int careerMonths, int careerTotalDays) {
        this.developerGrade = developerGrade;
        this.careerMonths = careerMonths;
        this.careerTotalDays = careerTotalDays;
    }

    public void changeAvailableStatus(AvailableStatus status) {
        this.availableStatus = status;
    }
}
