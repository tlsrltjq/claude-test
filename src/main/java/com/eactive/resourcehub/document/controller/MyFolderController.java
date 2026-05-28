package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.service.DocumentDeleteService;
import com.eactive.resourcehub.document.service.DocumentPreviewResolver;
import com.eactive.resourcehub.document.service.DocumentUploadService;
import com.eactive.resourcehub.document.service.MyFolderService;
import com.eactive.resourcehub.template.service.ResumeTemplateService;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/my/folder")
@RequiredArgsConstructor
public class MyFolderController {

    private final MyFolderService myFolderService;
    private final DocumentUploadService documentUploadService;
    private final DocumentDeleteService documentDeleteService;
    private final ResumeTemplateService resumeTemplateService;
    private final DocumentPreviewResolver previewResolver;

    @GetMapping
    public String myFolder(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        Folder folder = myFolderService.findPersonalFolder(userId).orElse(null);
        if (folder == null) {
            model.addAttribute("errorMessage", "개인 폴더가 없습니다. 관리자에게 문의하세요.");
            return "my/folder";
        }
        List<Document> documents = myFolderService.findActiveDocuments(folder.getId());
        Map<Long, DocumentVersion> latestVersions = myFolderService.findLatestVersionMap(documents);
        model.addAttribute("folder", folder);
        model.addAttribute("documents", documents);
        model.addAttribute("latestVersions", latestVersions);
        model.addAttribute("activeResumeTemplate", resumeTemplateService.getActive().orElse(null));
        return "my/folder";
    }

    @GetMapping("/documents/upload")
    public String uploadForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        if (myFolderService.findPersonalFolder(userId).isEmpty()) {
            return "redirect:/my/folder";
        }
        model.addAttribute("documentTypes", Arrays.stream(DocumentType.values())
                .filter(DocumentType::isActive).toArray());
        model.addAttribute("uploadRequest", new DocumentUploadRequest());
        return "my/upload";
    }

    @PostMapping("/documents")
    public Object upload(@ModelAttribute DocumentUploadRequest req,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         HttpServletRequest httpRequest,
                         RedirectAttributes redirectAttributes) {
        boolean isXhr = "XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"));
        try {
            documentUploadService.upload(userDetails.getUser().getId(), req, httpRequest);
            if (isXhr) {
                return ResponseEntity.ok(Map.of("success", true, "message", "문서가 업로드되었습니다."));
            }
            redirectAttributes.addFlashAttribute("successMessage", "문서가 업로드되었습니다.");
            return "redirect:/my/folder";
        } catch (IllegalArgumentException e) {
            if (isXhr) {
                return ResponseEntity.unprocessableEntity()
                        .body(Map.of("success", false, "message", e.getMessage()));
            }
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my/folder/documents/upload";
        } catch (Exception e) {
            if (isXhr) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("success", false, "message", "업로드 중 오류가 발생했습니다."));
            }
            redirectAttributes.addFlashAttribute("errorMessage", "업로드 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/my/folder/documents/upload";
        }
    }

    @GetMapping("/documents/{documentId}")
    public String documentDetail(@PathVariable Long documentId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Long userId = userDetails.getUser().getId();
        Document document = myFolderService.findDocumentDetail(documentId);

        UserRole role = userDetails.getUser().getRole();
        boolean isOwner = document.getFolder().getOwner().getId().equals(userId);
        if (role != UserRole.ADMIN && !isOwner) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<DocumentVersion> versions = myFolderService.findDocumentVersions(documentId);
        DocumentVersion currentVersion = versions.isEmpty() ? null : versions.get(0);

        model.addAttribute("document", document);
        model.addAttribute("currentVersion", currentVersion);
        model.addAttribute("versions", versions);
        model.addAttribute("previewType", previewResolver.resolve(currentVersion));
        model.addAttribute("isOwner", isOwner);
        return "my/document-detail";
    }

    @PostMapping("/documents/{documentId}/expiry")
    public String updateExpiry(@PathVariable Long documentId,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        myFolderService.updateExpiry(documentId, userDetails.getUser().getId(), expiresAt);
        redirectAttributes.addFlashAttribute("successMessage", "만료일이 수정되었습니다.");
        return "redirect:/my/folder/documents/" + documentId;
    }

    @PostMapping("/documents/{documentId}/delete")
    public String deleteOwnDocument(@PathVariable Long documentId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            documentDeleteService.deleteOwnDocument(documentId, userDetails.getUser().getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "문서가 삭제되었습니다.");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
        }
        return "redirect:/my/folder";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxSize(MaxUploadSizeExceededException e,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("success", false, "message", "파일 크기가 20MB를 초과합니다."));
        }
        redirectAttributes.addFlashAttribute("errorMessage", "파일 크기가 20MB를 초과합니다.");
        return "redirect:/my/folder/documents/upload";
    }
}
