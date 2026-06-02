package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.common.security.LoginAttemptService;
import com.eactive.resourcehub.common.security.SecurityConfig;
import com.eactive.resourcehub.user.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForgotPasswordController.class)
@Import(SecurityConfig.class)
class ForgotPasswordControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean PasswordResetService passwordResetService;
    @MockitoBean LoginAttemptService  loginAttemptService;

    @Test
    void 코드_불일치_4회_누적_후_5번째_실패시_토큰_무효화하고_forgot_폼으로_이동() throws Exception {
        when(passwordResetService.verifyCode(any(), any())).thenReturn(false);

        mockMvc.perform(post("/login/forgot/verify")
                        .with(csrf())
                        .sessionAttr("RESET_EMAIL", "user@test.com")
                        .sessionAttr("RESET_FAIL_COUNT", 4)
                        .param("code", "000000")
                        .param("newPassword", "P@ssw0rd!")
                        .param("newPasswordConfirm", "P@ssw0rd!"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-forgot"));

        verify(passwordResetService).invalidateCurrentToken("user@test.com");
    }

    @Test
    void 이미_5회_초과된_세션이면_즉시_forgot_폼으로_이동() throws Exception {
        mockMvc.perform(post("/login/forgot/verify")
                        .with(csrf())
                        .sessionAttr("RESET_EMAIL", "user@test.com")
                        .sessionAttr("RESET_FAIL_COUNT", 5)
                        .param("code", "000000")
                        .param("newPassword", "P@ssw0rd!")
                        .param("newPasswordConfirm", "P@ssw0rd!"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-forgot"));

        verify(passwordResetService, never()).verifyCode(any(), any());
        verify(passwordResetService).invalidateCurrentToken("user@test.com");
    }

    @Test
    void 코드_불일치_1회이면_남은_횟수_안내_후_verify_폼_유지() throws Exception {
        when(passwordResetService.verifyCode(any(), any())).thenReturn(false);

        mockMvc.perform(post("/login/forgot/verify")
                        .with(csrf())
                        .sessionAttr("RESET_EMAIL", "user@test.com")
                        .param("code", "000000")
                        .param("newPassword", "P@ssw0rd!")
                        .param("newPasswordConfirm", "P@ssw0rd!"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-forgot-verify"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(passwordResetService, never()).invalidateCurrentToken(any());
    }
}
