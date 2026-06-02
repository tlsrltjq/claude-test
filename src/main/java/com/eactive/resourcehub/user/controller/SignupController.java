package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.team.service.TeamService;
import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.service.SignupService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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

@Slf4j
@Controller
@RequestMapping("/signup")
@RequiredArgsConstructor
public class SignupController {

    private static final String SESSION_REQUEST    = "PENDING_SIGNUP_REQUEST";
    private static final String SESSION_CODE       = "PENDING_SIGNUP_CODE";
    private static final String SESSION_EXPIRY     = "PENDING_SIGNUP_EXPIRY";
    private static final String SESSION_HASHED_PW  = "PENDING_SIGNUP_HASHED_PW";
    private static final String SESSION_FAIL_CNT   = "PENDING_SIGNUP_FAIL_COUNT";
    private static final int    MAX_VERIFY_ATTEMPTS = 5;

    private final SignupService signupService;
    private final TeamService teamService;
    private final PasswordEncoder passwordEncoder;

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
            model.addAttribute("signupRequest", new SignupRequest());
            loadFormModel(model);
            return "signup";
        }
        try {
            String code = signupService.initiateSignup(signupRequest);
            // 평문 비밀번호를 세션에 저장하지 않음 — 즉시 BCrypt 해시 후 분리 보관
            String hashedPw = passwordEncoder.encode(signupRequest.getPassword());
            signupRequest.setPassword(null);
            signupRequest.setPasswordConfirm(null);
            session.setAttribute(SESSION_REQUEST, signupRequest);
            session.setAttribute(SESSION_HASHED_PW, hashedPw);
            session.setAttribute(SESSION_CODE, code);
            session.setAttribute(SESSION_EXPIRY, LocalDateTime.now().plusMinutes(10));
            return "redirect:/signup/verify";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("signupRequest", new SignupRequest());
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
        model.addAttribute("teams", teamService.findAll());
    }

    @GetMapping("/verify")
    public String verifyForm(HttpSession session, Model model) {
        SignupRequest pending = (SignupRequest) session.getAttribute(SESSION_REQUEST);
        if (pending == null) {
            return "redirect:/signup";
        }
        LocalDateTime expiry = (LocalDateTime) session.getAttribute(SESSION_EXPIRY);
        model.addAttribute("pendingEmail", pending.getEmail());
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
        String hashedPw      = (String) session.getAttribute(SESSION_HASHED_PW);

        if (pending == null || savedCode == null || expiry == null || hashedPw == null) {
            return "redirect:/signup";
        }

        String pendingEmail = pending.getEmail();

        if (LocalDateTime.now().isAfter(expiry)) {
            clearPendingSession(session);
            model.addAttribute("errorMessage", "인증 코드가 만료되었습니다. 다시 회원가입해 주세요.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "signup-verify";
        }

        Integer failAttr = (Integer) session.getAttribute(SESSION_FAIL_CNT);
        int failCount = failAttr == null ? 0 : failAttr;
        if (failCount >= MAX_VERIFY_ATTEMPTS) {
            clearPendingSession(session);
            return "redirect:/signup?toomany";
        }

        if (!savedCode.equals(code.trim())) {
            int newCount = failCount + 1;
            session.setAttribute(SESSION_FAIL_CNT, newCount);
            int remaining = MAX_VERIFY_ATTEMPTS - newCount;
            if (remaining <= 0) {
                clearPendingSession(session);
                return "redirect:/signup?toomany";
            }
            model.addAttribute("errorMessage",
                    "인증 코드가 올바르지 않습니다. (남은 시도: " + remaining + "회)");
            model.addAttribute("pendingEmail", pendingEmail);
            return "signup-verify";
        }

        try {
            pending.setPassword(hashedPw); // 해시된 비밀번호 복원 후 저장
            signupService.completeSignup(pending);
            clearPendingSession(session);
            return "redirect:/login?signup";
        } catch (IllegalStateException e) {
            clearPendingSession(session);
            model.addAttribute("errorMessage", e.getMessage() + " 다시 회원가입해 주세요.");
            model.addAttribute("pendingEmail", pendingEmail);
            return "signup-verify";
        }
    }

    /** 인증 코드 재발송 (비밀번호 재검증 없이 코드만 재생성) */
    @PostMapping("/resend")
    public String resend(HttpSession session, Model model) {
        SignupRequest pending = (SignupRequest) session.getAttribute(SESSION_REQUEST);
        if (pending == null) {
            return "redirect:/signup";
        }
        String code = signupService.resendVerificationCode(pending.getEmail());
        session.setAttribute(SESSION_CODE, code);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        session.setAttribute(SESSION_EXPIRY, expiry);
        model.addAttribute("pendingEmail", pending.getEmail());
        model.addAttribute("expireAt", expiry.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        model.addAttribute("infoMessage", "인증 코드를 재발송했습니다.");
        return "signup-verify";
    }

    private void clearPendingSession(HttpSession session) {
        session.removeAttribute(SESSION_REQUEST);
        session.removeAttribute(SESSION_CODE);
        session.removeAttribute(SESSION_EXPIRY);
        session.removeAttribute(SESSION_HASHED_PW);
        session.removeAttribute(SESSION_FAIL_CNT);
    }
}
