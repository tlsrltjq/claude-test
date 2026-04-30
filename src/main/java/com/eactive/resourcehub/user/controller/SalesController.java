package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.service.SalesMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesMemberService salesMemberService;

    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", salesMemberService.findActiveMembers());
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
