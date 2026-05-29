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

import com.eactive.resourcehub.project.entity.ProjectStatus;
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
            @RequestParam(defaultValue = "false") boolean partial,
            @AuthenticationPrincipal CustomUserDetails details,
            Model model) {

        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month) : YearMonth.now();

        LocalDate today = LocalDate.now();

        // 캘린더 그리드용
        List<ProjectAssignment> assignments = assignmentService.getMonthlyAssignments(ym, q, project, status);
        List<Project> monthlyProjects       = projectService.getMonthlyProjects(ym);

        // 전체 프로젝트 목록 (취소 제외) + 인원 수
        List<Project> allProjects = projectService.getAllNonCancelledProjects();
        Map<Long, Long> memberCounts = assignmentService.getMemberCountsByProject();

        List<Project> activeAndPlannedProjects = allProjects.stream()
                .filter(p -> p.getStatus() != ProjectStatus.ENDED)
                .collect(Collectors.toList());
        List<Project> endedProjects = allProjects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.ENDED)
                .sorted(Comparator.comparing(Project::getEndDate).reversed())
                .collect(Collectors.toList());

        // 캘린더 공통 모델 (full + partial 공통)
        model.addAttribute("ym",                       ym);
        model.addAttribute("prev",                     ym.minusMonths(1));
        model.addAttribute("next",                     ym.plusMonths(1));
        model.addAttribute("today",                    today);
        model.addAttribute("weeks",                    CalendarGridBuilder.buildWeeks(ym));
        model.addAttribute("weekBars",                 CalendarGridBuilder.buildProjectWeekBars(monthlyProjects, ym));
        model.addAttribute("dayMap",                   CalendarGridBuilder.buildDayMap(assignments, ym));
        model.addAttribute("allAssignments",           assignments);
        model.addAttribute("monthlyProjects",          monthlyProjects);
        model.addAttribute("activeAndPlannedProjects", activeAndPlannedProjects);
        model.addAttribute("endedProjects",            endedProjects);
        model.addAttribute("memberCounts",             memberCounts);
        model.addAttribute("currentUser",              details.getUser());

        // fragment 요청(월 이동 AJAX)은 캘린더 영역만 반환
        if (partial) {
            return "sales/calendar :: calendarContent";
        }

        // Mode 2: 전체 인력 현황 (풀 페이지 로드 시만 계산)
        Map<Long, ProjectAssignment> activeAssignMap  = assignmentService.getActiveAssignmentsByUserId();
        Map<Long, ProjectAssignment> plannedAssignMap = assignmentService.getNextAssignmentsByUserId();
        List<User> assignableUsers = assignmentService.findAssignableUsers();
        List<PersonnelStatusItem> personnelStatusList = assignableUsers.stream()
                .map(u -> new PersonnelStatusItem(u,
                        activeAssignMap.get(u.getId()),
                        plannedAssignMap.get(u.getId())))
                .collect(Collectors.toList());
        long deployedCount = personnelStatusList.stream()
                .filter(i -> i.activeAssignment() != null).count();

        model.addAttribute("assignableUsers",    assignableUsers);
        model.addAttribute("personnelStatusList",personnelStatusList);
        model.addAttribute("deployedCount",      deployedCount);
        model.addAttribute("idleCount",          (long) personnelStatusList.size() - deployedCount);
        model.addAttribute("totalPersonnel",     (long) personnelStatusList.size());
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
