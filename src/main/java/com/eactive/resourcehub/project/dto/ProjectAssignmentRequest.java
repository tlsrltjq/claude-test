package com.eactive.resourcehub.project.dto;

import com.eactive.resourcehub.project.entity.AssignmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectAssignmentRequest {

    private Long userId;
    private String projectName;
    private String clientName;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private AssignmentStatus status;
    private String memo;

    public void validate() {
        if (userId == null)
            throw new IllegalArgumentException("대상 직원을 선택해야 합니다.");
        if (projectName == null || projectName.isBlank())
            throw new IllegalArgumentException("프로젝트명은 필수입니다.");
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("투입 기간을 입력해야 합니다.");
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
    }
}
