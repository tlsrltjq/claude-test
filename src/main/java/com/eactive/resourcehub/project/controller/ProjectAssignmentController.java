package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.project.dto.ProjectAssignmentRequest;
import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.service.ProjectAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProjectAssignmentController {

    private final ProjectAssignmentService assignmentService;

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

        model.addAttribute("ym",             ym);
        model.addAttribute("prev",           ym.minusMonths(1));
        model.addAttribute("next",           ym.plusMonths(1));
        model.addAttribute("today",          LocalDate.now());
        model.addAttribute("weeks",          buildCalendarWeeks(ym));
        model.addAttribute("dayMap",         buildDayMap(assignments, ym));
        model.addAttribute("allAssignments", assignments);
        model.addAttribute("allStatuses",    AssignmentStatus.values());
        model.addAttribute("assignableUsers", assignmentService.findAssignableUsers());
        model.addAttribute("currentUser",    details.getUser());
        model.addAttribute("q",              q != null ? q : "");
        model.addAttribute("project",        project != null ? project : "");
        model.addAttribute("filterStatus",   status);
        return "sales/calendar";
    }

    // ── 배정 CRUD (ADMIN 전용 — 서비스에서 역할 검증) ────────────

    @PostMapping("/sales/assignments")
    public String create(
            @ModelAttribute ProjectAssignmentRequest req,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            List<ProjectAssignment> overlaps = assignmentService.checkOverlap(
                    req.getUserId(), req.getStartDate(), req.getEndDate(), null);
            if (!overlaps.isEmpty()) {
                ra.addFlashAttribute("overlapWarning",
                        "겹치는 배정이 " + overlaps.size() + "건 있습니다. 확인 후 조정하세요.");
            }
            assignmentService.create(req, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "배정이 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/calendar";
    }

    @PostMapping("/sales/assignments/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute ProjectAssignmentRequest req,
            @AuthenticationPrincipal CustomUserDetails details,
            HttpServletRequest httpReq,
            RedirectAttributes ra) {
        try {
            List<ProjectAssignment> overlaps = assignmentService.checkOverlap(
                    req.getUserId(), req.getStartDate(), req.getEndDate(), id);
            if (!overlaps.isEmpty()) {
                ra.addFlashAttribute("overlapWarning",
                        "겹치는 배정이 " + overlaps.size() + "건 있습니다.");
            }
            assignmentService.update(id, req, details.getUser().getId(),
                    details.getUser().getRole(), httpReq);
            ra.addFlashAttribute("success", "배정이 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales/calendar";
    }

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

    // ── 캘린더 그리드 빌더 (프레젠테이션 로직, 컨트롤러에 위치) ──

    /**
     * 해당 월의 주(week) 목록을 반환.
     * 각 주는 7개의 LocalDate(null=해당 월 밖 날짜)로 구성. 일요일 시작.
     */
    private static List<List<LocalDate>> buildCalendarWeeks(YearMonth ym) {
        LocalDate firstDay = ym.atDay(1);
        // DayOfWeek: MON=1..SUN=7. 일요일 시작: SUN→0, MON→1, ..., SAT→6
        int offset = firstDay.getDayOfWeek().getValue() % 7;

        List<List<LocalDate>> weeks = new ArrayList<>();
        List<LocalDate> week = new ArrayList<>(Collections.nCopies(offset, null));

        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            week.add(ym.atDay(d));
            if (week.size() == 7) {
                weeks.add(new ArrayList<>(week));
                week = new ArrayList<>();
            }
        }
        if (!week.isEmpty()) {
            while (week.size() < 7) week.add(null);
            weeks.add(week);
        }
        return weeks;
    }

    /**
     * 날짜 → 해당 날짜에 기간이 겹치는 배정 목록. CANCELLED는 캘린더 그리드에서 제외.
     */
    private static Map<LocalDate, List<ProjectAssignment>> buildDayMap(
            List<ProjectAssignment> assignments, YearMonth ym) {
        Map<LocalDate, List<ProjectAssignment>> map = new LinkedHashMap<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            map.put(ym.atDay(d), new ArrayList<>());
        }
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();

        for (ProjectAssignment pa : assignments) {
            if (pa.getStatus() == AssignmentStatus.CANCELLED) continue;
            LocalDate from = pa.getStartDate().isBefore(monthStart) ? monthStart : pa.getStartDate();
            LocalDate to   = pa.getEndDate().isAfter(monthEnd)      ? monthEnd   : pa.getEndDate();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                List<ProjectAssignment> dayList = map.get(d);
                if (dayList != null) dayList.add(pa);
            }
        }
        return map;
    }
}
