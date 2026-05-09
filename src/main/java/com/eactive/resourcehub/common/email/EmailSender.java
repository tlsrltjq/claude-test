package com.eactive.resourcehub.common.email;

public interface EmailSender {

    void sendVerificationCode(String toEmail, String code);

    void sendDocumentApproved(String toEmail, String ownerName,
                              String documentTitle, int versionNo);

    void sendDocumentRejected(String toEmail, String ownerName,
                              String documentTitle, int versionNo, String reason);

    void sendDocumentExpiringSoon(String toEmail, String ownerName,
                                  String documentTitle, java.time.LocalDate expiresAt, int daysLeft);

    void sendDocumentExpired(String toEmail, String ownerName,
                             String documentTitle, java.time.LocalDate expiresAt);

    void sendPasswordResetCode(String toEmail, String code);
}
