package com.eactive.resourcehub.template.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.template.service.ResumeTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminResumeTemplateController {

    private final ResumeTemplateService resumeTemplateService;

    @GetMapping("/admin/resume-template")
    public String page(Model model) {
        model.addAttribute("activeTemplate", resumeTemplateService.getActive().orElse(null));
        return "admin/resume-template";
    }

    @PostMapping("/admin/resume-template")
    public String upload(@RequestParam("file") MultipartFile file,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         HttpServletRequest request,
                         RedirectAttributes ra) {
        try {
            resumeTemplateService.upload(file, userDetails.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "양식 이력서가 업로드되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", "업로드 실패: " + e.getMessage());
        }
        return "redirect:/admin/resume-template";
    }
}
