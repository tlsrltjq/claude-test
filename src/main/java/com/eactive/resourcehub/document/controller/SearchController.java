package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String type,
                         @RequestParam(required = false) String uploader,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                         @RequestParam(required = false) String folderKind,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {

        DocumentType typeFilter = null;
        if (type != null && !type.isBlank()) {
            try { typeFilter = DocumentType.valueOf(type); } catch (IllegalArgumentException ignored) {}
        }

        var results = searchService.search(
                userDetails.getUser().getId(),
                userDetails.getUser().getRole(),
                q, typeFilter, uploader, dateFrom, dateTo, folderKind);

        model.addAttribute("results", results);
        model.addAttribute("totalCount", results.size());
        model.addAttribute("userId", userDetails.getUser().getId());
        model.addAttribute("q", q);
        model.addAttribute("type", type);
        model.addAttribute("uploader", uploader);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("folderKind", folderKind);
        model.addAttribute("documentTypes", Arrays.stream(DocumentType.values())
                .filter(DocumentType::isActive).toArray());
        return "search";
    }
}
