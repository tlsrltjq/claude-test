package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.entity.EmailVerificationToken;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.EmailVerificationTokenRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    @Value("${resourcehub.company-email-domain}")
    private String companyEmailDomain;

    private static final int TOKEN_TTL_MINUTES = 10;

    @Transactional
    public String signup(SignupRequest request) {
        String email = request.getEmailPrefix() + "@" + companyEmailDomain;
        validateSignupRequest(request, email);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.create(
                email,
                encodedPassword,
                request.getName(),
                email,
                null,
                request.getPosition(),
                request.getBirthDate(),
                request.getPhone()
        );
        userRepository.save(user);

        String code = generateCode();
        EmailVerificationToken token = EmailVerificationToken.create(user, code, TOKEN_TTL_MINUTES);
        tokenRepository.save(token);

        try {
            emailSender.sendVerificationCode(email, code);
        } catch (Exception e) {
            log.warn("이메일 발송 실패 — email={}, error={}", email, e.getMessage());
        }
        log.info("회원가입 완료 — email={}, status={}", email, user.getStatus());
        return email;
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        EmailVerificationToken token = tokenRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 코드를 찾을 수 없습니다."));

        if (token.isVerified()) {
            throw new IllegalArgumentException("이미 인증이 완료된 코드입니다.");
        }
        if (token.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }
        if (!token.getToken().equals(code)) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않습니다.");
        }

        token.markVerified();
        user.verifyEmail();
        log.info("이메일 인증 완료 — email={}, status={}", email, user.getStatus());
    }

    @Transactional
    public void resendCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        String code = generateCode();
        EmailVerificationToken token = EmailVerificationToken.create(user, code, TOKEN_TTL_MINUTES);
        tokenRepository.save(token);
        try {
            emailSender.sendVerificationCode(email, code);
        } catch (Exception e) {
            log.warn("재발송 이메일 실패 — email={}, error={}", email, e.getMessage());
        }
    }

    private void validateSignupRequest(SignupRequest request, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        validatePasswordComplexity(request.getPassword());
    }

    // 영문/숫자/특수문자 중 3종류 이상 + 8자 이상
    private void validatePasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        int types = 0;
        if (password.matches(".*[a-zA-Z].*")) types++;
        if (password.matches(".*[0-9].*")) types++;
        if (password.matches(".*[^a-zA-Z0-9].*")) types++;
        if (types < 3) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
