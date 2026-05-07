package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.document.service.DocumentDeleteService;
import com.eactive.resourcehub.document.service.DocumentUploadService;
import com.eactive.resourcehub.permission.entity.PermissionTargetType;
import com.eactive.resourcehub.permission.entity.PermissionType;
import com.eactive.resourcehub.permission.repository.PermissionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/shared")
@RequiredArgsConstructor
public class SharedFolderController {

    private final PermissionRepository permissionRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentUploadService documentUploadService;
    private final DocumentDeleteService documentDeleteService;

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

    /** 전 사원 공용 폴더 목록 */
    @GetMapping("/folders/public")
    public String publicFolder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Folder publicFolder = folderRepository.findFirstByType(FolderType.SHARED_PUBLIC)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공용 폴더가 없습니다."));

        List<Document> documents = documentRepository
                .findByFolderIdAndStatusWithVersion(publicFolder.getId(), DocumentStatus.ACTIVE);

        model.addAttribute("folder", publicFolder);
        model.addAttribute("documents", documents);
        model.addAttribute("currentUserId", userDetails.getUser().getId());
        model.addAttribute("isAdmin", userDetails.isAdmin());
        model.addAttribute("documentTypes", Arrays.stream(DocumentType.values())
                .filter(DocumentType::isActive).toArray());
        return "shared/public-folder";
    }

    /** 공용 폴더 업로드 */
    @PostMapping("/folders/public/documents")
    public String uploadToPublicFolder(@ModelAttribute DocumentUploadRequest req,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       HttpServletRequest httpRequest,
                                       RedirectAttributes redirectAttributes) {
        Folder publicFolder = folderRepository.findFirstByType(FolderType.SHARED_PUBLIC)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공용 폴더가 없습니다."));

        try {
            documentUploadService.uploadToFolder(
                    publicFolder.getId(), userDetails.getUser().getId(), req, httpRequest);
            redirectAttributes.addFlashAttribute("successMessage", "문서가 업로드되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/shared/folders/public";
    }

    /** 공용 폴더 문서 삭제 (본인 업로드 또는 ADMIN) */
    @PostMapping("/folders/public/documents/{documentId}/delete")
    public String deletePublicDocument(@PathVariable Long documentId,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       HttpServletRequest httpRequest,
                                       RedirectAttributes redirectAttributes) {
        try {
            documentDeleteService.deletePublicFolderDocument(
                    documentId,
                    userDetails.getUser().getId(),
                    userDetails.getUser().getRole(),
                    httpRequest);
            redirectAttributes.addFlashAttribute("successMessage", "문서가 삭제되었습니다.");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
        }
        return "redirect:/shared/folders/public";
    }
}
