package com.eactive.resourcehub.template.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.template.service.ResumeTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ResumeTemplateDownloadController {

    private final ResumeTemplateService resumeTemplateService;

    @GetMapping("/my/folder/resume-template/download")
    public ResponseEntity<Resource> downloadFromMyFolder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        try {
            return resumeTemplateService.download(userDetails.getUser().getId(), request);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/sales/resume-template/download")
    public ResponseEntity<Resource> downloadFromSales(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        try {
            return resumeTemplateService.download(userDetails.getUser().getId(), request);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
