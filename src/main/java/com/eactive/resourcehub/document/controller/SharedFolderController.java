package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.service.DocumentDeleteService;
import com.eactive.resourcehub.document.service.DocumentUploadService;
import com.eactive.resourcehub.document.service.SharedFolderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/shared")
@RequiredArgsConstructor
public class SharedFolderController {

    private final SharedFolderService sharedFolderService;
    private final DocumentUploadService documentUploadService;
    private final DocumentDeleteService documentDeleteService;

    /** 전 사원 공용 폴더 목록 */
    @GetMapping("/folders/public")
    public String publicFolder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Folder publicFolder = sharedFolderService.findPublicFolder();
        model.addAttribute("folder", publicFolder);
        model.addAttribute("documents", sharedFolderService.findFolderDocuments(publicFolder.getId()));
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
        Folder publicFolder = sharedFolderService.findPublicFolder();
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
