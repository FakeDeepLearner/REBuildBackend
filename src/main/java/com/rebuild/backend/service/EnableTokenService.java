package com.rebuild.backend.service;

import com.rebuild.backend.model.entities.EnableAccountToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Base64.Encoder;

@Service
public class EnableTokenService {

    private final SecureRandom random  = new SecureRandom();

    private final Encoder encoder = Base64.getUrlEncoder();

    private final JavaMailSender mailSender;

    @Autowired
    public EnableTokenService(@Qualifier("mailSender") JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String generateRandomActivateToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }

    public void sendActivationEmail(String addressFor, String activationToken){
        SimpleMailMessage mailMessage =  new SimpleMailMessage();
        mailMessage.setTo(addressFor);
        mailMessage.setSubject("Account activation");
        String activationUrl = "/api/activate/" + activationToken;

        mailMessage.setText("""
                In order to activate your account, please click on, or paste onto your browser, the following link:
                
           
                """ + activationUrl +
                """
                
                The token will expire in 20 minutes
                
                """);
        mailSender.send(mailMessage);
    }

    public boolean checkTokenExpiry(EnableAccountToken accountToken){
        return accountToken.getExpiryTime().isAfter(LocalDateTime.now());
    }


}
