package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeManagementService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final FolderRepository folderRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<User> findAllActive() {
        return userRepository.findByStatusWithTeam(UserStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findByIdWithTeam(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public boolean hasPersonalFolder(Long userId) {
        return folderRepository.existsByOwnerId(userId);
    }

    @Transactional
    public void changeTeam(Long userId, Long teamId, Long actorUserId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        String oldTeamName = user.getTeam() != null ? user.getTeam().getName() : "없음";
        user.changeTeam(team);
        log.info("팀 변경 — userId={}, {} → {}", userId, oldTeamName, team.getName());
        auditService.log(actorUserId, AuditActionType.UPDATE,
                AuditTargetType.USER, userId,
                "팀 변경: " + oldTeamName + " → " + team.getName(), request);
    }
}
