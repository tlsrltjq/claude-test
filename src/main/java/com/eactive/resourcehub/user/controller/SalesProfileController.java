package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.user.dto.SalesProfileQuery;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.service.SalesMemberService;
import com.eactive.resourcehub.user.service.SalesProfileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SalesProfileController {

    private final SalesProfileQueryService profileQueryService;
    private final SalesMemberService salesMemberService;

    @GetMapping("/sales/profiles")
    public String profiles(@ModelAttribute("query") SalesProfileQuery query, Model model) {
        model.addAttribute("rows", profileQueryService.findAllProfiles(query));
        model.addAttribute("docTypes", DocumentType.values());
        model.addAttribute("positions", Position.values());
        return "sales/profiles";
    }

    @GetMapping("/sales/employees/{userId}/documents")
    public String employeeDocuments(@PathVariable Long userId, Model model) {
        User member = salesMemberService.findMemberById(userId);
        List<Document> documents = salesMemberService.findMemberDocuments(userId);
        model.addAttribute("member", member);
        model.addAttribute("documents", documents);
        return "sales/employee-documents";
    }
}
