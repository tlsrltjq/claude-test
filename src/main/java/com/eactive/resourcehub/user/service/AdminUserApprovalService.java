package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.service.FolderService;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.entity.Position;
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
public class AdminUserApprovalService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final FolderService folderService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<User> findPendingUsers() {
        return userRepository.findByStatus(UserStatus.PENDING_ADMIN_APPROVAL);
    }

    @Transactional
    public void approve(Long userId, Long teamId, Position position,
                        Long actorUserId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않은 사용자입니다.");
        }
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("이미 승인된 사용자입니다.");
        }

        if (teamId != null) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
            user.changeTeam(team);
        }
        if (position != null) {
            user.changePosition(position);
        }
        user.activate();

        // 개인 폴더 생성 (같은 트랜잭션)
        folderService.createPersonalFolder(user, actorUserId, request);

        log.info("사용자 승인 — userId={}, email={}", userId, user.getEmail());
        auditService.log(actorUserId, AuditActionType.APPROVE,
                AuditTargetType.USER, userId, "사용자 승인", request);
    }

    @Transactional
    public void reject(Long userId, String reason, Long actorUserId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("이미 승인된 사용자는 반려할 수 없습니다.");
        }

        user.reject();
        log.info("사용자 반려 — userId={}, reason={}", userId, reason);
        auditService.log(actorUserId, AuditActionType.REJECT,
                AuditTargetType.USER, userId, reason, request);
    }
}
