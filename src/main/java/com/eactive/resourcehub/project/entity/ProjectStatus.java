package com.eactive.resourcehub.project.entity;

public enum ProjectStatus {
    PLANNED("진행 예정"),
    ACTIVE("진행 중"),
    ENDED("종료"),
    CANCELLED("취소");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
