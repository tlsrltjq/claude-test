package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.employee.entity.EmployeeProfile;
import com.eactive.resourcehub.employee.repository.EmployeeProfileRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userRepository.findByIdWithTeam(userDetails.getUser().getId())
                .orElse(userDetails.getUser());
        model.addAttribute("user", user);

        employeeProfileRepository.findByUserId(user.getId())
                .ifPresent(ep -> model.addAttribute("profile", ep));

        return "dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
