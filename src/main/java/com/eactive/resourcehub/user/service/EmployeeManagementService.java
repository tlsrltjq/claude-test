package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeManagementService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final AuditService auditService;

    private static final List<UserStatus> MANAGED_STATUSES =
            List.of(UserStatus.ACTIVE, UserStatus.DISABLED);

    @Transactional(readOnly = true)
    public List<User> findAllActive() {
        return userRepository.findByStatusWithTeam(UserStatus.ACTIVE);
    }

    private static final int PAGE_SIZE = 20;

    @Transactional(readOnly = true)
    public Page<User> findActiveFilteredPaged(String q, String position, String role, Long teamId, int page) {
        String qLike = (q == null || q.isBlank()) ? null : "%" + q.toLowerCase() + "%";
        Position pos = parsePosition(position);
        UserRole roleEnum = parseRole(role);
        return userRepository.findFilteredPage(
                MANAGED_STATUSES, qLike, pos, roleEnum, teamId,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE));
    }

    private Position parsePosition(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Position.valueOf(value); }
        catch (IllegalArgumentException e) { return null; }
    }

    private UserRole parseRole(String value) {
        if (value == null || value.isBlank()) return null;
        try { return UserRole.valueOf(value); }
        catch (IllegalArgumentException e) { return null; }
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findByIdWithTeam(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public boolean hasPersonalFolder(Long userId) {
        return folderRepository.existsByOwnerIdAndType(userId, FolderType.PERSONAL);
    }

    @Transactional(readOnly = true)
    public List<Document> findPersonalDocuments(Long userId) {
        return folderRepository.findByOwnerIdAndType(userId, FolderType.PERSONAL)
                .map(folder -> documentRepository.findByFolderIdAndStatusWithVersion(
                        folder.getId(), DocumentStatus.ACTIVE))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public Document findDocumentDetail(Long documentId) {
        return documentRepository.findByIdForDetail(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<DocumentVersion> findDocumentVersions(Long documentId) {
        return documentVersionRepository.findByDocumentIdOrderByVersionNoDesc(documentId);
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

    @Transactional
    public void changePosition(Long userId, Position position, Long actorUserId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String oldPosition = user.getPosition() != null ? user.getPosition().getDisplayName() : "없음";
        user.changePosition(position);
        log.info("직급 변경 — userId={}, {} → {}", userId, oldPosition, position.getDisplayName());
        auditService.log(actorUserId, AuditActionType.UPDATE,
                AuditTargetType.USER, userId,
                "직급 변경: " + oldPosition + " → " + position.getDisplayName(), request);
    }
}
