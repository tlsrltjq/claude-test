package com.eactive.resourcehub.common.util;

public final class PasswordValidator {

    private PasswordValidator() {}

    public static void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        int types = 0;
        if (password.matches(".*[a-zA-Z].*")) types++;
        if (password.matches(".*[0-9].*"))    types++;
        if (password.matches(".*[^a-zA-Z0-9].*")) types++;
        if (types < 3) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
    }
}
