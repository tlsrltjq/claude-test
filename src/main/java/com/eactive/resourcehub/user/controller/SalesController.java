package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.team.service.TeamService;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.service.SalesMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesMemberService salesMemberService;
    private final TeamService teamService;

    private static final int PAGE_SIZE = 30;

    @GetMapping("/members")
    public String members(@RequestParam(required = false) String q,
                          @RequestParam(required = false) Long teamId,
                          @RequestParam(defaultValue = "name") String sort,
                          @RequestParam(defaultValue = "asc") String direction,
                          @RequestParam(defaultValue = "1") int page,
                          Model model) {
        var all = salesMemberService.findActiveMembers(q, teamId, sort, direction);
        int totalCount  = all.size();
        int totalPages  = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        int safePage    = Math.max(1, Math.min(page, totalPages));
        int from        = (safePage - 1) * PAGE_SIZE;
        int to          = Math.min(from + PAGE_SIZE, totalCount);

        model.addAttribute("members",    all.subList(from, to));
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("teams",      teamService.findAll());
        model.addAttribute("q",          q);
        model.addAttribute("teamId",     teamId);
        model.addAttribute("sort",       sort);
        model.addAttribute("direction",  direction);
        model.addAttribute("page",       safePage);
        model.addAttribute("totalPages", totalPages);
        return "sales/members";
    }

    @GetMapping("/members/{userId}/documents")
    public String memberDocuments(@PathVariable Long userId, Model model) {
        User member = salesMemberService.findMemberById(userId);
        model.addAttribute("member", member);
        model.addAttribute("documents", salesMemberService.findMemberDocuments(userId));
        return "sales/member-documents";
    }
}
