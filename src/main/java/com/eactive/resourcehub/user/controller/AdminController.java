package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.audit.entity.AuditLog;
import com.eactive.resourcehub.audit.service.StatisticsService;
import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.service.DocumentDeleteService;
import com.eactive.resourcehub.document.service.DocumentExpiryService;
import com.eactive.resourcehub.document.service.DocumentFileGcService;
import com.eactive.resourcehub.document.service.DocumentReviewService;
import com.eactive.resourcehub.permission.service.FolderPermissionService;
import com.eactive.resourcehub.team.service.TeamService;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.service.EmployeeManagementService;
import com.eactive.resourcehub.user.service.UserRoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import com.eactive.resourcehub.common.util.FileUtils;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeManagementService employeeService;
    private final UserRoleService userRoleService;
    private final FolderPermissionService folderPermissionService;
    private final TeamService teamService;
    private final DocumentReviewService documentReviewService;
    private final DocumentDeleteService documentDeleteService;
    private final StatisticsService statisticsService;
    private final DocumentExpiryService documentExpiryService;
    private final DocumentFileGcService documentFileGcService;
    private final SessionRegistry sessionRegistry;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", statisticsService.countUsers());
        model.addAttribute("totalTeams", teamService.count());
        model.addAttribute("pendingReviewCount",
                documentReviewService.findPendingVersions().size());
        return "admin/dashboard";
    }

    // ── 직원 목록 ─────────────────────────────
    @GetMapping("/employees")
    public String employees(@RequestParam(required = false) String q,
                            @RequestParam(required = false) String position,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) Long teamId,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        var result = employeeService.findActiveFilteredPaged(q, position, role, teamId, page);
        model.addAttribute("employees", result.getContent());
        model.addAttribute("totalCount", result.getTotalElements());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("totalPages", Math.max(1, result.getTotalPages()));
        model.addAttribute("positions", Position.values());
        model.addAttribute("roles", java.util.Arrays.stream(UserRole.values())
                .filter(r -> r != UserRole.TEAM_LEADER).toList());
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("position", position);
        model.addAttribute("role", role);
        model.addAttribute("teamId", teamId);
        return "admin/employees";
    }

    @GetMapping("/employees/{userId}")
    public String employeeDetail(@PathVariable Long userId,
                                 @RequestParam(defaultValue = "info") String tab,
                                 Model model) {
        model.addAttribute("activeTab", tab);
        User user = employeeService.findById(userId);
        model.addAttribute("user", user);
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("hasFolder", employeeService.hasPersonalFolder(userId));

        model.addAttribute("roles", java.util.Arrays.stream(UserRole.values())
                .filter(r -> r != UserRole.TEAM_LEADER).toList());

        model.addAttribute("documents", employeeService.findPersonalDocuments(userId));

        model.addAttribute("permissions", folderPermissionService.findPermissionsByUser(userId));
        model.addAttribute("grantableFolders", folderPermissionService.findGrantableFolders(userId));

        return "admin/employee-detail";
    }

    @PostMapping("/employees/{userId}/toggle-status")
    public String toggleStatus(@PathVariable Long userId,
                               @AuthenticationPrincipal CustomUserDetails actor,
                               HttpServletRequest request,
                               RedirectAttributes ra) {
        try {
            UserStatus newStatus = employeeService.toggleStatus(userId, actor.getUser().getId(), request);
            if (newStatus == UserStatus.DISABLED) {
                for (Object principal : sessionRegistry.getAllPrincipals()) {
                    if (principal instanceof CustomUserDetails cud
                            && cud.getUser().getId().equals(userId)) {
                        sessionRegistry.getAllSessions(principal, false)
                                .forEach(SessionInformation::expireNow);
                        break;
                    }
                }
                ra.addFlashAttribute("successMessage", "계정을 비활성화하고 세션을 종료했습니다.");
            } else {
                ra.addFlashAttribute("successMessage", "계정을 활성화했습니다.");
            }
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees/" + userId + "?tab=info";
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
        return "redirect:/admin/employees/" + userId + "?tab=team";
    }

    // ── 직원 문서 목록: /admin/employees/{userId}/documents ──
    @GetMapping("/employees/{userId}/documents")
    public String employeeDocuments(@PathVariable Long userId, Model model) {
        User user = employeeService.findById(userId);
        model.addAttribute("user", user);
        model.addAttribute("documents", employeeService.findPersonalDocuments(userId));
        return "admin/employee-documents";
    }

    // ── 직원 문서 상세: /admin/employees/{userId}/documents/{documentId} ──
    @GetMapping("/employees/{userId}/documents/{documentId}")
    public String employeeDocumentDetail(@PathVariable Long userId,
                                         @PathVariable Long documentId,
                                         Model model) {
        User user = employeeService.findById(userId);
        var document = employeeService.findDocumentDetail(documentId);
        var versions = employeeService.findDocumentVersions(documentId);
        DocumentVersion currentVersion = document.getCurrentVersion() != null
                ? document.getCurrentVersion() : (versions.isEmpty() ? null : versions.get(0));

        model.addAttribute("user", user);
        model.addAttribute("document", document);
        model.addAttribute("currentVersion", currentVersion);
        model.addAttribute("versions", versions);
        model.addAttribute("previewType", resolvePreviewType(currentVersion));
        return "admin/employee-document-detail";
    }

    // ── 문서 삭제: /admin/documents/{documentId}/delete ──────────
    @PostMapping("/documents/{documentId}/delete")
    public String deleteDocument(@PathVariable Long documentId,
                                 @AuthenticationPrincipal CustomUserDetails actor,
                                 HttpServletRequest request,
                                 RedirectAttributes ra) {
        try {
            documentDeleteService.deleteDocument(documentId, actor, request);
            ra.addFlashAttribute("successMessage", "문서가 삭제되었습니다.");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            ra.addFlashAttribute("errorMessage", e.getReason());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "문서 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:" + (request.getHeader("Referer") != null
                ? request.getHeader("Referer") : "/admin");
    }

    // ── 역할 변경: /admin/users/{userId}/role ──────────────────
    @GetMapping("/users/{userId}/role")
    public String roleForm(@PathVariable Long userId, Model model) {
        User user = employeeService.findById(userId);
        model.addAttribute("user", user);
        model.addAttribute("roles", java.util.Arrays.stream(UserRole.values())
                .filter(r -> r != UserRole.TEAM_LEADER)
                .toList());
        return "admin/user-role";
    }

    @PostMapping("/users/{userId}/role")
    public String changeRole(@PathVariable Long userId,
                             @RequestParam UserRole role,
                             @AuthenticationPrincipal CustomUserDetails actor,
                             HttpServletRequest request,
                             RedirectAttributes ra) {
        try {
            userRoleService.changeRole(userId, role, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "역할을 변경했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees/" + userId + "?tab=role";
    }

    // ── 권한 관리: /admin/users/{userId}/permissions ────────────
    @GetMapping("/users/{userId}/permissions")
    public String permissionsForm(@PathVariable Long userId, Model model) {
        model.addAttribute("user", employeeService.findById(userId));
        model.addAttribute("permissions", folderPermissionService.findPermissionsByUser(userId));
        model.addAttribute("grantableFolders", folderPermissionService.findGrantableFolders(userId));
        return "admin/user-permissions";
    }

    @PostMapping("/users/{userId}/permissions/grant")
    public String grantPermission(@PathVariable Long userId,
                                  @RequestParam Long folderId,
                                  @AuthenticationPrincipal CustomUserDetails actor,
                                  HttpServletRequest request,
                                  RedirectAttributes ra) {
        try {
            folderPermissionService.grant(userId, folderId, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "권한을 부여했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees/" + userId + "?tab=permissions";
    }

    @PostMapping("/users/{userId}/permissions/revoke")
    public String revokePermission(@PathVariable Long userId,
                                   @RequestParam Long folderId,
                                   @AuthenticationPrincipal CustomUserDetails actor,
                                   HttpServletRequest request,
                                   RedirectAttributes ra) {
        try {
            folderPermissionService.revoke(userId, folderId, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "권한을 회수했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/employees/" + userId + "?tab=permissions";
    }

    // ── 문서 검토: /admin/documents/review ──────────────────────
    @GetMapping("/documents/review")
    public String reviewList(Model model) {
        model.addAttribute("pendingVersions", documentReviewService.findPendingVersions());
        return "admin/documents-review";
    }

    @GetMapping("/documents/review/{documentVersionId}")
    public String reviewDetail(@PathVariable Long documentVersionId, Model model) {
        DocumentVersion version = documentReviewService.findVersionForReview(documentVersionId);
        model.addAttribute("version", version);
        model.addAttribute("previewType", resolvePreviewType(version));
        return "admin/document-review-detail";
    }

    @PostMapping("/documents/review/{documentVersionId}/approve")
    public String approve(@PathVariable Long documentVersionId,
                          @AuthenticationPrincipal CustomUserDetails actor,
                          HttpServletRequest request,
                          RedirectAttributes ra) {
        try {
            documentReviewService.approve(documentVersionId, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "문서를 승인했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/documents/review";
    }

    @PostMapping("/documents/review/{documentVersionId}/reject")
    public String rejectDocument(@PathVariable Long documentVersionId,
                                 @RequestParam String reason,
                                 @AuthenticationPrincipal CustomUserDetails actor,
                                 HttpServletRequest request,
                                 RedirectAttributes ra) {
        try {
            documentReviewService.reject(documentVersionId, reason, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "문서를 반려했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/documents/review";
    }

    // ── 만료 현황: /admin/documents/expiry ──────────────────────
    @GetMapping("/documents/expiry")
    public String expiryDashboard(Model model) {
        model.addAttribute("expiredDocuments", documentExpiryService.findExpired());
        model.addAttribute("expiringSoonDocuments", documentExpiryService.findExpiringSoon());
        return "admin/documents-expiry";
    }

    // ── 통계: /admin/statistics ──────────────────────────────────
    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("downloadStats", statisticsService.getDownloadStats());
        model.addAttribute("topDocuments", statisticsService.getTopDownloadedDocuments(10));
        model.addAttribute("recentDownloads", statisticsService.findRecentDownloads(30));
        return "admin/statistics";
    }

    // ── GC 대시보드: /admin/gc ────────────────────────────────────
    @GetMapping("/gc")
    public String gcDashboard(Model model) {
        model.addAttribute("retentionDays", documentFileGcService.getRetentionDays());
        return "admin/gc";
    }

    @PostMapping("/gc/run")
    public String runGc(RedirectAttributes redirectAttributes) {
        int count = documentFileGcService.runGc();
        redirectAttributes.addFlashAttribute("gcResult",
                count == 0 ? "정리 대상 없음" : count + "건 처리 완료");
        return "redirect:/admin/gc";
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

}
