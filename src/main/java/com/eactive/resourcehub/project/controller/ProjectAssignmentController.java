package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.service.ProjectAssignmentService;
import com.eactive.resourcehub.project.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eactive.resourcehub.user.entity.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProjectAssignmentController {

    private final ProjectAssignmentService assignmentService;
    private final ProjectService projectService;

    // ── 캘린더 페이지 ─────────────────────────────────────────────

    @GetMapping("/sales/calendar")
    public String calendarPage(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String project,
            @RequestParam(required = false) AssignmentStatus status,
            @AuthenticationPrincipal CustomUserDetails details,
            Model model) {

        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month) : YearMonth.now();

        List<ProjectAssignment> assignments =
                assignmentService.getMonthlyAssignments(ym, q, project, status);
        List<Project> monthlyProjects = projectService.getMonthlyProjects(ym);

        // 프로젝트 리스트용: 필터 무관 전체 배정을 프로젝트별로 묶어 대표자 산출
        List<ProjectAssignment> allMonthly =
                assignmentService.getMonthlyAssignments(ym, null, null, null);
        Map<Long, List<ProjectAssignment>> byProject = allMonthly.stream()
                .collect(Collectors.groupingBy(pa -> pa.getProject().getId()));
        List<ProjectListItem> projectListItems = monthlyProjects.stream()
                .map(p -> {
                    List<ProjectAssignment> members = byProject.getOrDefault(p.getId(), List.of());
                    User lead = members.stream()
                            .map(ProjectAssignment::getUser)
                            .filter(u -> u.getPosition() != null)
                            .min(Comparator.comparingInt(u -> u.getPosition().ordinal()))
                            .orElse(null);
                    return new ProjectListItem(p, lead, members.size());
                })
                .collect(Collectors.toList());

        model.addAttribute("ym",               ym);
        model.addAttribute("prev",             ym.minusMonths(1));
        model.addAttribute("next",             ym.plusMonths(1));
        model.addAttribute("today",            LocalDate.now());
        model.addAttribute("weeks",            CalendarGridBuilder.buildWeeks(ym));
        model.addAttribute("weekBars",         CalendarGridBuilder.buildProjectWeekBars(monthlyProjects, ym));
        model.addAttribute("dayMap",           CalendarGridBuilder.buildDayMap(assignments, ym));
        model.addAttribute("allAssignments",   assignments);
        model.addAttribute("monthlyProjects",  monthlyProjects);
        model.addAttribute("projectListItems", projectListItems);
        model.addAttribute("allStatuses",      AssignmentStatus.values());
        model.addAttribute("assignableUsers",  assignmentService.findAssignableUsers());
        model.addAttribute("currentUser",      details.getUser());
        model.addAttribute("q",                q != null ? q : "");
        model.addAttribute("project",          project != null ? project : "");
        model.addAttribute("filterStatus",     status);
        return "sales/calendar";
    }

    // ── 배정 삭제 (ADMIN 전용 — 서비스에서 역할 검증) ────────────

    @PostMapping("/sales/assignments/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            assignmentService.delete(id, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "배정이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/calendar";
    }
}
