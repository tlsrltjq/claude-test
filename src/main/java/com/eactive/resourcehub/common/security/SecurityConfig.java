package com.eactive.resourcehub.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(c -> {})
                .referrerPolicy(ref -> ref.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' cdn.jsdelivr.net *.daumcdn.net *.kakao.com 'unsafe-inline'; " +
                    "style-src 'self' cdn.jsdelivr.net 'unsafe-inline'; " +
                    "img-src 'self' data: *.daumcdn.net *.kakao.com *.kakaocdn.net; " +
                    "font-src 'self' cdn.jsdelivr.net; " +
                    "frame-src 'self' *.daumcdn.net *.kakao.com; " +
                    "connect-src 'self' *.daumcdn.net *.kakao.com"
                ))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/login", "/login/forgot", "/login/forgot/verify",
                        "/signup", "/signup/**",
                        "/error", "/error/**",
                        "/health", "/css/**", "/js/**", "/images/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/sales/**").hasAnyRole("ADMIN", "SALES")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(new LoginSuccessHandler("/dashboard"))
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("RESOURCEHUB_SESSION")
                .permitAll()
            )
            .sessionManagement(session -> {
                session.sessionFixation().changeSessionId();
                session.maximumSessions(-1)
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl("/login?expired");
            });

        return http.build();
    }
}
