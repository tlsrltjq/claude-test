package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {

    private static final DateTimeFormatter BIRTH_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    @Value("${resourcehub.company-email-domain}")
    private String companyEmailDomain;

    public String buildEmail(String prefix) {
        return prefix + "@" + companyEmailDomain;
    }

    /**
     * 1단계: 폼 유효성 검증 + 인증 코드 발송. DB에 저장하지 않음.
     * @return 생성된 6자리 인증 코드
     */
    @Transactional(readOnly = true)
    public String initiateSignup(SignupRequest request) {
        String email = buildEmail(request.getEmailPrefix());
        parseBirthDate(request.getBirthDateStr());
        validateSignupRequest(request, email);

        String code = generateCode();
        try {
            emailSender.sendVerificationCode(email, code);
        } catch (Exception e) {
            log.warn("이메일 발송 실패 — email={}, error={}", email, e.getMessage());
        }
        log.info("인증 코드 발송 — email={}", email);
        return code;
    }

    /**
     * 2단계: 이메일 인증 완료 후 DB에 유저 저장. status = ACTIVE (즉시 로그인 가능).
     */
    @Transactional
    public void completeSignup(SignupRequest request) {
        String email = buildEmail(request.getEmailPrefix());
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        LocalDate birthDate = parseBirthDate(request.getBirthDateStr());
        Team team = (request.getTeamId() != null)
                ? teamRepository.findById(request.getTeamId()).orElse(null)
                : null;
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.create(email, encodedPassword, request.getName(), email, team,
                request.getPosition(), birthDate, request.getPhone());
        user.verifyEmail();
        userRepository.save(user);
        log.info("회원가입 완료 (즉시 활성화) — email={}", email);
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

    private LocalDate parseBirthDate(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.isBlank())
            throw new IllegalArgumentException("생년월일을 입력해주세요.");
        if (!birthDateStr.matches("\\d{8}"))
            throw new IllegalArgumentException("생년월일은 8자 숫자로 입력하세요. 예: 20010904");
        try {
            LocalDate date = LocalDate.parse(birthDateStr, BIRTH_FMT);
            if (date.isBefore(LocalDate.of(1900, 1, 1)) || date.isAfter(LocalDate.now()))
                throw new IllegalArgumentException("유효하지 않은 생년월일입니다.");
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("생년월일은 8자 숫자로 입력하세요. 예: 20010904");
        }
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
