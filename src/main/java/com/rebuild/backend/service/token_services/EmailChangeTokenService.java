package com.rebuild.backend.service.token_services;

import com.rebuild.backend.model.entities.tokens.EmailChangeToken;
import com.rebuild.backend.repository.EmailChangeTokenRepository;
import com.sun.security.auth.UserPrincipal;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("email")
public class EmailChangeTokenService {
    private final EmailChangeTokenRepository changeTokenRepository;

    private final JavaMailSender mailSender;

    private final SecureRandom random  = new SecureRandom();

    private final Base64.Encoder encoder = Base64.getUrlEncoder();

    private final Map<String, String> tokensMap = new ConcurrentHashMap<>();

    public EmailChangeTokenService(EmailChangeTokenRepository changeTokenRepository,
                                   JavaMailSender mailSender) {
        this.changeTokenRepository = changeTokenRepository;
        this.mailSender = mailSender;
    }

    private String generateRandomEmailChangeToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }

    public boolean checkTokenExpiry(EmailChangeToken token){
        return token.getExpiryTime().isAfter(LocalDateTime.now());
    }

    public void sendActivationEmail(String addressFor, String activationToken,
                                    Long timeCount, ChronoUnit timeUnit){
        SimpleMailMessage mailMessage =  new SimpleMailMessage();
        mailMessage.setTo(addressFor);
        mailMessage.setSubject("Change email");
        String activationUrl = "/api/change_mail?token=" + activationToken;
        String timeStringDefault = timeUnit.toString().toLowerCase(Locale.CANADA);
        String reprInMessage = (timeCount >= 2) ? timeStringDefault :
                (timeStringDefault.substring(0, timeStringDefault.length() - 1));
        mailMessage.setText("""
                In order to confirm your new email, please click on, or paste onto your browser, the following link:
                
           
                """ + activationUrl +
                """
                \n
                The token will expire in
                
                """ + timeCount + reprInMessage);
        mailSender.send(mailMessage);
    }


}
