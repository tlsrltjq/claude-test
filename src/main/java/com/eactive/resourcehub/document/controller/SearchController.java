package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.service.SearchService;
import com.eactive.resourcehub.document.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final TagService tagService;

    @GetMapping
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String type,
                         @RequestParam(required = false) String tag,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         Model model) {

        DocumentType typeFilter = null;
        if (type != null && !type.isBlank()) {
            try { typeFilter = DocumentType.valueOf(type); } catch (IllegalArgumentException ignored) {}
        }

        boolean hasQuery = (q != null && !q.isBlank()) || typeFilter != null
                || (tag != null && !tag.isBlank());

        if (hasQuery) {
            var results = searchService.search(
                    userDetails.getUser().getId(),
                    userDetails.getUser().getRole(),
                    q, typeFilter, tag);
            model.addAttribute("results", results);
            model.addAttribute("totalCount", results.size());
        }

        model.addAttribute("q", q);
        model.addAttribute("type", type);
        model.addAttribute("tag", tag);
        model.addAttribute("documentTypes", DocumentType.values());
        model.addAttribute("allTags", tagService.findAll());
        return "search";
    }
}
