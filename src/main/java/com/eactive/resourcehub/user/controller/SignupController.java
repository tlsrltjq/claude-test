package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.dto.VerifyCodeRequest;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.service.SignupService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {

    private static final String SESSION_PENDING_EMAIL = "PENDING_VERIFY_EMAIL";

    private final SignupService signupService;

    @Value("${resourcehub.company-email-domain}")
    private String emailDomain;

    @GetMapping
    public String signupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        model.addAttribute("positions", Position.values());
        model.addAttribute("emailDomain", emailDomain);
        return "signup";
    }

    @PostMapping
    public String signup(@Valid @ModelAttribute SignupRequest signupRequest,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("positions", Position.values());
            model.addAttribute("emailDomain", emailDomain);
            return "signup";
        }
        try {
            String email = signupService.signup(signupRequest);
            session.setAttribute(SESSION_PENDING_EMAIL, email);
            return "redirect:/signup/verify";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("positions", Position.values());
            model.addAttribute("emailDomain", emailDomain);
            return "signup";
        }
    }

    @GetMapping("/verify")
    public String verifyForm(HttpSession session, Model model) {
        String pendingEmail = (String) session.getAttribute(SESSION_PENDING_EMAIL);
        if (pendingEmail == null) {
            return "redirect:/signup";
        }
        model.addAttribute("verifyCodeRequest", new VerifyCodeRequest());
        model.addAttribute("pendingEmail", pendingEmail);
        return "signup-verify";
    }

    @PostMapping("/verify")
    public String verify(@Valid @ModelAttribute VerifyCodeRequest verifyCodeRequest,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        String email = (String) session.getAttribute(SESSION_PENDING_EMAIL);
        if (email == null) {
            return "redirect:/signup";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pendingEmail", email);
            return "signup-verify";
        }
        try {
            signupService.verifyEmail(email, verifyCodeRequest.getCode());
            session.removeAttribute(SESSION_PENDING_EMAIL);
            return "redirect:/signup/pending";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pendingEmail", email);
            return "signup-verify";
        }
    }

    @GetMapping("/pending")
    public String pending() {
        return "signup-pending";
    }
}
