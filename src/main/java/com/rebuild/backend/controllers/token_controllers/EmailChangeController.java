package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.model.entities.enums.TokenBlacklistPurpose;
import com.rebuild.backend.model.entities.enums.TokenType;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.EmailChangeForm;
import com.rebuild.backend.model.responses.EmailChangeResponse;
import com.rebuild.backend.service.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;

@RestController
public class EmailChangeController {

    private final UserService userService;

    private final JWTTokenService tokenService;

    private final TokenBlacklistService blacklistService;


    @Autowired
    public EmailChangeController(UserService userService,
                                 JWTTokenService tokenService,
                                 TokenBlacklistService blacklistService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/api/change_email")
    public void sendEmailChange(@RequestBody EmailChangeForm changeForm,
                                @AuthenticationPrincipal UserDetails currentDetails){
        String oldEmail = currentDetails.getUsername();
        String newToken = tokenService.generateTokenForEmailChange(oldEmail, changeForm.newEmail(),15L,
                ChronoUnit.MINUTES, TokenType.CHANGE_EMAIL.typeName);
        tokenService.sendProperEmail(newToken, 15L, ChronoUnit.MINUTES);
    }

    @GetMapping("/api/change_email")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<EmailChangeResponse> changeUserEmail(@RequestParam String token){
        if(!tokenService.tokenNonExpired(token)){
            throw new RuntimeException();
        }
        String subject = tokenService.extractSubject(token);
        String newMail = tokenService.extractNewMail(token);
        User user = userService.findByEmail(subject).orElseThrow(() -> new UserNotFoundException("Email not found"));

        userService.changeEmail(user.getId(), newMail);
        userService.invalidateAllSessions(user.getUsername());
        blacklistService.blacklistTokenFor(token, TokenBlacklistPurpose.EMAIL_CHANGE);
        return redirectUserToLogin(subject, newMail);
    }

    private ResponseEntity<EmailChangeResponse> redirectUserToLogin(String oldEmail, String newEmail){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/login");
        EmailChangeResponse body = new EmailChangeResponse(oldEmail, newEmail);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);

    }

}
