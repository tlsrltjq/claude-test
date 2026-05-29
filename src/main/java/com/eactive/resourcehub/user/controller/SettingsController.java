package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.team.service.TeamService;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final TeamService teamService;

    @GetMapping
    public String settingsPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(defaultValue = "info") String tab,
                               Model model) {
        User user = settingsService.getUser(userDetails.getUser().getId());
        model.addAttribute("user", user);
        model.addAttribute("tab", tab);
        model.addAttribute("teams", teamService.findAll());
        return "settings";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinDate,
                                RedirectAttributes redirectAttributes) {
        settingsService.updateProfile(userDetails.getUser().getId(), name, phone, birthDate, address, joinDate);
        redirectAttributes.addFlashAttribute("successMessage", "개인정보가 저장되었습니다.");
        return "redirect:/settings?tab=profile";
    }

    @PostMapping("/team")
    public String updateTeam(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam(required = false) Long teamId,
                             RedirectAttributes redirectAttributes) {
        settingsService.updateTeam(userDetails.getUser().getId(), teamId);
        redirectAttributes.addFlashAttribute("successMessage", "소속 팀이 변경되었습니다.");
        return "redirect:/settings?tab=profile";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "새 비밀번호가 일치하지 않습니다.");
            return "redirect:/settings?tab=password";
        }
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호는 8자 이상이어야 합니다.");
            return "redirect:/settings?tab=password";
        }
        boolean changed = settingsService.changePassword(userDetails.getUser().getId(), currentPassword, newPassword);
        if (!changed) {
            redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 올바르지 않습니다.");
            return "redirect:/settings?tab=password";
        }
        redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다.");
        return "redirect:/settings?tab=password";
    }
}
