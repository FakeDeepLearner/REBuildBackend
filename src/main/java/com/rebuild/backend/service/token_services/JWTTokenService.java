package com.rebuild.backend.service.token_services;

import com.rebuild.backend.config.properties.MailAppCredentials;
import com.rebuild.backend.exceptions.jwt_exceptions.JWTCredentialsMismatchException;
import com.rebuild.backend.exceptions.jwt_exceptions.JWTTokenExpiredException;
import com.rebuild.backend.exceptions.jwt_exceptions.NoJWTTokenException;
import com.rebuild.backend.service.util_services.CustomUserDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("jwt")
public class JWTTokenService {
    private final JwtEncoder encoder;

    private final JwtDecoder decoder;

    private final CustomUserDetailsService detailsService;

    @Autowired
    public JWTTokenService(@Qualifier("encoder") JwtEncoder encoder,
                           @Qualifier("decoder") JwtDecoder decoder,
                           CustomUserDetailsService detailsService,
                           @Qualifier("mailSender") JavaMailSender mailSender,
                           MailAppCredentials credentials) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.detailsService = detailsService;
    }

    public String obtainRefreshTokenFromCookie(HttpServletRequest request) {
        if(request.getCookies() == null){
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) {
                return cookie.getValue();
            }
        }

        return null;

    }

    private String generateTokenGivenExpiration(Authentication auth, long amount, ChronoUnit unit){
        Instant curr = Instant.now();
        String claim = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().
                issuer("self").
                issuedAt(curr).
                expiresAt(curr.plus(amount, unit)).
                subject(auth.getName()).
                claim("scope", claim).
                build();
        return encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public String generateRefreshToken(Authentication auth){
        return generateTokenGivenExpiration(auth, 2, ChronoUnit.HOURS);
    }

    public String generateAccessToken(Authentication auth){
        return generateTokenGivenExpiration(auth, 10, ChronoUnit.MINUTES);
    }

    public String extractTokenFromRequest(HttpServletRequest request){
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")){
            return auth.substring("Bearer ".length());
        }
        else{
            throw new NoJWTTokenException("No JWT token is in the request");
        }
    }

    private Jwt extractAllClaims(String token) {
        return decoder.decode(token);
    }

    public String extractSubject(String token) {
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsString("sub");
    }

    private Instant extractExpiration(String token) {
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsInstant("exp");
    }

    public Duration getExpiryDuration(String token){
        Jwt allClaims = extractAllClaims(token);
        Instant expiryTime = allClaims.getClaimAsInstant("exp");
        return Duration.between(Instant.now(), expiryTime);
    }

    private boolean tokenCredentialsMatch(String token, UserDetails details) {
        String tokenUsername = extractSubject(token);
        String actualUsername = details.getUsername();
        boolean result = tokenUsername.equals(actualUsername);
        if (!result){
            throw new JWTCredentialsMismatchException("Credentials mismatch");
        }
        return true;
    }

    public boolean tokenNonExpired(String token) {
        Instant tokenExpiration = extractExpiration(token);
        boolean result = tokenExpiration.isBefore(Instant.now());
        if (!result){
            throw new JWTTokenExpiredException("Token expired", token);
        }
        return true;
    }

    public boolean isTokenValid(String token, UserDetails details){
        return tokenCredentialsMatch(token, details) && tokenNonExpired(token);
    }


    public String issueNewAccessToken(HttpServletRequest request){
        String refresh_token = obtainRefreshTokenFromCookie(request);
        String subject = extractSubject(refresh_token);
        UserDetails details = detailsService.loadUserByUsername(subject);
        Instant curr = Instant.now();
        String claim = details.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().
                issuer("self").
                issuedAt(curr).
                expiresAt(curr.plus(2, ChronoUnit.HOURS)).
                subject(subject).
                claim("scope", claim).
                build();
        return encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();

    }




}
