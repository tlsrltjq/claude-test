package com.eactive.resourcehub.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.header.HeaderWriter;

public class CspNonceHeaderWriter implements HeaderWriter {

    private static final String HEADER = "Content-Security-Policy";

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
        String nonce = (String) request.getAttribute(CspNonceFilter.NONCE_ATTR);
        if (nonce == null) return;
        response.setHeader(HEADER,
            "default-src 'self'; " +
            "script-src 'self' cdn.jsdelivr.net *.daumcdn.net *.kakao.com 'nonce-" + nonce + "'; " +
            "style-src 'self' cdn.jsdelivr.net 'unsafe-inline'; " +
            "img-src 'self' data: *.daumcdn.net *.kakao.com *.kakaocdn.net; " +
            "font-src 'self' cdn.jsdelivr.net; " +
            "frame-src 'self' *.daumcdn.net *.kakao.com; " +
            "connect-src 'self' *.daumcdn.net *.kakao.com"
        );
    }
}
