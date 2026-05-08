package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.document.service.DocumentDeleteService;
import com.eactive.resourcehub.document.service.DocumentUploadService;
import com.eactive.resourcehub.template.service.ResumeTemplateService;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import com.eactive.resourcehub.common.util.FileUtils;

@Controller
@RequestMapping("/my/folder")
@RequiredArgsConstructor
public class MyFolderController {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentUploadService documentUploadService;
    private final DocumentDeleteService documentDeleteService;
    private final ResumeTemplateService resumeTemplateService;

    @GetMapping
    public String myFolder(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        Folder folder = folderRepository.findByOwnerIdAndType(userId, FolderType.PERSONAL).orElse(null);
        if (folder == null) {
            model.addAttribute("errorMessage", "개인 폴더가 없습니다. 관리자에게 문의하세요.");
            return "my/folder";
        }
        // 본인은 모든 상태 문서 조회 (currentVersion이 없는 PENDING 문서 포함)
        List<Document> documents = documentRepository.findByFolderIdAndStatus(folder.getId(), DocumentStatus.ACTIVE);
        // 각 문서의 최신 버전을 별도 조회 (review status 포함)
        Map<Long, DocumentVersion> latestVersions = new HashMap<>();
        for (Document doc : documents) {
            documentVersionRepository.findFirstByDocumentIdOrderByVersionNoDesc(doc.getId())
                    .ifPresent(v -> latestVersions.put(doc.getId(), v));
        }
        model.addAttribute("folder", folder);
        model.addAttribute("documents", documents);
        model.addAttribute("latestVersions", latestVersions);
        model.addAttribute("activeResumeTemplate", resumeTemplateService.getActive().orElse(null));
        return "my/folder";
    }

    @GetMapping("/documents/upload")
    public String uploadForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        if (folderRepository.findByOwnerIdAndType(userId, FolderType.PERSONAL).isEmpty()) {
            return "redirect:/my/folder";
        }
        model.addAttribute("documentTypes", Arrays.stream(DocumentType.values())
                .filter(DocumentType::isActive).toArray());
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
        Document document = documentRepository.findByIdForDetailWithTags(documentId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."));

        if (!document.getFolder().getOwner().getId().equals(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<DocumentVersion> versions = documentVersionRepository
                .findByDocumentIdOrderByVersionNoDesc(documentId);
        // 본인은 최신 버전을 currentVersion으로 표시 (pending/rejected 포함)
        DocumentVersion currentVersion = versions.isEmpty() ? null : versions.get(0);

        model.addAttribute("document", document);
        model.addAttribute("currentVersion", currentVersion);
        model.addAttribute("versions", versions);
        model.addAttribute("previewType", resolvePreviewType(currentVersion));
        return "my/document-detail";
    }

    private String resolvePreviewType(DocumentVersion version) {
        if (version == null) return "none";
        String ext = FileUtils.extension(version.getOriginalFileName());
        if ("pdf".equals(ext)) return "pdf";
        if (Set.of("jpg", "jpeg", "png").contains(ext)) return "image";
        if (Set.of("docx", "hwp", "hwpx").contains(ext))
            return version.getPreviewStoragePath() != null ? "pdf" : "none";
        return "none";
    }

    @PostMapping("/documents/{documentId}/expiry")
    public String updateExpiry(@PathVariable Long documentId,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUser().getId();
        Document document = documentRepository.findByIdForDetail(documentId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND));
        if (!document.getFolder().getOwner().getId().equals(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        document.updateExpiresAt(expiresAt);
        documentRepository.save(document);
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
    public String handleMaxSize(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "파일 크기가 20MB를 초과합니다.");
        return "redirect:/my/folder/documents/upload";
    }
}
