package com.rebuild.backend.service;

import com.rebuild.backend.model.entities.EnableAccountToken;
import com.rebuild.backend.repository.EnableTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Locale;

@Service
public class EnableTokenService {

    private final SecureRandom random  = new SecureRandom();

    private final Encoder encoder = Base64.getUrlEncoder();

    private final JavaMailSender mailSender;

    private final EnableTokenRepository tokenRepository;

    @Autowired
    public EnableTokenService(@Qualifier("mailSender") JavaMailSender mailSender, EnableTokenRepository tokenRepository) {
        this.mailSender = mailSender;
        this.tokenRepository = tokenRepository;
    }

    private String generateRandomActivateToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }

    public EnableAccountToken createActivationToken(String email, long timeCount, ChronoUnit timeUnit){
        String randomToken = generateRandomActivateToken();
        EnableAccountToken newToken = new EnableAccountToken(randomToken, email,
                LocalDateTime.now().plus(timeCount, timeUnit));
        return tokenRepository.save(newToken);
    }

    public void sendActivationEmail(String addressFor, String activationToken,
                                    Long timeCount, ChronoUnit timeUnit){
        SimpleMailMessage mailMessage =  new SimpleMailMessage();
        mailMessage.setTo(addressFor);
        mailMessage.setSubject("Account activation");
        String activationUrl = "/api/activate/" + activationToken;
        String timeStringDefault = timeUnit.toString().toLowerCase(Locale.CANADA);
        String reprInMessage = (timeCount >= 2) ? timeStringDefault :
                (timeStringDefault.substring(0, timeStringDefault.length() - 1));
        mailMessage.setText("""
                In order to activate your account, please click on, or paste onto your browser, the following link:
                
           
                """ + activationUrl +
                """
                \n
                The token will expire in
                
                """ + timeCount + reprInMessage);
        mailSender.send(mailMessage);
    }

    public boolean checkTokenExpiry(EnableAccountToken accountToken){
        return accountToken.getExpiryTime().isAfter(LocalDateTime.now());
    }


}
