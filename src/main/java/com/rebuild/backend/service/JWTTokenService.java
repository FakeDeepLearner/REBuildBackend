package com.rebuild.backend.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JWTTokenService {
    private final JwtEncoder encoder;

    private final JwtDecoder decoder;

    @Autowired
    public JWTTokenService(JwtEncoder encoder, JwtDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public String generateJWTToken(Authentication auth){
        Instant curr = Instant.now();
        String claim = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().
                issuer("self").
                issuedAt(curr).
                expiresAt(curr.plus(2, ChronoUnit.HOURS)).
                subject(auth.getName()).
                claim("scope", claim).
                build();
        return encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    private Jwt extractAllClaims(String token) {
        return decoder.decode(token);

    }

    public String extractUsername(String token) {
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsString("sub");
    }

    private Instant extractExpiration(String token) {
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsInstant("exp");
    }

    public boolean isTokenValid(String token, UserDetails details) {
        String tokenUsername = extractUsername(token);
        String actualUsername = details.getUsername();
        return tokenUsername.equals(actualUsername);
    }

    public boolean tokenNonExpired(String token) {
        Instant tokenExpiration = extractExpiration(token);
        return tokenExpiration.isBefore(Instant.now());
    }




}
