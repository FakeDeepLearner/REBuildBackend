package com.rebuild.backend.service;

import com.rebuild.backend.exceptions.JWTCredentialsMismatchException;
import com.rebuild.backend.exceptions.JWTTokenExpiredException;
import com.rebuild.backend.exceptions.NoJWTTokenException;
import com.rebuild.backend.model.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JWTTokenService {
    private final JwtEncoder encoder;

    private final JwtDecoder decoder;

    private final UserDetailsService detailsService;

    @Autowired
    public JWTTokenService(@Qualifier("encoder") JwtEncoder encoder,
                           @Qualifier("decoder") JwtDecoder decoder,
                           @Qualifier("details") UserDetailsService detailsService) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.detailsService = detailsService;
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
        return generateTokenGivenExpiration(auth, 3, ChronoUnit.DAYS);
    }

    public String generateAccessToken(Authentication auth){
        return generateTokenGivenExpiration(auth, 2, ChronoUnit.HOURS);
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

    public String extractUsername(String token) {
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsString("sub");
    }

    private Instant extractExpiration(String token) {
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsInstant("exp");
    }

    private boolean tokenCredentialsMatch(String token, UserDetails details) {
        String tokenUsername = extractUsername(token);
        String actualUsername = details.getUsername();
        boolean result = tokenUsername.equals(actualUsername);
        if (!result){
            throw new JWTCredentialsMismatchException("Credentials mismatch");
        }
        return true;
    }

    private boolean tokenNonExpired(String token) {
        Instant tokenExpiration = extractExpiration(token);
        boolean result = tokenExpiration.isBefore(Instant.now());
        if (!result){
            throw new JWTTokenExpiredException("Token expired");
        }
        return true;
    }

    public boolean isTokenValid(String token, UserDetails details){
        return tokenCredentialsMatch(token, details) && tokenNonExpired(token);
    }


    public String issueNewAccessToken(HttpServletRequest request, HttpServletResponse response){
        String refresh_token = extractTokenFromRequest(request);
        String username = extractUsername(refresh_token);
        UserDetails user =  detailsService.loadUserByUsername(username);
        Instant curr = Instant.now();
        String claim = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().
                issuer("self").
                issuedAt(curr).
                expiresAt(curr.plus(2, ChronoUnit.HOURS)).
                subject(username).
                claim("scope", claim).
                build();
        return encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();

    }




}
