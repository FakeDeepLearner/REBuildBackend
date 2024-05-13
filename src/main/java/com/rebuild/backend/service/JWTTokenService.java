package com.rebuild.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JWTTokenService {
    private final JwtEncoder encoder;

    @Autowired
    public JWTTokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String generateJWTToken(Authentication auth){
        Instant curr = Instant.now();
        String claim = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().
                issuer("self").
                issuedAt(curr).
                expiresAt(curr.plus(30, ChronoUnit.MINUTES)).
                subject(auth.getName()).
                claim("scope", claim).
                build();
        return encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}
