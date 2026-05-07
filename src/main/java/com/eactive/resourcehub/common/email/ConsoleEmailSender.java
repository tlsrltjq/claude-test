package com.eactive.resourcehub.common.email;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    @Override
    public void sendDocumentExpiringSoon(String toEmail, String ownerName,
                                         String documentTitle, java.time.LocalDate expiresAt,
                                         int daysLeft) {
        log.info("========================================");
        log.info("[문서 만료 임박 알림]");
        log.info("  수신자: {} <{}>", ownerName, toEmail);
        log.info("  문서: {}", documentTitle);
        log.info("  만료일: {} ({}일 후)", expiresAt, daysLeft);
        log.info("  내용: 문서 유효기간이 {}일 후 만료됩니다. 갱신을 준비해 주세요.", daysLeft);
        log.info("========================================");
    }

    @Override
    public void sendDocumentExpired(String toEmail, String ownerName,
                                    String documentTitle, java.time.LocalDate expiresAt) {
        log.info("========================================");
        log.info("[문서 만료 알림]");
        log.info("  수신자: {} <{}>", ownerName, toEmail);
        log.info("  문서: {}", documentTitle);
        log.info("  만료일: {}", expiresAt);
        log.info("  내용: 문서 유효기간이 만료되었습니다. 최신 문서로 갱신해 주세요.");
        log.info("========================================");
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        log.info("========================================");
        log.info("[비밀번호 재설정 인증코드]");
        log.info("  수신자: {}", toEmail);
        log.info("  인증코드: {}", code);
        log.info("  유효시간: 5분");
        log.info("========================================");
    }
}
