package com.eactive.resourcehub.user.entity;

public enum UserRole {
    ADMIN,
    SALES,
    EMPLOYEE;

    public String getDisplayName() {
        return switch (this) {
            case ADMIN -> "관리자";
            case SALES -> "영업";
            case EMPLOYEE -> "사원";
        };
    }
}
