package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.dto.SignupRequest;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {

    private static final String SESSION_REQUEST = "PENDING_SIGNUP_REQUEST";
    private static final String SESSION_CODE    = "PENDING_SIGNUP_CODE";
    private static final String SESSION_EXPIRY  = "PENDING_SIGNUP_EXPIRY";

    private final SignupService signupService;
    private final TeamRepository teamRepository;

    @Value("${resourcehub.company-email-domain}")
    private String emailDomain;

    @GetMapping
    public String signupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        loadFormModel(model);
        return "signup";
    }

    /** 1단계: 폼 제출 → 유효성 검증 + 인증 코드 발송. DB 미저장. */
    @PostMapping
    public String signup(@Valid @ModelAttribute SignupRequest signupRequest,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        // @Valid 통과 후 추가 필드 레벨 검증
        String pw  = signupRequest.getPassword();
        String pwc = signupRequest.getPasswordConfirm();
        if (pw != null && !bindingResult.hasFieldErrors("password") && !isPasswordComplex(pw)) {
            bindingResult.rejectValue("password", "complexity",
                    "영문, 숫자, 특수문자를 모두 포함하여 8자 이상 입력해주세요.");
        }
        if (pw != null && pwc != null && !pwc.isBlank()
                && !bindingResult.hasFieldErrors("passwordConfirm") && !pw.equals(pwc)) {
            bindingResult.rejectValue("passwordConfirm", "mismatch",
                    "비밀번호가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            loadFormModel(model);
            return "signup";
        }
        try {
            String code = signupService.initiateSignup(signupRequest);
            session.setAttribute(SESSION_REQUEST, signupRequest);
            session.setAttribute(SESSION_CODE, code);
            session.setAttribute(SESSION_EXPIRY, LocalDateTime.now().plusMinutes(5));
            return "redirect:/signup/verify";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            loadFormModel(model);
            return "signup";
        }
    }

    private boolean isPasswordComplex(String password) {
        if (password.length() < 8) return false;
        int types = 0;
        if (password.matches(".*[a-zA-Z].*"))     types++;
        if (password.matches(".*[0-9].*"))        types++;
        if (password.matches(".*[^a-zA-Z0-9].*")) types++;
        return types >= 3;
    }

    private void loadFormModel(Model model) {
        model.addAttribute("positions", Position.values());
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("emailDomain", emailDomain);
    }

    @GetMapping("/verify")
    public String verifyForm(HttpSession session, Model model) {
        SignupRequest pending = (SignupRequest) session.getAttribute(SESSION_REQUEST);
        if (pending == null) {
            return "redirect:/signup";
        }
        LocalDateTime expiry = (LocalDateTime) session.getAttribute(SESSION_EXPIRY);
        model.addAttribute("pendingEmail", signupService.buildEmail(pending.getEmailPrefix()));
        model.addAttribute("expireAt", expiry != null
                ? expiry.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : 0L);
        return "signup-verify";
    }

    /** 2단계: 인증 코드 확인 → 성공 시 DB 저장. */
    @PostMapping("/verify")
    public String verify(@RequestParam String code,
                         HttpSession session,
                         Model model) {
        SignupRequest pending = (SignupRequest) session.getAttribute(SESSION_REQUEST);
        String savedCode     = (String) session.getAttribute(SESSION_CODE);
        LocalDateTime expiry = (LocalDateTime) session.getAttribute(SESSION_EXPIRY);

        if (pending == null || savedCode == null || expiry == null) {
            return "redirect:/signup";
        }

        String pendingEmail = signupService.buildEmail(pending.getEmailPrefix());

        if (LocalDateTime.now().isAfter(expiry)) {
            clearPendingSession(session);
            model.addAttribute("errorMessage", "인증 코드가 만료되었습니다. 다시 회원가입해 주세요.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "signup-verify";
        }

        if (!savedCode.equals(code.trim())) {
            model.addAttribute("errorMessage", "인증 코드가 올바르지 않습니다.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "signup-verify";
        }

        try {
            signupService.completeSignup(pending);
            clearPendingSession(session);
            return "redirect:/signup/pending";
        } catch (IllegalStateException e) {
            clearPendingSession(session);
            model.addAttribute("errorMessage", e.getMessage() + " 다시 회원가입해 주세요.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "signup-verify";
        }
    }

    /** 인증 코드 재발송 */
    @PostMapping("/resend")
    public String resend(HttpSession session, Model model) {
        SignupRequest pending = (SignupRequest) session.getAttribute(SESSION_REQUEST);
        if (pending == null) {
            return "redirect:/signup";
        }
        try {
            String code = signupService.initiateSignup(pending);
            session.setAttribute(SESSION_CODE, code);
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
            session.setAttribute(SESSION_EXPIRY, expiry);
            model.addAttribute("pendingEmail", signupService.buildEmail(pending.getEmailPrefix()));
            model.addAttribute("expireAt", expiry.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            model.addAttribute("infoMessage", "인증 코드를 재발송했습니다.");
        } catch (IllegalArgumentException e) {
            clearPendingSession(session);
            return "redirect:/signup";
        }
        return "signup-verify";
    }

    @GetMapping("/pending")
    public String pending() {
        return "signup-pending";
    }

    private void clearPendingSession(HttpSession session) {
        session.removeAttribute(SESSION_REQUEST);
        session.removeAttribute(SESSION_CODE);
        session.removeAttribute(SESSION_EXPIRY);
    }
}
