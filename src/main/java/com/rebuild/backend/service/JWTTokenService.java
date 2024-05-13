package com.rebuild.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.JwtEncoder;
@Service
public class JWTTokenService {
    private final JwtEncoder encoder;

    @Autowired
    public JWTTokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }
}
