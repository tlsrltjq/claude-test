package com.eactive.resourcehub.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Position {
    REPRESENTATIVE("대표"),
    EXECUTIVE_DIRECTOR("전무"),
    MANAGING_DIRECTOR("상무"),
    DIRECTOR("이사"),
    GENERAL_MANAGER("부장"),
    DEPUTY_GENERAL_MANAGER("차장"),
    MANAGER("과장"),
    ASSISTANT_MANAGER("대리"),
    STAFF("사원");

    private final String displayName;
}
