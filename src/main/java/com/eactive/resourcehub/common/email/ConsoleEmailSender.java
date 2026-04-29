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

    @Override
    public void sendDocumentApproved(String toEmail, String ownerName,
                                     String documentTitle, int versionNo) {
        log.info("========================================");
        log.info("[문서 승인 알림]");
        log.info("  수신자: {} <{}>", ownerName, toEmail);
        log.info("  문서: {} (v{})", documentTitle, versionNo);
        log.info("  내용: 제출하신 문서가 승인되었습니다.");
        log.info("========================================");
    }

    @Override
    public void sendDocumentRejected(String toEmail, String ownerName,
                                     String documentTitle, int versionNo, String reason) {
        log.info("========================================");
        log.info("[문서 반려 알림]");
        log.info("  수신자: {} <{}>", ownerName, toEmail);
        log.info("  문서: {} (v{})", documentTitle, versionNo);
        log.info("  반려 사유: {}", reason);
        log.info("  내용: 제출하신 문서가 반려되었습니다. 사유를 확인 후 재업로드해 주세요.");
        log.info("========================================");
    }
}
