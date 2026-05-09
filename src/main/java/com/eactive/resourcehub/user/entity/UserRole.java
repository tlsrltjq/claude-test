package com.eactive.resourcehub.user.entity;

public enum UserRole {
    ADMIN,
    SALES,
    EMPLOYEE,

    /**
     * @deprecated MVP2부터 SALES로 대체됨. 신규 부여 불가. DB 호환을 위해 enum 값은 유지.
     */
    @Deprecated
    TEAM_LEADER;

    public String getDisplayName() {
        return switch (this) {
            case ADMIN -> "관리자";
            case SALES -> "영업";
            case EMPLOYEE -> "사원";
            case TEAM_LEADER -> "팀장(사용중지)";
        };
    }
}
