package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import com.eactive.resourcehub.common.util.PasswordValidator;
import com.eactive.resourcehub.user.dto.SignupRequest;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.AllowedEmailRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final DateTimeFormatter JOIN_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final FolderRepository folderRepository;
    private final AllowedEmailRepository allowedEmailRepository;

    /**
     * 1단계: 폼 유효성 검증 + 허용 이메일 확인 + 인증 코드 발송. DB에 저장하지 않음.
     * @return 생성된 6자리 인증 코드
     */
    @Transactional(readOnly = true)
    public String initiateSignup(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();
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
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        LocalDate birthDate = parseBirthDate(request.getBirthDateStr());
        Team team = (request.getTeamId() != null)
                ? teamRepository.findById(request.getTeamId()).orElse(null)
                : null;
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        LocalDate joinDate = parseJoinDate(request.getJoinDateStr());
        User user = User.create(email, encodedPassword, request.getName(), email, team,
                request.getPosition(), birthDate, request.getPhone());
        user.verifyEmail();
        if (request.getAddress() != null && !request.getAddress().isBlank() || joinDate != null) {
            user.updateProfile(null, null, null,
                    (request.getAddress() != null && !request.getAddress().isBlank()) ? request.getAddress() : null,
                    joinDate);
        }
        userRepository.save(user);
        if (!folderRepository.existsByOwnerIdAndType(user.getId(), FolderType.PERSONAL)) {
            folderRepository.save(Folder.create(user, user.getName() + " 개인 폴더"));
        }
        log.info("회원가입 완료 (즉시 활성화) — email={}", email);
    }

    private void validateSignupRequest(SignupRequest request, String email) {
        if (!allowedEmailRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("가입이 허용되지 않은 이메일입니다. 관리자에게 문의하세요.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (!request.isPrivacyConsent()) {
            throw new IllegalArgumentException("개인정보 수집·이용에 동의해주세요.");
        }
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        PasswordValidator.validate(request.getPassword());
    }

    private LocalDate parseJoinDate(String joinDateStr) {
        if (joinDateStr == null || joinDateStr.isBlank()) return null;
        String digits = joinDateStr.replace(".", "");
        if (!digits.matches("\\d{8}")) return null;
        try {
            LocalDate date = LocalDate.parse(digits, JOIN_FMT);
            if (date.isBefore(LocalDate.of(1900, 1, 1)) || date.isAfter(LocalDate.now())) return null;
            return date;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDate parseBirthDate(String birthDateStr) {
        if (birthDateStr == null || birthDateStr.isBlank())
            throw new IllegalArgumentException("생년월일을 입력해주세요.");
        String digits = birthDateStr.replace(".", "");
        if (!digits.matches("\\d{8}"))
            throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다. 예: 1999.01.01");
        try {
            LocalDate date = LocalDate.parse(digits, BIRTH_FMT);
            if (date.isBefore(LocalDate.of(1900, 1, 1)) || date.isAfter(LocalDate.now()))
                throw new IllegalArgumentException("유효하지 않은 생년월일입니다.");
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다. 예: 1999.01.01");
        }
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
}
