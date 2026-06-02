package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.common.security.LoginAttemptService;
import com.eactive.resourcehub.common.security.SecurityConfig;
import com.eactive.resourcehub.team.service.TeamService;
import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.service.SignupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignupController.class)
@Import(SecurityConfig.class)
class SignupControllerVerifyTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean SignupService       signupService;
    @MockitoBean TeamService         teamService;
    @MockitoBean PasswordEncoder     passwordEncoder;
    @MockitoBean LoginAttemptService loginAttemptService;

    private SignupRequest pendingRequest() {
        SignupRequest req = new SignupRequest();
        req.setEmail("new@test.com");
        return req;
    }

    @Test
    void 코드_불일치_4회_누적_후_5번째_실패시_세션_초기화하고_signup으로_리다이렉트() throws Exception {
        mockMvc.perform(post("/signup/verify")
                        .with(csrf())
                        .sessionAttr("PENDING_SIGNUP_REQUEST",    pendingRequest())
                        .sessionAttr("PENDING_SIGNUP_CODE",       "123456")
                        .sessionAttr("PENDING_SIGNUP_EXPIRY",     LocalDateTime.now().plusMinutes(5))
                        .sessionAttr("PENDING_SIGNUP_HASHED_PW",  "$2a$10$hash")
                        .sessionAttr("PENDING_SIGNUP_FAIL_COUNT", 4)
                        .param("code", "000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup?toomany"));
    }

    @Test
    void 이미_5회_초과된_세션이면_즉시_signup으로_리다이렉트() throws Exception {
        mockMvc.perform(post("/signup/verify")
                        .with(csrf())
                        .sessionAttr("PENDING_SIGNUP_REQUEST",    pendingRequest())
                        .sessionAttr("PENDING_SIGNUP_CODE",       "123456")
                        .sessionAttr("PENDING_SIGNUP_EXPIRY",     LocalDateTime.now().plusMinutes(5))
                        .sessionAttr("PENDING_SIGNUP_HASHED_PW",  "$2a$10$hash")
                        .sessionAttr("PENDING_SIGNUP_FAIL_COUNT", 5)
                        .param("code", "000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup?toomany"));
    }

    @Test
    void 코드_불일치_1회이면_남은_횟수_안내_후_verify_폼_유지() throws Exception {
        mockMvc.perform(post("/signup/verify")
                        .with(csrf())
                        .sessionAttr("PENDING_SIGNUP_REQUEST",   pendingRequest())
                        .sessionAttr("PENDING_SIGNUP_CODE",      "123456")
                        .sessionAttr("PENDING_SIGNUP_EXPIRY",    LocalDateTime.now().plusMinutes(5))
                        .sessionAttr("PENDING_SIGNUP_HASHED_PW", "$2a$10$hash")
                        .param("code", "000000"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup-verify"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
