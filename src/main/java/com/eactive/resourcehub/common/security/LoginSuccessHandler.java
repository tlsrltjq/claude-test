package com.eactive.resourcehub.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String COOKIE_NAME = "RESOURCEHUB_LAST_EMAIL";
    private static final int MAX_AGE_30_DAYS = 60 * 60 * 24 * 30;

    public LoginSuccessHandler(String defaultTargetUrl) {
        setDefaultTargetUrl(defaultTargetUrl);
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {
        String rememberEmail = request.getParameter("rememberEmail");
        String username = request.getParameter("username");

        if ("on".equals(rememberEmail) && username != null && !username.isBlank()) {
            Cookie cookie = new Cookie(COOKIE_NAME,
                    URLEncoder.encode(username.trim(), StandardCharsets.UTF_8));
            cookie.setMaxAge(MAX_AGE_30_DAYS);
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);
        } else {
            Cookie cookie = new Cookie(COOKIE_NAME, "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
