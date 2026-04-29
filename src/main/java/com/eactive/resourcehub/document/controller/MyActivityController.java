package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditLog;
import com.eactive.resourcehub.audit.repository.AuditLogRepository;
import com.eactive.resourcehub.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/my/activity")
@RequiredArgsConstructor
public class MyActivityController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public String myActivity(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        Long userId = userDetails.getUser().getId();
        Page<AuditLog> downloadPage = auditLogRepository.findByActionTypeAndUserIdWithUser(
                AuditActionType.DOWNLOAD, userId, PageRequest.of(page, 20));

        model.addAttribute("downloads", downloadPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", downloadPage.getTotalPages());
        model.addAttribute("totalElements", downloadPage.getTotalElements());
        return "my/activity";
    }
}
