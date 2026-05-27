package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.user.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/login/forgot")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private static final String SESSION_RESET_EMAIL = "RESET_EMAIL";
    private static final String SESSION_RESET_VERIFIED = "RESET_VERIFIED";

    private final PasswordResetService passwordResetService;

    @GetMapping
    public String forgotForm() {
        return "login-forgot";
    }

    @PostMapping
    public String forgotSubmit(@RequestParam String email,
                               HttpSession session,
                               RedirectAttributes ra) {
        passwordResetService.requestReset(email.trim().toLowerCase());
        session.setAttribute(SESSION_RESET_EMAIL, email.trim().toLowerCase());
        session.removeAttribute(SESSION_RESET_VERIFIED);
        ra.addFlashAttribute("infoMessage", "인증코드를 발송했습니다. 이메일을 확인해 주세요. (로컬: 서버 로그 확인)");
        return "redirect:/login/forgot/verify";
    }

    @GetMapping("/verify")
    public String verifyForm(HttpSession session, Model model) {
        String email = (String) session.getAttribute(SESSION_RESET_EMAIL);
        if (email == null) return "redirect:/login/forgot";
        model.addAttribute("email", email);
        return "login-forgot-verify";
    }

    @PostMapping("/verify")
    public String verifySubmit(@RequestParam String code,
                               @RequestParam String newPassword,
                               @RequestParam String newPasswordConfirm,
                               HttpSession session,
                               HttpServletRequest request,
                               Model model) {
        String email = (String) session.getAttribute(SESSION_RESET_EMAIL);
        if (email == null) return "redirect:/login/forgot";

        model.addAttribute("email", email);

        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("errorMessage", "새 비밀번호가 일치하지 않습니다.");
            return "login-forgot-verify";
        }

        if (!passwordResetService.verifyCode(email, code)) {
            model.addAttribute("errorMessage", "인증코드가 올바르지 않거나 만료되었습니다.");
            return "login-forgot-verify";
        }

        try {
            passwordResetService.resetPassword(email, newPassword, request);
            session.removeAttribute(SESSION_RESET_EMAIL);
            session.removeAttribute(SESSION_RESET_VERIFIED);
            return "redirect:/login?reset";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login-forgot-verify";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", "재설정에 실패했습니다. 다시 시도해 주세요.");
            return "login-forgot-verify";
        }
    }
}
