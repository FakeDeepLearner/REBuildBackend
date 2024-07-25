package com.rebuild.backend.service.token_services;

import com.rebuild.backend.config.properties.MailAppCredentials;
import com.rebuild.backend.exceptions.jwt_exceptions.JWTCredentialsMismatchException;
import com.rebuild.backend.exceptions.jwt_exceptions.JWTTokenExpiredException;
import com.rebuild.backend.exceptions.jwt_exceptions.NoJWTTokenException;
import com.rebuild.backend.model.entities.TokenType;
import com.rebuild.backend.utils.EmailOrUsernameDecider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service("jwt")
public class JWTTokenService {
    private final JwtEncoder encoder;

    private final JwtDecoder decoder;

    private final EmailOrUsernameDecider decider;

    private final Map<String, String> accessAndRefreshTokens = new ConcurrentHashMap<>();

    private final Map<String, String> refreshAndAccessTokens = new ConcurrentHashMap<>();

    private final JavaMailSender mailSender;

    private final MailAppCredentials credentials;

    @Autowired
    public JWTTokenService(@Qualifier("encoder") JwtEncoder encoder,
                           @Qualifier("decoder") JwtDecoder decoder,
                           EmailOrUsernameDecider decider,
                           @Qualifier("mailSender") JavaMailSender mailSender,
                           MailAppCredentials credentials) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.decider = decider;
        this.mailSender = mailSender;
        this.credentials = credentials;
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

    public String generateGivenBothEmails(String oldEmail, String newEmail, long amount ,ChronoUnit unit){
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "email_change");

        Instant curr = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().
                issuer("self").
                issuedAt(curr).
                expiresAt(curr.plus(amount, unit)).
                subject(oldEmail).
                claims((claimSet) -> claims.put("new_email", newEmail)).
                build();
        return encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }


    public String generateTokenForEmailChange(String email, String newEmail, long amount,
                                              ChronoUnit unit,
                                              String purpose){
        if(purpose.equals(TokenType.CHANGE_EMAIL.typeName)){
            return generateGivenBothEmails(email, newEmail, amount, unit);
        }
        else{
            throw new NullPointerException("Token type must be change_email");
        }


    }


    public String generateTokenForPasswordReset(String email, long amount, ChronoUnit unit, String purpose){
        if(!Objects.equals(purpose, TokenType.CHANGE_PASSWORD.typeName)){
            throw new NullPointerException("purpose must be change_password");
        }

        Instant curr = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().
                issuer("self").
                issuedAt(curr).
                expiresAt(curr.plus(amount, unit)).
                subject(email).
                build();
        return encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public String generateTokenForAccountActivation(String email,
                                                    long amount,
                                                    ChronoUnit unit,
                                                    String purpose,
                                                    boolean remember,
                                                    String password){
        if(!Objects.equals(purpose, TokenType.ACTIVATE_ACCOUNT.typeName)){
            throw new NullPointerException("Purpose must be activate_account");
        }
        Instant curr = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().
                issuer("self").
                issuedAt(curr).
                expiresAt(curr.plus(amount, unit)).
                subject(email).
                claim("password", password).
                claim("rememberMe", remember).
                build();
        return encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public void addTokenPair(String access, String refresh){
        accessAndRefreshTokens.put(access, refresh);
        refreshAndAccessTokens.put(refresh, access);
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

    public String extractPassword(String token){
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsString("password");
    }


    public boolean extractRemember(String token){
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsBoolean("rememberMe");
    }

    public String extractNewMail(String token){
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsString("new_email");
    }

    private Instant extractExpiration(String token) {
        Jwt allClaims = extractAllClaims(token);
        return allClaims.getClaimAsInstant("exp");
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
        String correspondingRefreshToken = accessAndRefreshTokens.get(token);
        boolean result = tokenExpiration.isBefore(Instant.now());
        if (!result){
            throw new JWTTokenExpiredException("Token expired", correspondingRefreshToken);
        }
        return true;
    }

    public boolean isTokenValid(String token, UserDetails details){
        return tokenCredentialsMatch(token, details) && tokenNonExpired(token);
    }

    private void removeTokenPair(String refresh){
        String correspondingAccess = refreshAndAccessTokens.get(refresh);
        accessAndRefreshTokens.remove(correspondingAccess);
        refreshAndAccessTokens.remove(refresh);
    }

    public String issueNewAccessToken(HttpServletRequest request){
        String refresh_token = extractTokenFromRequest(request);
        removeTokenPair(refresh_token);
        String subject = extractSubject(refresh_token);
        UserDetails details = decider.createProperUserDetails(subject);
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

    public void sendProperEmail(String token, Long timeAmount, ChronoUnit timeUnit){
       Jwt allClaims = extractAllClaims(token);
       String purpose = allClaims.getClaimAsString("purpose");
       String address = allClaims.getClaimAsString("sub");
       if(purpose.equals(TokenType.ACTIVATE_ACCOUNT.typeName)){
           sendActivationEmail(address, token, timeAmount, timeUnit);
       }
       if(purpose.equals(TokenType.CHANGE_PASSWORD.typeName)){
           sendResetEmail(address, token, timeAmount, timeUnit);
       }
       if(purpose.equals(TokenType.CHANGE_EMAIL.typeName)){
           sendEmailChange(address, token, timeAmount, timeUnit);
       }
    }

    private void prePrepareMessage(SimpleMailMessage message, String to){
        message.setFrom(credentials.address());
        message.setTo(to);
        message.setReplyTo(credentials.replyTo());
    }

    private void sendActivationEmail(String addressFor, String activationToken,
                                    Long timeCount, ChronoUnit timeUnit){
        SimpleMailMessage mailMessage =  new SimpleMailMessage();
        prePrepareMessage(mailMessage, addressFor);
        mailMessage.setSubject("Account activation");
        String activationUrl = "/api/activate?token=" + activationToken;
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

    private void sendResetEmail(String addressFor, String resetToken, Long timeCount, ChronoUnit timeUnit){
        SimpleMailMessage mailMessage =  new SimpleMailMessage();
        prePrepareMessage(mailMessage, addressFor);
        mailMessage.setSubject("Reset your password");
        String activationUrl = "/api/reset?token=" + resetToken;
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

    private void sendEmailChange(String addressFor, String activationToken,
                                    Long timeCount, ChronoUnit timeUnit){
        SimpleMailMessage mailMessage =  new SimpleMailMessage();
        prePrepareMessage(mailMessage, addressFor);
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
