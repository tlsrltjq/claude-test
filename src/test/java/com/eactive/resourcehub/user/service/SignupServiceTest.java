package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.AllowedEmailRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock UserRepository         userRepository;
    @Mock TeamRepository         teamRepository;
    @Mock PasswordEncoder        passwordEncoder;
    @Mock EmailSender            emailSender;
    @Mock FolderRepository       folderRepository;
    @Mock AllowedEmailRepository allowedEmailRepository;

    @InjectMocks SignupService signupService;

    private SignupRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SignupRequest();
        validRequest.setName("홍길동");
        validRequest.setBirthDateStr("19900101");
        validRequest.setPhone("010-1234-5678");
        validRequest.setEmail("hong@test.com");
        validRequest.setPosition(Position.STAFF);
        validRequest.setPassword("Test1234!");
        validRequest.setPasswordConfirm("Test1234!");
        validRequest.setPrivacyConsent(true);
    }

    // ── initiateSignup 유효성 검증 ────────────────────────────────

    @Test
    void initiateSignup_허용되지않은_이메일이면_예외() {
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> signupService.initiateSignup(validRequest));
    }

    @Test
    void initiateSignup_이미_존재하는_이메일이면_예외() {
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> signupService.initiateSignup(validRequest));
    }

    @Test
    void initiateSignup_개인정보_동의_안하면_예외() {
        validRequest.setPrivacyConsent(false);
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> signupService.initiateSignup(validRequest));
    }

    @Test
    void initiateSignup_비밀번호_불일치이면_예외() {
        validRequest.setPasswordConfirm("Other1234!");
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> signupService.initiateSignup(validRequest));
    }

    @Test
    void initiateSignup_유효하지않은_생년월일이면_예외() {
        validRequest.setBirthDateStr("invalid");

        assertThrows(IllegalArgumentException.class,
                () -> signupService.initiateSignup(validRequest));
    }

    @Test
    void initiateSignup_미래_생년월일이면_예외() {
        validRequest.setBirthDateStr("29990101");
        // parseBirthDate 가 먼저 실행되어 예외 — allowlist/user repo 미호출
        assertThrows(IllegalArgumentException.class,
                () -> signupService.initiateSignup(validRequest));
    }

    @Test
    void initiateSignup_성공이면_6자리_인증코드_반환() {
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        String code = signupService.initiateSignup(validRequest);

        assertThat(code).matches("\\d{6}");
        verify(emailSender).sendVerificationCode(eq("hong@test.com"), eq(code));
    }

    @Test
    void initiateSignup_이메일_발송_실패해도_코드_반환() {
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        doThrow(new RuntimeException("SMTP error")).when(emailSender)
                .sendVerificationCode(anyString(), anyString());

        String code = signupService.initiateSignup(validRequest);

        assertThat(code).matches("\\d{6}");
    }

    // ── completeSignup ────────────────────────────────────────────

    @Test
    void completeSignup_이메일_중복이면_예외() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> signupService.completeSignup(validRequest));
    }

    @Test
    void completeSignup_성공시_유저저장_폴더생성_ACTIVE상태() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        // teamId == null → teamRepository.findById 호출 안 됨

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return u;
        });
        when(folderRepository.existsByOwnerIdAndType(any(), eq(FolderType.PERSONAL)))
                .thenReturn(false);
        when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        signupService.completeSignup(validRequest);

        User saved = userCaptor.getValue();
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
        assertTrue(saved.isEmailVerified());
        verify(folderRepository).save(any(Folder.class));
    }

    @Test
    void completeSignup_이미_폴더존재하면_폴더_미생성() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(folderRepository.existsByOwnerIdAndType(any(), eq(FolderType.PERSONAL)))
                .thenReturn(true);

        signupService.completeSignup(validRequest);

        verify(folderRepository, never()).save(any());
    }

    @Test
    void completeSignup_이메일_소문자_정규화() {
        validRequest.setEmail("  HONG@TEST.COM  ");
        when(userRepository.existsByEmail("hong@test.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(folderRepository.existsByOwnerIdAndType(any(), any())).thenReturn(false);
        when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        signupService.completeSignup(validRequest);

        verify(userRepository).existsByEmail("hong@test.com");
    }

    // ── resendVerificationCode ────────────────────────────────────

    @Test
    void resend_새_코드_생성_발송() {
        String code = signupService.resendVerificationCode("hong@test.com");

        assertThat(code).matches("\\d{6}");
        verify(emailSender).sendVerificationCode(eq("hong@test.com"), eq(code));
    }

    @Test
    void resend_이메일_발송_실패해도_코드_반환() {
        doThrow(new RuntimeException("SMTP error")).when(emailSender)
                .sendVerificationCode(anyString(), anyString());

        String code = signupService.resendVerificationCode("hong@test.com");

        assertThat(code).matches("\\d{6}");
    }

    // ── parseBirthDate 경계 ───────────────────────────────────────

    @Test
    void 생년월일_점구분_형식_허용() {
        validRequest.setBirthDateStr("1990.01.01");
        when(allowedEmailRepository.existsByEmail(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> signupService.initiateSignup(validRequest));
    }

    @Test
    void 생년월일_빈값이면_예외() {
        validRequest.setBirthDateStr("");

        assertThrows(IllegalArgumentException.class,
                () -> signupService.initiateSignup(validRequest));
    }
}
