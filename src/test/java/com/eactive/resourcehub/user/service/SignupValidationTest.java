package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.repository.AllowedEmailRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SignupValidationTest {

    @Mock UserRepository userRepository;
    @Mock TeamRepository teamRepository;
    @Mock EmailSender emailSender;
    @Mock FolderRepository folderRepository;
    @Mock AllowedEmailRepository allowedEmailRepository;

    private SignupService signupService;

    @BeforeEach
    void setUp() {
        signupService = new SignupService(
                userRepository, teamRepository,
                new BCryptPasswordEncoder(), emailSender, folderRepository,
                allowedEmailRepository
        );
        // 기본: 허용 이메일로 간주
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(true);
    }

    // ── 이메일 허용 목록 ────────────────────────────────────────

    @Test
    void 허용되지_않은_이메일이면_예외() {
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    // ── 이메일 중복 ────────────────────────────────────────────

    @Test
    void 이미_가입된_이메일이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 신규_이메일이면_인증코드_반환() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        String code = signupService.initiateSignup(req);
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    // ── 개인정보 동의 ───────────────────────────────────────────

    @Test
    void 개인정보_동의_미체크이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        req.setPrivacyConsent(false);
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    // ── 비밀번호 복잡도 ─────────────────────────────────────────

    @Test
    void 비밀번호_8자_미만이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Ab1!");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 비밀번호_영문만이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "abcdefgh");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 비밀번호_영문숫자만이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "abcd1234");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 비밀번호_영문숫자특수문자_포함이면_통과() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        assertDoesNotThrow(() -> signupService.initiateSignup(req));
    }

    @Test
    void 비밀번호_불일치이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        req.setPasswordConfirm("Aa1!wxyz");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    // ── 생년월일 파싱 ──────────────────────────────────────────

    @Test
    void 생년월일_8자리_숫자_형식_통과() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        req.setBirthDateStr("19900101");
        assertDoesNotThrow(() -> signupService.initiateSignup(req));
    }

    @Test
    void 생년월일_점구분_형식_통과() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        req.setBirthDateStr("1990.01.01");
        assertDoesNotThrow(() -> signupService.initiateSignup(req));
    }

    @Test
    void 생년월일_빈값이면_예외() {
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        req.setBirthDateStr("");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 생년월일_잘못된_형식이면_예외() {
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        req.setBirthDateStr("abcdefgh");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 생년월일_미래날짜이면_예외() {
        SignupRequest req = makeRequest("john@eactive.co.kr", "Aa1!abcd");
        req.setBirthDateStr("29991231");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    // ── 헬퍼 ──────────────────────────────────────────────────

    private SignupRequest makeRequest(String email, String password) {
        SignupRequest req = new SignupRequest();
        req.setName("테스트");
        req.setBirthDateStr("19900101");
        req.setPhone("010-1234-5678");
        req.setEmail(email);
        req.setPosition(Position.STAFF);
        req.setPassword(password);
        req.setPasswordConfirm(password);
        req.setPrivacyConsent(true);
        return req;
    }
}
