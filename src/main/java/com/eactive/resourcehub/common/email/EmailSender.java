package com.eactive.resourcehub.common.email;

public interface EmailSender {

    void sendVerificationCode(String toEmail, String code);

    void sendDocumentApproved(String toEmail, String ownerName,
                              String documentTitle, int versionNo);

    void sendDocumentRejected(String toEmail, String ownerName,
                              String documentTitle, int versionNo, String reason);
}
