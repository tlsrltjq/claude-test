package com.eactive.resourcehub.team.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @deprecated MVP2부터 /sales/** 로 이전. 이 컨트롤러는 북마크 호환을 위한 리다이렉트만 담당.
 */
@Controller
@RequestMapping("/team")
@Deprecated
public class TeamController {

    @GetMapping("/members")
    public String redirectMembers() {
        return "redirect:/sales/members";
    }

    @GetMapping("/members/{userId}/documents")
    public String redirectMemberDocuments(@PathVariable Long userId) {
        return "redirect:/sales/members/" + userId + "/documents";
    }
}
