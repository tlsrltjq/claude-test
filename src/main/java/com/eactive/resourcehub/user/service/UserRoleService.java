package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.audit.service.AuditLogService;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void changeRole(Long targetUserId, UserRole newRole, Long actorUserId,
                           HttpServletRequest request) {
        if (newRole == UserRole.TEAM_LEADER) {
            throw new IllegalArgumentException("TEAM_LEADER는 더 이상 사용되지 않습니다. SALES를 사용하세요.");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserRole oldRole = user.getRole();
        user.changeRole(newRole);

        auditLogService.logChangeRole(actorUserId, targetUserId,
                "역할 변경: " + oldRole + " → " + newRole, request);
    }
}
