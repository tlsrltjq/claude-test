package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.DocumentReviewStatus;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
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
public class DocumentAccessService {

    private final DocumentVersionRepository documentVersionRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public DocumentVersion getVersionWithAccessCheck(Long documentVersionId,
                                                     CustomUserDetails userDetails) {
        DocumentVersion version = documentVersionRepository
                .findByIdWithDocumentAndFolder(documentVersionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "문서 버전을 찾을 수 없습니다."));

        checkReadAccess(version.getDocument().getFolder(), userDetails);
        checkReviewStatusAccess(version, userDetails);
        return version;
    }

    public void checkReadAccess(Folder folder, CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        UserRole role = userDetails.getUser().getRole();

        if (role == UserRole.ADMIN) return;
        if (role == UserRole.SALES) return;
        if (folder.isPublic()) return;  // 전 사원 공용 폴더는 모든 인증 사용자 접근 허용
        if (folder.getOwner().getId().equals(userId)) return;

        if (permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                userId, PermissionType.FOLDER_ACCESS,
                PermissionTargetType.FOLDER, folder.getId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    /**
     * 팀장/개별 권한자는 APPROVED 버전만 접근 가능.
     * ADMIN/본인은 모든 상태 접근 가능.
     */
    public void checkReviewStatusAccess(DocumentVersion version, CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        UserRole role = userDetails.getUser().getRole();

        if (role == UserRole.ADMIN) return;
        if (role == UserRole.SALES) return;   // 영업부: 승인 상태 제한 없음
        if (version.getDocument().getFolder().getOwner().getId().equals(userId)) return;

        if (version.getReviewStatus() != DocumentReviewStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "승인된 문서만 접근할 수 있습니다.");
        }
    }
}
