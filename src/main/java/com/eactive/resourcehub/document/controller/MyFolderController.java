package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.document.service.DocumentUploadService;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/my/folder")
@RequiredArgsConstructor
public class MyFolderController {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
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

    @GetMapping("/documents/{documentId}")
    public String documentDetail(@PathVariable Long documentId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Long userId = userDetails.getUser().getId();
        Document document = documentRepository.findByIdForDetail(documentId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));

        if (!document.getFolder().getOwner().getId().equals(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<DocumentVersion> versions = documentVersionRepository
                .findByDocumentIdOrderByVersionNoDesc(documentId);
        DocumentVersion currentVersion = document.getCurrentVersion() != null
                ? document.getCurrentVersion() : (versions.isEmpty() ? null : versions.get(0));

        model.addAttribute("document", document);
        model.addAttribute("currentVersion", currentVersion);
        model.addAttribute("versions", versions);
        model.addAttribute("previewType", resolvePreviewType(currentVersion));
        return "my/document-detail";
    }

    private String resolvePreviewType(DocumentVersion version) {
        if (version == null) return "none";
        String ext = extension(version.getOriginalFileName()).toLowerCase();
        if ("pdf".equals(ext)) return "pdf";
        if (Set.of("jpg", "jpeg", "png").contains(ext)) return "image";
        if (Set.of("docx", "hwp", "hwpx").contains(ext))
            return version.getPreviewStoragePath() != null ? "pdf" : "none";
        return "none";
    }

    private static String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSize(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "파일 크기가 20MB를 초과합니다.");
        return "redirect:/my/folder/documents/upload";
    }
}
