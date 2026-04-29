package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.dto.ApproveUserRequest;
import com.eactive.resourcehub.user.dto.RejectUserRequest;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import com.eactive.resourcehub.user.service.AdminUserApprovalService;
import com.eactive.resourcehub.user.service.EmployeeManagementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserApprovalService approvalService;
    private final EmployeeManagementService employeeService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("pendingCount",
                userRepository.findByStatus(UserStatus.PENDING_ADMIN_APPROVAL).size());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalTeams", teamRepository.count());
        return "admin/dashboard";
    }

    // ── 승인 대기 ─────────────────────────────
    @GetMapping("/users/pending")
    public String pendingUsers(Model model) {
        model.addAttribute("pendingUsers", approvalService.findPendingUsers());
        model.addAttribute("teams", teamRepository.findAll());
        return "admin/users-pending";
    }

    @PostMapping("/users/{userId}/approve")
    public String approve(@PathVariable Long userId,
                          @ModelAttribute ApproveUserRequest req,
                          @AuthenticationPrincipal CustomUserDetails actor,
                          HttpServletRequest request,
                          RedirectAttributes ra) {
        try {
            approvalService.approve(userId, req.getTeamId(), req.getPosition(),
                    actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "사용자를 승인했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users/pending";
    }

    @PostMapping("/users/{userId}/reject")
    public String reject(@PathVariable Long userId,
                         @ModelAttribute RejectUserRequest req,
                         @AuthenticationPrincipal CustomUserDetails actor,
                         HttpServletRequest request,
                         RedirectAttributes ra) {
        try {
            approvalService.reject(userId, req.getReason(), actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "사용자를 반려했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users/pending";
    }

    // ── 직원 목록 ─────────────────────────────
    @GetMapping("/employees")
    public String employees(Model model) {
        List<User> employees = employeeService.findAllActive();
        model.addAttribute("employees", employees);
        return "admin/employees";
    }

    @GetMapping("/employees/{userId}")
    public String employeeDetail(@PathVariable Long userId, Model model) {
        User user = employeeService.findById(userId);
        model.addAttribute("user", user);
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("hasFolder", employeeService.hasPersonalFolder(userId));
        return "admin/employee-detail";
    }

    @PostMapping("/employees/{userId}/change-team")
    public String changeTeam(@PathVariable Long userId,
                             @RequestParam Long teamId,
                             @AuthenticationPrincipal CustomUserDetails actor,
                             HttpServletRequest request,
                             RedirectAttributes ra) {
        try {
            employeeService.changeTeam(userId, teamId, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "팀을 변경했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees/" + userId;
    }

    // ── 직원 문서 목록: /admin/employees/{userId}/documents ──
    @GetMapping("/employees/{userId}/documents")
    public String employeeDocuments(@PathVariable Long userId, Model model) {
        User user = employeeService.findById(userId);
        model.addAttribute("user", user);
        folderRepository.findByOwnerId(userId).ifPresent(folder -> {
            model.addAttribute("documents",
                    documentRepository.findByFolderIdAndStatusWithVersion(folder.getId(), DocumentStatus.ACTIVE));
        });
        return "admin/employee-documents";
    }
}
