package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedFolderService {

    private final PermissionRepository permissionRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<Folder> findAccessibleFolders(Long userId) {
        List<Long> folderIds = permissionRepository
                .findByUserIdAndTargetType(userId, PermissionTargetType.FOLDER)
                .stream()
                .filter(p -> p.getPermissionType() == PermissionType.FOLDER_ACCESS)
                .map(p -> p.getTargetId())
                .toList();
        return folderIds.isEmpty() ? List.of() : folderRepository.findByIdInWithOwner(folderIds);
    }

    @Transactional(readOnly = true)
    public boolean hasAccess(Long userId, Long folderId) {
        return permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                userId, PermissionType.FOLDER_ACCESS, PermissionTargetType.FOLDER, folderId);
    }

    @Transactional(readOnly = true)
    public Folder findFolderById(Long folderId) {
        return folderRepository.findByIdWithOwner(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "폴더를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Document> findFolderDocuments(Long folderId) {
        return documentRepository.findByFolderIdAndStatusWithVersion(folderId, DocumentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Folder findPublicFolder() {
        return folderRepository.findFirstByType(FolderType.SHARED_PUBLIC)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공용 폴더가 없습니다."));
    }
}
