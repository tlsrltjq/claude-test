package com.eactive.resourcehub.team.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.team.dto.TeamRequest;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/teams")
@RequiredArgsConstructor
public class AdminTeamController {

    private final TeamService teamService;

    @GetMapping("/project-settings")
    public String projectSettings(Model model) {
        model.addAttribute("teams", teamService.findAll());
        return "admin/project-settings";
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("teamRequest", new TeamRequest());
        return "admin/teams";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute TeamRequest teamRequest,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails actor,
                         HttpServletRequest request,
                         Model model,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("teams", teamService.findAll());
            return "admin/teams";
        }
        try {
            teamService.create(teamRequest, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "팀을 생성했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/teams";
    }

    @GetMapping("/{teamId}/edit")
    public String editForm(@PathVariable Long teamId, Model model) {
        Team team = teamService.findById(teamId);
        TeamRequest teamRequest = new TeamRequest();
        teamRequest.setName(team.getName());
        teamRequest.setDescription(team.getDescription());
        model.addAttribute("team", team);
        model.addAttribute("teamRequest", teamRequest);
        return "admin/team-edit";
    }

    @PostMapping("/{teamId}/update")
    public String update(@PathVariable Long teamId,
                         @Valid @ModelAttribute TeamRequest teamRequest,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal CustomUserDetails actor,
                         HttpServletRequest request,
                         Model model,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("team", teamService.findById(teamId));
            return "admin/team-edit";
        }
        try {
            teamService.update(teamId, teamRequest, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "팀을 수정했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/teams";
    }

    @PostMapping("/{teamId}/toggle-project")
    public String toggleProject(@PathVariable Long teamId, RedirectAttributes ra) {
        boolean enabled = teamService.toggleProjectTeam(teamId);
        ra.addFlashAttribute("successMessage",
                enabled ? "인력표에 포함됩니다." : "인력표에서 제외됩니다.");
        return "redirect:/admin/teams/project-settings";
    }

    @PostMapping("/{teamId}/delete")
    public String delete(@PathVariable Long teamId,
                         @AuthenticationPrincipal CustomUserDetails actor,
                         HttpServletRequest request,
                         RedirectAttributes ra) {
        try {
            teamService.delete(teamId, actor.getUser().getId(), request);
            ra.addFlashAttribute("successMessage", "팀을 삭제했습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/teams";
    }
}
