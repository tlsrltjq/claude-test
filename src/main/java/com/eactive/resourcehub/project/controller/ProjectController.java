package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.project.dto.ProjectCreateRequest;
import com.eactive.resourcehub.project.dto.ProjectMemberRequest;
import com.eactive.resourcehub.project.dto.ProjectUpdateRequest;
import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectStatus;
import com.eactive.resourcehub.project.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ── 프로젝트 상세 페이지 (ADMIN·SALES) ──────────────────────────

    @GetMapping("/sales/projects/{id}")
    public String detailPage(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails details,
            Model model) {

        Project project = projectService.findById(id);
        model.addAttribute("project",         project);
        model.addAttribute("members",         projectService.getMembersForProject(id));
        model.addAttribute("assignableUsers", projectService.findAssignableUsers());
        model.addAttribute("allStatuses",     AssignmentStatus.values());
        model.addAttribute("projectStatuses", ProjectStatus.values());
        model.addAttribute("currentUser",     details.getUser());
        return "sales/project-detail";
    }

    // ── 프로젝트 CRUD (ADMIN·SALES — 서비스에서 역할 검증) ──────────

    @PostMapping("/sales/projects")
    public String create(
            @ModelAttribute ProjectCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            Project saved = projectService.create(req, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "프로젝트가 등록되었습니다.");
            return "redirect:/sales/projects/" + saved.getId();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/sales/calendar";
        }
    }

    @PostMapping("/sales/projects/{id}/update")
    public String update(
            @PathVariable Long id,
            @ModelAttribute ProjectUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            projectService.update(id, req, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "프로젝트 정보가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/projects/" + id;
    }

    @PostMapping("/sales/projects/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            projectService.delete(id, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "프로젝트가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/calendar";
    }

    // ── 멤버 관리 (ADMIN·SALES) ─────────────────────────────────────

    @PostMapping("/sales/projects/{id}/members")
    public String addMember(
            @PathVariable Long id,
            @ModelAttribute ProjectMemberRequest req,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            projectService.addMember(id, req, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "멤버가 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/projects/" + id;
    }

    @PostMapping("/sales/projects/{id}/members/{aId}/update")
    public String updateMember(
            @PathVariable Long id,
            @PathVariable Long aId,
            @ModelAttribute ProjectMemberRequest req,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            projectService.updateMember(id, aId, req, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "투입 정보가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/projects/" + id;
    }

    @PostMapping("/sales/projects/{id}/members/{aId}/delete")
    public String removeMember(
            @PathVariable Long id,
            @PathVariable Long aId,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            projectService.removeMember(id, aId, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "멤버가 제거되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/projects/" + id;
    }
}
