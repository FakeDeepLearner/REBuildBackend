package com.rebuild.backend.service;

import com.rebuild.backend.model.entities.EnableAccountToken;
import com.rebuild.backend.model.entities.ResetPasswordToken;
import com.rebuild.backend.repository.ResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Base64.Encoder;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class ResetTokenService {
    private final ResetTokenRepository tokenRepository;

    private final JavaMailSender mailSender;

    private final SecureRandom random = new SecureRandom();

    private final Encoder encoder = Base64.getUrlEncoder();

    @Autowired
    public ResetTokenService(ResetTokenRepository tokenRepository,
                             @Qualifier("mailSender") JavaMailSender mailSender) {
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    private String generateBase64Token() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }

    public ResetPasswordToken createResetToken(String email, Long timeCount, ChronoUnit timeUnit){
        String randomToken = generateBase64Token();
        ResetPasswordToken newToken = new ResetPasswordToken(randomToken, email,
                LocalDateTime.now().plus(timeCount, timeUnit));
        return tokenRepository.save(newToken);
    }

    public void sendResetEmail(String addressFor, String resetToken, Long timeCount, ChronoUnit timeUnit){
        SimpleMailMessage mailMessage =  new SimpleMailMessage();
        mailMessage.setTo(addressFor);
        mailMessage.setSubject("Reset your password");
        String activationUrl = "/api/reset/" + resetToken;
        String timeStringDefault = timeUnit.toString().toLowerCase(Locale.CANADA);
        String reprInMessage = (timeCount >= 2) ? timeStringDefault :
                (timeStringDefault.substring(0, timeStringDefault.length() - 1));
        mailMessage.setText("""
                In order to reset your password, please click on, or paste onto your browser, the following link:
                
           
                """ + activationUrl +
                """
                \n
                The token will expire in
                
                """ + timeCount + reprInMessage);
        mailSender.send(mailMessage);
    }


    public boolean checkTokenExpiry(ResetPasswordToken token){
        return token.getExpiry().isAfter(LocalDateTime.now());
    }

}
