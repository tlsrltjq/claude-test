package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.employee.service.CareerSaveService;
import com.eactive.resourcehub.project.service.ProjectAssignmentService;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final SettingsService settingsService;
    private final CareerSaveService careerSaveService;
    private final ProjectAssignmentService projectAssignmentService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = settingsService.getUser(userDetails.getUser().getId());
        model.addAttribute("user", user);

        careerSaveService.findProfile(user.getId())
                .ifPresent(ep -> model.addAttribute("profile", ep));

        model.addAttribute("deployStats",  projectAssignmentService.getDeployStats());
        model.addAttribute("endingSoon",   projectAssignmentService.findEndingSoon(14));

        return "dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
