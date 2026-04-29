package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.audit.service.AuditLogService;
import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.service.DocumentAccessService;
import com.eactive.resourcehub.document.service.ThumbnailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class DocumentController {

    private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> OFFICE_EXTS = Set.of("docx", "hwp", "hwpx");

    private final DocumentAccessService accessService;
    private final AuditLogService auditLogService;
    private final FileStorage fileStorage;
    private final ThumbnailService thumbnailService;

    // GET /documents/{documentVersionId}/preview
    @GetMapping("/documents/{documentVersionId}/preview")
    public ResponseEntity<InputStreamResource> preview(
            @PathVariable Long documentVersionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) throws IOException {

        DocumentVersion version = accessService.getVersionWithAccessCheck(documentVersionId, userDetails);

        String storagePath;
        MediaType mediaType;
        String filename = version.getOriginalFileName();

        String ext = extension(filename).toLowerCase();

        if (OFFICE_EXTS.contains(ext)) {
            if (version.getPreviewStoragePath() == null) {
                return ResponseEntity.status(415)
                        .body(null);
            }
            storagePath = version.getPreviewStoragePath();
            mediaType = MediaType.APPLICATION_PDF;
        } else if ("pdf".equals(ext)) {
            storagePath = version.getStoragePath();
            mediaType = MediaType.APPLICATION_PDF;
        } else if (IMAGE_EXTS.contains(ext)) {
            storagePath = version.getStoragePath();
            mediaType = "png".equals(ext) ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        } else {
            return ResponseEntity.status(415).body(null);
        }

        InputStream stream;
        try {
            stream = fileStorage.load(storagePath);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }

        auditLogService.logView(userDetails.getUser().getId(), documentVersionId, request);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new InputStreamResource(stream));
    }

    // GET /documents/{documentVersionId}/download/reason
    @GetMapping("/documents/{documentVersionId}/download/reason")
    public String downloadReasonForm(
            @PathVariable Long documentVersionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        DocumentVersion version = accessService.getVersionWithAccessCheck(documentVersionId, userDetails);
        model.addAttribute("version", version);
        return "document/download-reason";
    }

    // POST /documents/{documentVersionId}/download
    @PostMapping("/documents/{documentVersionId}/download")
    public Object download(
            @PathVariable Long documentVersionId,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) throws IOException {

        if (reason == null || reason.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "다운로드 사유는 최소 2자 이상 입력해야 합니다.");
            return "redirect:/documents/" + documentVersionId + "/download/reason";
        }

        DocumentVersion version = accessService.getVersionWithAccessCheck(documentVersionId, userDetails);

        InputStream stream;
        try {
            stream = fileStorage.load(version.getStoragePath());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "파일을 찾을 수 없습니다.");
            return "redirect:/my/folder";
        }

        auditLogService.logDownload(userDetails.getUser().getId(), documentVersionId,
                reason.trim(), request);

        String originalName = version.getOriginalFileName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(originalName, StandardCharsets.UTF_8)
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    // GET /documents/{documentVersionId}/thumbnail
    @GetMapping("/documents/{documentVersionId}/thumbnail")
    public ResponseEntity<InputStreamResource> thumbnail(
            @PathVariable Long documentVersionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        DocumentVersion version = accessService.getVersionWithAccessCheck(documentVersionId, userDetails);

        if (version.getThumbnailStoragePath() != null) {
            try {
                InputStream stream = fileStorage.load(version.getThumbnailStoragePath());
                String ct = version.getThumbnailContentType() != null
                        ? version.getThumbnailContentType() : "image/png";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(ct))
                        .body(new InputStreamResource(stream));
            } catch (IOException e) {
                // fall through to default
            }
        }

        // Default icon from classpath
        ClassPathResource icon = new ClassPathResource("static/images/doc-icon.png");
        if (icon.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(new InputStreamResource(icon.getInputStream()));
        }
        return ResponseEntity.notFound().build();
    }

    // POST /documents/{documentVersionId}/thumbnail/regenerate
    @PostMapping("/documents/{documentVersionId}/thumbnail/regenerate")
    public String regenerateThumbnail(
            @PathVariable Long documentVersionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            RedirectAttributes ra) {

        try {
            thumbnailService.regenerate(documentVersionId, userDetails, request);
            ra.addFlashAttribute("successMessage", "썸네일이 재생성되었습니다.");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            ra.addFlashAttribute("errorMessage", e.getReason());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "썸네일 재생성 중 오류가 발생했습니다.");
        }
        return "redirect:" + request.getHeader("Referer");
    }

    private static String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
