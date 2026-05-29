package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
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
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findByIdWithTeam(userId).orElseThrow();
    }

    @Transactional
    public void updateProfile(Long userId, String name, String phone, LocalDate birthDate, String address, LocalDate joinDate) {
        User user = userRepository.findById(userId).orElseThrow();
        user.updateProfile(name, phone, birthDate, address, joinDate);
    }

    @Transactional
    public void updateTeam(Long userId, Long teamId) {
        User user = userRepository.findById(userId).orElseThrow();
        Team team = teamId != null ? teamRepository.findById(teamId).orElse(null) : null;
        user.changeTeam(team);
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
