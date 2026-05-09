package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findByIdWithTeam(userId).orElseThrow();
    }

    @Transactional
    public void updateProfile(Long userId, String phone, LocalDate birthDate) {
        User user = userRepository.findById(userId).orElseThrow();
        user.updateProfile(phone, birthDate);
    }

    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        user.changePassword(passwordEncoder.encode(newPassword));
        return true;
    }
}
