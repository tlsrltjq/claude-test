package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import com.eactive.resourcehub.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FolderAccessService {

    private final FolderRepository folderRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 폴더 읽기 접근 허용: ADMIN / 본인 / 같은 팀 TEAM_LEADER / FOLDER_ACCESS 권한 보유자
     */
    @Transactional(readOnly = true)
    public Folder getReadableFolder(Long folderId, CustomUserDetails userDetails) {
        Folder folder = folderRepository.findByIdWithOwner(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "폴더를 찾을 수 없습니다."));
        checkReadAccess(folder, userDetails);
        return folder;
    }

    @Transactional(readOnly = true)
    public void checkReadAccess(Folder folder, CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        UserRole role = userDetails.getUser().getRole();

        if (role == UserRole.ADMIN) return;
        if (role == UserRole.SALES) return;   // 영업부: 전사 read-only
        if (folder.getOwner().getId().equals(userId)) return;

        if (permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                userId, PermissionType.FOLDER_ACCESS, PermissionTargetType.FOLDER, folder.getId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    /**
     * 폴더 쓰기 접근 허용: ADMIN / 본인만
     */
    @Transactional(readOnly = true)
    public void checkWriteAccess(Folder folder, CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        UserRole role = userDetails.getUser().getRole();

        if (role == UserRole.ADMIN) return;
        if (folder.getOwner().getId().equals(userId)) return;

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
    }

    @Transactional(readOnly = true)
    public boolean hasReadAccess(Long folderId, CustomUserDetails userDetails) {
        Folder folder = folderRepository.findByIdWithOwner(folderId).orElse(null);
        if (folder == null) return false;
        try {
            checkReadAccess(folder, userDetails);
            return true;
        } catch (ResponseStatusException e) {
            return false;
        }
    }
}
