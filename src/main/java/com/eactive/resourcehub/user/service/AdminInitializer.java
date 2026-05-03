package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${resourcehub.admin.email}")
    private String adminEmail;

    @Value("${resourcehub.admin.password}")
    private String adminPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User admin = User.create(
                adminEmail,
                passwordEncoder.encode(adminPassword),
                "관리자",
                adminEmail,
                null,
                Position.REPRESENTATIVE,
                java.time.LocalDate.of(1970, 1, 1),
                ""
        );
        admin.changeRole(UserRole.ADMIN);
        admin.verifyEmail();   // emailVerified=true, status=PENDING_ADMIN_APPROVAL
        admin.activate();      // status=ACTIVE

        userRepository.save(admin);
        log.info("기본 관리자 계정 생성 — email={}", adminEmail);
    }
}
