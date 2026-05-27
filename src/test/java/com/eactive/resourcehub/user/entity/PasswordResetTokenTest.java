package com.eactive.resourcehub.user.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    private static final String CODE = "123456";

    @Test
    void 생성_직후_만료되지_않음() {
        PasswordResetToken token = PasswordResetToken.create(makeUser(), CODE);
        assertFalse(token.isExpired());
        assertFalse(token.isVerified());
        assertFalse(token.isConsumed());
    }

    @Test
    void markVerified_호출_후_verified_true() {
        PasswordResetToken token = PasswordResetToken.create(makeUser(), CODE);
        token.markVerified();
        assertTrue(token.isVerified());
        assertFalse(token.isConsumed());
    }

    @Test
    void markConsumed_호출_후_consumed_true() {
        PasswordResetToken token = PasswordResetToken.create(makeUser(), CODE);
        token.markVerified();
        token.markConsumed();
        assertTrue(token.isConsumed());
    }

    @Test
    void invalidate_호출_후_즉시_만료() {
        PasswordResetToken token = PasswordResetToken.create(makeUser(), CODE);
        token.invalidate();
        assertTrue(token.isExpired());
    }

    @Test
    void expiredAt_과거로_설정하면_만료() throws Exception {
        PasswordResetToken token = PasswordResetToken.create(makeUser(), CODE);
        setField(token, "expiredAt", LocalDateTime.now().minusMinutes(1));
        assertTrue(token.isExpired());
    }

    @Test
    void TTL이_5분임을_확인() {
        assertEquals(5, PasswordResetToken.TOKEN_TTL.toMinutes());
    }

    @Test
    void 토큰_코드_저장_확인() {
        PasswordResetToken token = PasswordResetToken.create(makeUser(), CODE);
        assertEquals(CODE, token.getToken());
    }

    @Test
    void 이메일_사용자_이메일과_일치() {
        User user = makeUser();
        PasswordResetToken token = PasswordResetToken.create(user, CODE);
        assertEquals(user.getEmail(), token.getEmail());
    }

    // ── 헬퍼 ──────────────────────────────────────────────────

    private User makeUser() {
        return User.create(
                "testlogin", "encodedPw", "테스트",
                "test@eactive.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-1234-5678"
        );
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
