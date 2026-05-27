package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.user.entity.PasswordResetToken;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.PasswordResetTokenRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordResetServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailSender emailSender;
    @Mock AuditService auditService;
    @Mock HttpServletRequest request;

    @InjectMocks PasswordResetService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create("hong@eactive.co.kr", "encoded", "홍길동",
                "hong@eactive.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    // ── requestReset ────────────────────────────────────────────

    @Test
    void 존재하는_이메일_코드_발급_성공() {
        when(userRepository.findByEmail("hong@eactive.co.kr")).thenReturn(Optional.of(user));
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        ReflectionTestUtils.setField(token, "id", 1L);
        when(tokenRepository.save(any())).thenReturn(token);

        assertDoesNotThrow(() -> service.requestReset("hong@eactive.co.kr"));
        verify(tokenRepository).save(any());
    }

    @Test
    void 존재하지_않는_이메일은_조용히_무시() {
        when(userRepository.findByEmail("ghost@eactive.co.kr")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.requestReset("ghost@eactive.co.kr"));
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void 이메일_발송_실패해도_예외_전파하지_않음() {
        when(userRepository.findByEmail("hong@eactive.co.kr")).thenReturn(Optional.of(user));
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        ReflectionTestUtils.setField(token, "id", 1L);
        when(tokenRepository.save(any())).thenReturn(token);
        doThrow(new RuntimeException("SMTP 장애")).when(emailSender).sendPasswordResetCode(anyString(), anyString());

        assertDoesNotThrow(() -> service.requestReset("hong@eactive.co.kr"));
    }

    // ── verifyCode ──────────────────────────────────────────────

    @Test
    void 올바른_코드이면_검증_성공() {
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.of(token));

        assertTrue(service.verifyCode("hong@eactive.co.kr", "123456"));
    }

    @Test
    void 토큰_없으면_검증_실패() {
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.empty());
        assertFalse(service.verifyCode("hong@eactive.co.kr", "123456"));
    }

    @Test
    void 만료된_토큰이면_검증_실패() {
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        token.invalidate();
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.of(token));

        assertFalse(service.verifyCode("hong@eactive.co.kr", "123456"));
    }

    @Test
    void 이미_소비된_토큰이면_검증_실패() {
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        token.markVerified();
        token.markConsumed();
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.of(token));

        assertFalse(service.verifyCode("hong@eactive.co.kr", "123456"));
    }

    @Test
    void 코드_불일치이면_검증_실패() {
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.of(token));

        assertFalse(service.verifyCode("hong@eactive.co.kr", "999999"));
    }

    // ── resetPassword ───────────────────────────────────────────

    @Test
    void 검증된_토큰으로_비밀번호_재설정_성공() {
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        token.markVerified();
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");

        assertDoesNotThrow(() -> service.resetPassword("hong@eactive.co.kr", "Aa1!abcd", request));
        assertTrue(token.isConsumed());
    }

    @Test
    void 미검증_토큰으로_재설정_시_예외() {
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.of(token));

        assertThrows(IllegalStateException.class,
                () -> service.resetPassword("hong@eactive.co.kr", "Aa1!abcd", request));
    }

    @Test
    void 이미_사용된_토큰으로_재설정_시_예외() {
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        token.markVerified();
        token.markConsumed();
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.of(token));

        assertThrows(IllegalStateException.class,
                () -> service.resetPassword("hong@eactive.co.kr", "Aa1!abcd", request));
    }

    @Test
    void 단순_비밀번호로_재설정_시_예외() {
        PasswordResetToken token = PasswordResetToken.create(user, "123456");
        token.markVerified();
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("hong@eactive.co.kr", "simple", request));
    }

    @Test
    void 토큰_없이_재설정_시_예외() {
        when(tokenRepository.findTopByEmailOrderByCreatedAtDesc("hong@eactive.co.kr"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.resetPassword("hong@eactive.co.kr", "Aa1!abcd", request));
    }
}
