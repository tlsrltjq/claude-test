package com.eactive.resourcehub.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String email = request.getParameter("username");
        if (email != null && !email.isBlank()) {
            loginAttemptService.loginFailed(email);
            if (loginAttemptService.isOverLimit(email)) {
                log.warn("[LOGIN] 10회 실패 — 비밀번호 재설정 유도 email={}", email);
                loginAttemptService.loginSucceeded(email); // 카운터 리셋 후 재설정 페이지로
                getRedirectStrategy().sendRedirect(request, response, "/login/forgot?toomany");
                return;
            }
        }
        getRedirectStrategy().sendRedirect(request, response, "/login?error");
    }
}
