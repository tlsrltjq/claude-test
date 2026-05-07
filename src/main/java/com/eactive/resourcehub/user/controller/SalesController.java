package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.team.repository.TeamRepository;
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
    private final TeamRepository teamRepository;

    @GetMapping("/members")
    public String members(@RequestParam(required = false) String q,
                          @RequestParam(required = false) Long teamId,
                          @RequestParam(defaultValue = "position") String sort,
                          @RequestParam(defaultValue = "asc") String direction,
                          Model model) {
        model.addAttribute("members", salesMemberService.findActiveMembers(q, teamId, sort, direction));
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("q", q);
        model.addAttribute("teamId", teamId);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
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
