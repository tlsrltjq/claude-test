package com.eactive.resourcehub.user.entity;

public enum UserRole {
    ADMIN,
    SALES,
    EMPLOYEE,

    /**
     * @deprecated MVP2부터 SALES로 대체됨. 신규 부여 불가. DB 호환을 위해 enum 값은 유지.
     */
    @Deprecated
    TEAM_LEADER
}
