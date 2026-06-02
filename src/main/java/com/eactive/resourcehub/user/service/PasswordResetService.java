package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.common.util.PasswordValidator;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.user.entity.PasswordResetToken;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.PasswordResetTokenRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final AuditService auditService;

    /**
     * 비밀번호 재설정 코드 발급. 사용자가 존재하지 않아도 동일 메시지 반환(이메일 열거 방지).
     */
    @Transactional
    public void requestReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("비번 찾기 요청 — 존재하지 않는 이메일: {}", email);
            return;
        }
        User user = userOpt.get();
        String code = generateCode();
        PasswordResetToken token = tokenRepository.save(PasswordResetToken.create(user, code));
        tokenRepository.invalidatePreviousTokens(user.getId(), token.getId());

        try {
            emailSender.sendPasswordResetCode(email, code);
        } catch (Exception e) {
            log.warn("비번 재설정 메일 발송 실패: {}", e.getMessage());
        }
        log.info("[PASSWORD_RESET] 코드 발급 — email={}", email);
    }

    /**
     * 인증코드 검증. 유효하면 PasswordResetToken을 verified로 표시 후 true 반환.
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository
                .findTopByEmailOrderByCreatedAtDesc(email);
        if (tokenOpt.isEmpty()) return false;
        PasswordResetToken token = tokenOpt.get();
        if (token.isExpired() || token.isConsumed() || !token.getToken().equals(code.trim())) {
            return false;
        }
        token.markVerified();
        return true;
    }

    /**
     * 새 비밀번호 설정. verifyCode()가 선행되어야 함.
     */
    @Transactional
    public void resetPassword(String email, String newPassword, HttpServletRequest request) {
        PasswordResetToken token = tokenRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalStateException("토큰이 없습니다."));

        if (!token.isVerified() || token.isConsumed() || token.isExpired()) {
            throw new IllegalStateException("유효하지 않은 재설정 요청입니다.");
        }

        PasswordValidator.validate(newPassword);

        User user = token.getUser();
        user.changePassword(passwordEncoder.encode(newPassword));
        token.markConsumed();

        auditService.log(user.getId(), AuditActionType.RESET_PASSWORD,
                AuditTargetType.USER, user.getId(), "비밀번호 재설정", request);
        log.info("[PASSWORD_RESET] 완료 — email={}", email);
    }

    /** 5회 실패 시 컨트롤러에서 호출 — 현재 토큰을 즉시 만료시킨다. */
    @Transactional
    public void invalidateCurrentToken(String email) {
        tokenRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .ifPresent(PasswordResetToken::invalidate);
    }

    private String generateCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
