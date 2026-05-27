package com.eactive.resourcehub.project.entity;

public enum AssignmentStatus {
    PLANNED("투입 예정"),
    ACTIVE("투입 중"),
    ENDED("종료"),
    CANCELLED("취소");

    private final String displayName;

    AssignmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
