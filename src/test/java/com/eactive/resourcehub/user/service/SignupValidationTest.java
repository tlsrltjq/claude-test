package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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

    private SignupService signupService;

    @BeforeEach
    void setUp() {
        signupService = new SignupService(
                userRepository, teamRepository,
                new BCryptPasswordEncoder(), emailSender, folderRepository
        );
        ReflectionTestUtils.setField(signupService, "companyEmailDomain", "eactive.co.kr");
    }

    // ── 이메일 중복 ────────────────────────────────────────────

    @Test
    void 이미_가입된_이메일이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 신규_이메일이면_인증코드_반환() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        String code = signupService.initiateSignup(req);
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    // ── 비밀번호 복잡도 ─────────────────────────────────────────

    @Test
    void 비밀번호_8자_미만이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Ab1!");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 비밀번호_영문만이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "abcdefgh");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 비밀번호_영문숫자만이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "abcd1234");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 비밀번호_영문숫자특수문자_포함이면_통과() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        assertDoesNotThrow(() -> signupService.initiateSignup(req));
    }

    @Test
    void 비밀번호_불일치이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        req.setPasswordConfirm("Aa1!wxyz");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    // ── 생년월일 파싱 ──────────────────────────────────────────

    @Test
    void 생년월일_8자리_숫자_형식_통과() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        req.setBirthDateStr("19900101");
        assertDoesNotThrow(() -> signupService.initiateSignup(req));
    }

    @Test
    void 생년월일_점구분_형식_통과() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        req.setBirthDateStr("1990.01.01");
        assertDoesNotThrow(() -> signupService.initiateSignup(req));
    }

    @Test
    void 생년월일_빈값이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        req.setBirthDateStr("");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 생년월일_잘못된_형식이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        req.setBirthDateStr("abcdefgh");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void 생년월일_미래날짜이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        SignupRequest req = makeRequest("john", "Aa1!abcd");
        req.setBirthDateStr("29991231");
        assertThrows(IllegalArgumentException.class, () -> signupService.initiateSignup(req));
    }

    @Test
    void buildEmail_prefix_도메인_결합() {
        assertEquals("john@eactive.co.kr", signupService.buildEmail("john"));
    }

    // ── 헬퍼 ──────────────────────────────────────────────────

    private SignupRequest makeRequest(String prefix, String password) {
        SignupRequest req = new SignupRequest();
        req.setName("테스트");
        req.setBirthDateStr("19900101");
        req.setPhone("010-1234-5678");
        req.setEmailPrefix(prefix);
        req.setPosition(Position.STAFF);
        req.setPassword(password);
        req.setPasswordConfirm(password);
        return req;
    }
}
