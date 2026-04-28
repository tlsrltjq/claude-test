package com.eactive.resourcehub.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 1단계 보안 설정 — 의존성만 추가하고 모든 요청을 허용한다.
 *
 * <p>본격적인 세션 인증/CSRF/Remember-me 정책은 3단계에서 잡는다.
 * 절대 원칙은 그대로 유지된다:
 * <ul>
 *   <li>JWT 사용 금지</li>
 *   <li>Remember-me 사용 금지</li>
 *   <li>3단계에서 CSRF 활성화 + 세션 쿠키 RESOURCEHUB_SESSION (httpOnly+sameSite=strict)</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Stage 01: /health 가 동작하도록 모든 요청 허용.
        // CSRF 도 일단 비활성 — 3단계에서 form 로그인과 함께 enable.
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
