package com.eactive.resourcehub.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    void 유효한_비밀번호_통과() {
        assertThatNoException().isThrownBy(() -> PasswordValidator.validate("Abc123!@#"));
        assertThatNoException().isThrownBy(() -> PasswordValidator.validate("hello1!WORLD"));
        assertThatNoException().isThrownBy(() -> PasswordValidator.validate("12345678aB!"));
    }

    @Test
    void null이면_예외() {
        assertThatThrownBy(() -> PasswordValidator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8자 이상");
    }

    @Test
    void 길이_부족이면_예외() {
        assertThatThrownBy(() -> PasswordValidator.validate("Ab1!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8자 이상");
    }

    @Test
    void 영문만_있으면_복잡도_부족_예외() {
        assertThatThrownBy(() -> PasswordValidator.validate("abcdefgh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("영문, 숫자, 특수문자");
    }

    @Test
    void 영문과_숫자만_있으면_복잡도_부족_예외() {
        assertThatThrownBy(() -> PasswordValidator.validate("abcdef12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("영문, 숫자, 특수문자");
    }

    @Test
    void 숫자와_특수문자만_있으면_복잡도_부족_예외() {
        assertThatThrownBy(() -> PasswordValidator.validate("12345678!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("영문, 숫자, 특수문자");
    }

    @Test
    void 영문_숫자_특수문자_모두_포함하면_통과() {
        assertThatNoException().isThrownBy(() -> PasswordValidator.validate("P@ssw0rd"));
    }
}
