package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/shared")
@RequiredArgsConstructor
public class SharedFolderController {

    private final PermissionRepository permissionRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    // /shared/folders
    @GetMapping("/folders")
    public String sharedFolders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();

        List<Long> folderIds = permissionRepository
                .findByUserIdAndTargetType(userId, PermissionTargetType.FOLDER)
                .stream()
                .filter(p -> p.getPermissionType() == PermissionType.FOLDER_ACCESS)
                .map(p -> p.getTargetId())
                .toList();

        List<Folder> folders = folderIds.isEmpty()
                ? List.of()
                : folderRepository.findByIdInWithOwner(folderIds);

        model.addAttribute("folders", folders);
        return "shared/folders";
    }

    // /shared/folders/{folderId}/documents
    @GetMapping("/folders/{folderId}/documents")
    public String sharedFolderDocuments(@PathVariable Long folderId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model) {
        Long userId = userDetails.getUser().getId();

        boolean hasAccess = permissionRepository.existsByUserIdAndPermissionTypeAndTargetTypeAndTargetId(
                userId, PermissionType.FOLDER_ACCESS, PermissionTargetType.FOLDER, folderId);

        if (!hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        Folder folder = folderRepository.findByIdWithOwner(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "폴더를 찾을 수 없습니다."));

        List<Document> documents = documentRepository
                .findByFolderIdAndStatusWithVersion(folderId, DocumentStatus.ACTIVE);

        model.addAttribute("folder", folder);
        model.addAttribute("documents", documents);
        return "shared/folder-documents";
    }
}
