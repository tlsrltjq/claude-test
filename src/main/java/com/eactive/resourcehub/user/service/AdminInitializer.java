package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.FolderRepository;
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
    private final FolderRepository folderRepository;

    @Value("${resourcehub.admin.email}")
    private String adminEmail;

    @Value("${resourcehub.admin.password}")
    private String adminPassword;

    @Value("${resourcehub.company-email-domain}")
    private String companyEmailDomain;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        User admin = ensureSeedUser(adminEmail, adminPassword, "관리자", UserRole.ADMIN, Position.REPRESENTATIVE);
        String salesEmail = "test@" + companyEmailDomain;
        ensureSeedUser(salesEmail, "Test1234!", "테스트영업", UserRole.SALES, Position.MANAGER);
        ensurePublicFolder(admin);
    }

    private void ensurePublicFolder(User admin) {
        if (!folderRepository.existsByType(FolderType.SHARED_PUBLIC)) {
            folderRepository.save(Folder.createPublic(admin, "전사 공용 폴더"));
            log.info("전사 공용 폴더 생성 완료");
        }
    }

    private User ensureSeedUser(String email, String rawPassword, String name,
                                 UserRole role, Position position) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = User.create(email, passwordEncoder.encode(rawPassword), name,
                    email, null, position,
                    java.time.LocalDate.of(role == UserRole.ADMIN ? 1970 : 1990, 1, 1),
                    role == UserRole.ADMIN ? "" : "010-0000-0000");
            u.changeRole(role);
            u.verifyEmail();
            u.activate();
            User saved = userRepository.save(u);
            log.info("시드 계정 생성 — email={}, role={}", email, role);
            return saved;
        });

        if (!folderRepository.existsByOwnerIdAndType(user.getId(), FolderType.PERSONAL)) {
            folderRepository.save(Folder.create(user, user.getName() + " 개인 폴더"));
            log.info("시드 계정 폴더 생성 — email={}", email);
        }
        return user;
    }
}
