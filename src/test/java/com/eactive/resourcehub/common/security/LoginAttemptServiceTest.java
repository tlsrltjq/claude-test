package com.eactive.resourcehub.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    void 초기_상태에서_isOverLimit_false() {
        assertFalse(service.isOverLimit("user@test.com"));
    }

    @Test
    void 실패_횟수가_10미만이면_isOverLimit_false() {
        for (int i = 0; i < 9; i++) service.loginFailed("user@test.com");
        assertFalse(service.isOverLimit("user@test.com"));
    }

    @Test
    void 실패_10회_도달시_isOverLimit_true() {
        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            service.loginFailed("user@test.com");
        }
        assertTrue(service.isOverLimit("user@test.com"));
    }

    @Test
    void loginSucceeded_호출시_카운터_초기화() {
        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            service.loginFailed("user@test.com");
        }
        assertTrue(service.isOverLimit("user@test.com"));

        service.loginSucceeded("user@test.com");
        assertFalse(service.isOverLimit("user@test.com"));
        assertEquals(0, service.getFailCount("user@test.com"));
    }

    @Test
    void 이메일_대소문자_정규화() {
        service.loginFailed("User@Test.COM");
        assertEquals(1, service.getFailCount("user@test.com"));
    }

    @Test
    void 다른_이메일은_독립된_카운터() {
        for (int i = 0; i < LoginAttemptService.MAX_ATTEMPTS; i++) {
            service.loginFailed("a@test.com");
        }
        assertTrue(service.isOverLimit("a@test.com"));
        assertFalse(service.isOverLimit("b@test.com"));
    }

    @Test
    void getFailCount_정확히_반환() {
        service.loginFailed("user@test.com");
        service.loginFailed("user@test.com");
        service.loginFailed("user@test.com");
        assertEquals(3, service.getFailCount("user@test.com"));
    }

    @Test
    void 미등록_이메일_getFailCount_0() {
        assertEquals(0, service.getFailCount("nobody@test.com"));
    }
}
