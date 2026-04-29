package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.dto.VerifyCodeRequest;
import com.eactive.resourcehub.user.service.SignupService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {

    private static final String SESSION_PENDING_EMAIL = "PENDING_VERIFY_EMAIL";

    private final SignupService signupService;

    @GetMapping
    public String signupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping
    public String signup(@Valid @ModelAttribute SignupRequest signupRequest,
                         BindingResult bindingResult,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        try {
            signupService.signup(signupRequest);
            session.setAttribute(SESSION_PENDING_EMAIL, signupRequest.getEmail());
            return "redirect:/signup/verify";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "signup";
        }
    }

    @GetMapping("/verify")
    public String verifyForm(HttpSession session, Model model) {
        String pendingEmail = (String) session.getAttribute(SESSION_PENDING_EMAIL);
        model.addAttribute("verifyCodeRequest", new VerifyCodeRequest());
        if (pendingEmail != null) {
            model.addAttribute("pendingEmail", pendingEmail);
        }
        return "signup-verify";
    }

    @PostMapping("/verify")
    public String verify(@Valid @ModelAttribute VerifyCodeRequest verifyCodeRequest,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "signup-verify";
        }
        try {
            signupService.verifyEmail(verifyCodeRequest.getEmail(), verifyCodeRequest.getCode());
            session.removeAttribute(SESSION_PENDING_EMAIL);
            return "redirect:/signup/pending";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pendingEmail", verifyCodeRequest.getEmail());
            return "signup-verify";
        }
    }

    @GetMapping("/pending")
    public String pending() {
        return "signup-pending";
    }
}
