package com.rebuild.backend.service;

import com.rebuild.backend.repository.ResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Base64.Encoder;

import java.security.SecureRandom;

@Service
public class ResetTokenService {
    private final ResetTokenRepository tokenRepository;

    private final JavaMailSender mailSender;

    private final SecureRandom random = new SecureRandom();

    private final Encoder encoder = Base64.getUrlEncoder();

    @Autowired
    public ResetTokenService(ResetTokenRepository tokenRepository, JavaMailSender mailSender) {
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    public String generateBase64Token() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }



}
