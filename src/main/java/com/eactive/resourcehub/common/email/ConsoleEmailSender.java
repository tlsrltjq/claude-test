package com.eactive.resourcehub.common.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        log.info("========================================");
        log.info("[이메일 인증 코드]");
        log.info("  수신자: {}", toEmail);
        log.info("  인증코드: {}", code);
        log.info("  유효시간: 10분");
        log.info("========================================");
    }
}
