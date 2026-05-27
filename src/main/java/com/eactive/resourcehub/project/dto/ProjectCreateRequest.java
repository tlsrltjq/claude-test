package com.eactive.resourcehub.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ProjectCreateRequest {

    private String name;
    private String clientName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String memo;
    private List<Long> selectedUserIds;

    public void validate() {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("프로젝트명은 필수입니다.");
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("프로젝트 기간을 입력해야 합니다.");
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
    }
}
