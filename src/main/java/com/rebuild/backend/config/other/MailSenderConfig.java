package com.rebuild.backend.config.other;

import com.rebuild.backend.config.properties.MailAppCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailSenderConfig {
    private final MailAppCredentials credentials;

    @Autowired
    public MailSenderConfig(MailAppCredentials credentials) {
        this.credentials = credentials;
    }

    @Bean
    public JavaMailSender mailSender(){
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        sender.setHost("smtp.office365.com");
        sender.setPort(587);
        sender.setUsername(credentials.address());
        sender.setPassword(credentials.appPassword());

        Properties mailProperties = sender.getJavaMailProperties();

        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.starttls.enable", "true");

        return sender;

    }
}
