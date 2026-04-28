package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.document.service.DocumentUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/my/folder")
@RequiredArgsConstructor
public class MyFolderController {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentUploadService documentUploadService;

    @GetMapping
    public String myFolder(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        Folder folder = folderRepository.findByOwnerId(userId).orElse(null);
        if (folder == null) {
            model.addAttribute("errorMessage", "개인 폴더가 없습니다. 관리자에게 문의하세요.");
            return "my/folder";
        }
        List<Document> documents = documentRepository
                .findByFolderIdAndStatusWithVersion(folder.getId(), DocumentStatus.ACTIVE);
        model.addAttribute("folder", folder);
        model.addAttribute("documents", documents);
        return "my/folder";
    }

    @GetMapping("/documents/upload")
    public String uploadForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        if (folderRepository.findByOwnerId(userId).isEmpty()) {
            return "redirect:/my/folder";
        }
        model.addAttribute("documentTypes", DocumentType.values());
        model.addAttribute("uploadRequest", new DocumentUploadRequest());
        return "my/upload";
    }

    @PostMapping("/documents")
    public String upload(@ModelAttribute DocumentUploadRequest req,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         HttpServletRequest httpRequest,
                         RedirectAttributes redirectAttributes) {
        try {
            documentUploadService.upload(userDetails.getUser().getId(), req, httpRequest);
            redirectAttributes.addFlashAttribute("successMessage", "문서가 업로드되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my/folder/documents/upload";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "업로드 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/my/folder/documents/upload";
        }
        return "redirect:/my/folder";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSize(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "파일 크기가 20MB를 초과합니다.");
        return "redirect:/my/folder/documents/upload";
    }
}
