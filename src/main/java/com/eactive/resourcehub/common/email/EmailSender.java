package com.eactive.resourcehub.common.email;

public interface EmailSender {

    void sendVerificationCode(String toEmail, String code);
}
