package com.rebuild.backend.utils;

import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.responses.AccountActivationResponse;
import com.rebuild.backend.model.responses.EmailChangeResponse;
import com.rebuild.backend.model.responses.PasswordResetResponse;
import com.rebuild.backend.service.token_services.JWTTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedirectionUtility {

    private final AppUrlBase urlBase;
    private final JWTTokenService tokenService;

    @Autowired
    public RedirectionUtility(AppUrlBase urlBase, JWTTokenService tokenService) {
        this.urlBase = urlBase;
        this.tokenService = tokenService;
    }

    public ResponseEntity<AccountActivationResponse> redirectUserToLogin(User user,
                                                                          boolean remembered, String accessToken,
                                                                          String refreshToken){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", urlBase.baseUrl() + "/home/" + user.getId());
        Duration tokenExpiryDuration = tokenService.getExpiryDuration(refreshToken);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken).
                secure(true).
                httpOnly(true).
                path("/").
                sameSite("Strict").
                maxAge(tokenExpiryDuration).
                build();
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        AccountActivationResponse body = new AccountActivationResponse(user.getEmail(), accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);

    }

    public ResponseEntity<EmailChangeResponse> redirectUserToLogin(String oldEmail, String newEmail){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", urlBase.baseUrl() + "/login");
        EmailChangeResponse body = new EmailChangeResponse(oldEmail, newEmail);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);

    }

    public ResponseEntity<PasswordResetResponse> redirectUserToLogin(User user,
                                                                      String oldPassword){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", urlBase.baseUrl() + "/login");
        PasswordResetResponse body = new PasswordResetResponse(oldPassword, user.getPassword());
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);
    }

}
