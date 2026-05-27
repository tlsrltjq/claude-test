package com.eactive.resourcehub.project.dto;

import com.eactive.resourcehub.project.entity.AssignmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectMemberRequest {

    private Long userId;       // 멤버 추가 시 필수, 수정 시 무시
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private AssignmentStatus status;

    public void validateForAdd() {
        if (userId == null)
            throw new IllegalArgumentException("대상 직원을 선택해야 합니다.");
        validateDates();
    }

    public void validateForUpdate() {
        validateDates();
    }

    private void validateDates() {
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("투입 기간을 입력해야 합니다.");
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
    }
}
