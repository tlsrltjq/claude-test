package com.eactive.resourcehub.common.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

@Slf4j
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public SmtpEmailSender(JavaMailSender mailSender, String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        send(toEmail, "[eActive Resource Hub] 이메일 인증 코드",
                "안녕하세요.\n\n이메일 인증 코드: " + code + "\n\n유효시간: 10분\n\n본인이 요청하지 않았다면 이 메일을 무시해 주세요.");
    }

    @Override
    public void sendDocumentApproved(String toEmail, String ownerName, String documentTitle, int versionNo) {
        send(toEmail, "[eActive Resource Hub] 문서 승인 알림",
                ownerName + "님, 안녕하세요.\n\n제출하신 문서 '" + documentTitle + "' (v" + versionNo + ")가 승인되었습니다.");
    }

    @Override
    public void sendDocumentRejected(String toEmail, String ownerName, String documentTitle, int versionNo, String reason) {
        send(toEmail, "[eActive Resource Hub] 문서 반려 알림",
                ownerName + "님, 안녕하세요.\n\n제출하신 문서 '" + documentTitle + "' (v" + versionNo + ")가 반려되었습니다.\n\n반려 사유: " + reason);
    }

    @Override
    public void sendDocumentExpiringSoon(String toEmail, String ownerName, String documentTitle, LocalDate expiresAt, int daysLeft) {
        send(toEmail, "[eActive Resource Hub] 문서 만료 임박 알림",
                ownerName + "님, 안녕하세요.\n\n문서 '" + documentTitle + "'의 유효기간이 " + daysLeft + "일 후(" + expiresAt + ") 만료됩니다.\n\n갱신을 준비해 주세요.");
    }

    @Override
    public void sendDocumentExpired(String toEmail, String ownerName, String documentTitle, LocalDate expiresAt) {
        send(toEmail, "[eActive Resource Hub] 문서 만료 알림",
                ownerName + "님, 안녕하세요.\n\n문서 '" + documentTitle + "'의 유효기간이 만료되었습니다(" + expiresAt + ").\n\n최신 문서로 갱신해 주세요.");
    }

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.info("메일 발송 완료: {} → {}", subject, to);
        } catch (Exception e) {
            log.warn("메일 발송 실패 ({}): {}", to, e.getMessage());
        }
    }
}
