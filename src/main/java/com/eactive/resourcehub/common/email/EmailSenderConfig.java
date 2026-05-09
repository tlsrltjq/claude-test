package com.eactive.resourcehub.common.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailSenderConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.mail.host")
    public EmailSender smtpEmailSender(JavaMailSender mailSender,
                                       @Value("${spring.mail.username}") String fromEmail) {
        return new SmtpEmailSender(mailSender, fromEmail);
    }

    @Bean
    @ConditionalOnMissingBean(EmailSender.class)
    public EmailSender consoleEmailSender() {
        return new ConsoleEmailSender();
    }
}
