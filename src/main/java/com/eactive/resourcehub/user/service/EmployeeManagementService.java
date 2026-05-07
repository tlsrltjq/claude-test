package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
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

    private static final List<UserStatus> MANAGED_STATUSES =
            List.of(UserStatus.ACTIVE, UserStatus.DISABLED);

    @Transactional(readOnly = true)
    public List<User> findAllActive() {
        return userRepository.findByStatusWithTeam(UserStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<User> findActiveFiltered(String q, String position, String role, Long teamId) {
        return userRepository.findByStatusInWithTeam(MANAGED_STATUSES).stream()
                .filter(u -> {
                    if (q == null || q.isBlank()) return true;
                    String kw = q.toLowerCase();
                    String name  = u.getName()  != null ? u.getName().toLowerCase()  : "";
                    String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                    return name.contains(kw) || email.contains(kw);
                })
                .filter(u -> {
                    if (position == null || position.isBlank()) return true;
                    try { return u.getPosition() == Position.valueOf(position); }
                    catch (IllegalArgumentException e) { return false; }
                })
                .filter(u -> {
                    if (role == null || role.isBlank()) return true;
                    try { return u.getRole() == UserRole.valueOf(role); }
                    catch (IllegalArgumentException e) { return false; }
                })
                .filter(u -> teamId == null || (u.getTeam() != null && teamId.equals(u.getTeam().getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findByIdWithTeam(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public boolean hasPersonalFolder(Long userId) {
        return folderRepository.existsByOwnerIdAndType(userId, FolderType.PERSONAL);
    }

    /**
     * ACTIVE ↔ DISABLED 토글. 관리자 계정은 대상 불가.
     * @return 변경 후 상태 (ACTIVE or DISABLED)
     */
    @Transactional
    public UserStatus toggleStatus(Long userId, Long actorId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 비활성화할 수 없습니다.");
        }
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.disable();
            auditService.log(actorId, AuditActionType.DISABLE_USER,
                    AuditTargetType.USER, userId, null, request);
            log.info("계정 비활성화 — userId={}", userId);
            return UserStatus.DISABLED;
        } else {
            user.activate();
            auditService.log(actorId, AuditActionType.ENABLE_USER,
                    AuditTargetType.USER, userId, null, request);
            log.info("계정 활성화 — userId={}", userId);
            return UserStatus.ACTIVE;
        }
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
