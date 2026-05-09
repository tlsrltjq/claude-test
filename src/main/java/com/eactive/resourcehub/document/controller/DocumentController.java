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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import com.eactive.resourcehub.common.util.FileUtils;

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

        String storagePath = version.getStoragePath();
        String filename = version.getOriginalFileName();
        String ext = FileUtils.extension(filename);

        MediaType mediaType;
        if ("pdf".equals(ext)) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if ("png".equals(ext)) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (IMAGE_EXTS.contains(ext)) {
            mediaType = MediaType.IMAGE_JPEG;
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
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
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(filename, StandardCharsets.UTF_8)
                                .build().toString())
                .body(new InputStreamResource(stream));
    }

    // GET /documents/{documentVersionId}/download
    @GetMapping("/documents/{documentVersionId}/download")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable Long documentVersionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) throws IOException {

        DocumentVersion version = accessService.getVersionWithAccessCheck(documentVersionId, userDetails);

        InputStream stream;
        try {
            stream = fileStorage.load(version.getStoragePath());
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }

        auditLogService.logDownload(userDetails.getUser().getId(), documentVersionId,
                version.getOriginalFileName(), request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(version.getOriginalFileName(), StandardCharsets.UTF_8)
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

}
