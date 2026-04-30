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
    public void signup(SignupRequest request) {
        validateSignupRequest(request);

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.create(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getEmail(),
                null,
                null
        );
        userRepository.save(user);

        String code = generateCode();
        EmailVerificationToken token = EmailVerificationToken.create(user, code, TOKEN_TTL_MINUTES);
        tokenRepository.save(token);

        try {
            emailSender.sendVerificationCode(user.getEmail(), code);
        } catch (Exception e) {
            log.warn("이메일 발송 실패 — email={}, error={}", user.getEmail(), e.getMessage());
        }
        log.info("회원가입 완료 — email={}, status={}", user.getEmail(), user.getStatus());
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
        log.info("이메일 인증 완료 — email={}, status={}", user.getEmail(), user.getStatus());
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

    private void validateSignupRequest(SignupRequest request) {
        if (!request.getEmail().endsWith("@" + companyEmailDomain)) {
            throw new IllegalArgumentException("회사 이메일(@" + companyEmailDomain + ")만 가입할 수 있습니다.");
        }
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
