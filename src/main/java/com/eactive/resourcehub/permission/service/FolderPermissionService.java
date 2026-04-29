package com.eactive.resourcehub.permission.service;

import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.audit.service.AuditLogService;
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

@Service
@RequiredArgsConstructor
public class FolderPermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<Permission> findPermissionsByUser(Long userId) {
        return permissionRepository.findByUserIdAndTargetType(userId, PermissionTargetType.FOLDER);
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

        auditLogService.logGrantPermission(actorUserId, saved.getId(),
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

        auditLogService.logRevokePermission(actorUserId, targetUserId,
                "FOLDER_ACCESS 회수: userId=" + targetUserId + ", folderId=" + folderId, request);
    }
}
