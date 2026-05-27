package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.user.entity.AllowedEmail;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.AllowedEmailRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailAllowlistService {

    private final AllowedEmailRepository allowedEmailRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean isAllowed(String email) {
        return allowedEmailRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public List<AllowedEmail> findAll() {
        return allowedEmailRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public AllowedEmail add(String email, String note, Long adminUserId) {
        String normalized = email.trim().toLowerCase();
        if (allowedEmailRepository.existsByEmail(normalized)) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + normalized);
        }
        User admin = userRepository.findById(adminUserId).orElseThrow();
        return allowedEmailRepository.save(AllowedEmail.create(normalized, note, admin));
    }

    @Transactional
    public void remove(Long id) {
        if (!allowedEmailRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 허용 이메일입니다.");
        }
        allowedEmailRepository.deleteById(id);
    }
}
