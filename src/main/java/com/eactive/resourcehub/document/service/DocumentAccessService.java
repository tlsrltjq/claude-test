package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
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

    @Transactional(readOnly = true)
    public DocumentVersion getVersionWithAccessCheck(Long documentVersionId,
                                                     CustomUserDetails userDetails) {
        DocumentVersion version = documentVersionRepository
                .findByIdWithDocumentAndFolder(documentVersionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "문서 버전을 찾을 수 없습니다."));

        if (userDetails.getUser().getRole() == UserRole.ADMIN) {
            return version;
        }

        Long ownerId = version.getDocument().getFolder().getOwner().getId();
        if (!ownerId.equals(userDetails.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        return version;
    }
}
