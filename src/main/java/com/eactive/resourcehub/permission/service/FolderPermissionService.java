package com.eactive.resourcehub.permission.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.permission.entity.Permission;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderPermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Permission> findPermissionsByUser(Long userId) {
        return permissionRepository.findByUserIdAndTargetType(userId, PermissionTargetType.FOLDER);
    }

    @Transactional(readOnly = true)
    public List<Folder> findGrantableFolders(Long userId) {
        List<Long> grantedFolderIds = findPermissionsByUser(userId).stream()
                .map(Permission::getTargetId).collect(Collectors.toList());
        return folderRepository.findAllWithOwner().stream()
                .filter(f -> !f.getOwner().getId().equals(userId))
                .filter(f -> !grantedFolderIds.contains(f.getId()))
                .toList();
    }

    @Transactional
    public void grant(Long targetUserId, Long folderId, Long actorUserId, HttpServletRequest request) {
        if (permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                targetUserId, PermissionType.FOLDER_ACCESS, PermissionTargetType.FOLDER, folderId)) {
            throw new IllegalArgumentException("이미 부여된 권한입니다.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        Permission permission = Permission.grant(
                targetUser, PermissionType.FOLDER_ACCESS,
                PermissionTargetType.FOLDER, folderId, actor);
        Permission saved = permissionRepository.save(permission);

        auditService.log(actorUserId, AuditActionType.GRANT_PERMISSION,
                AuditTargetType.PERMISSION, saved.getId(),
                "FOLDER_ACCESS 부여: userId=" + targetUserId + ", folderId=" + folderId, request);
    }

    @Transactional
    public void revoke(Long targetUserId, Long folderId, Long actorUserId, HttpServletRequest request) {
        if (!permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                targetUserId, PermissionType.FOLDER_ACCESS, PermissionTargetType.FOLDER, folderId)) {
            throw new IllegalArgumentException("부여된 권한이 없습니다.");
        }

        permissionRepository.deleteByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                targetUserId, PermissionType.FOLDER_ACCESS, PermissionTargetType.FOLDER, folderId);

        auditService.log(actorUserId, AuditActionType.REVOKE_PERMISSION,
                AuditTargetType.PERMISSION, targetUserId,
                "FOLDER_ACCESS 회수: userId=" + targetUserId + ", folderId=" + folderId, request);
    }
}
