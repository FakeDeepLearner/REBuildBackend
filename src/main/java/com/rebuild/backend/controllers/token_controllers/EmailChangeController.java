package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.model.entities.TokenBlacklistPurpose;
import com.rebuild.backend.model.entities.TokenType;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.EmailChangeForm;
import com.rebuild.backend.model.responses.EmailChangeResponse;
import com.rebuild.backend.service.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import com.rebuild.backend.utils.EmailOrUsernameDecider;
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

    private final EmailOrUsernameDecider decider;

    @Autowired
    public EmailChangeController(UserService userService,
                                 JWTTokenService tokenService,
                                 TokenBlacklistService blacklistService, EmailOrUsernameDecider decider) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.blacklistService = blacklistService;
        this.decider = decider;
    }

    @PostMapping("/api/change_email")
    public void sendEmailChange(@RequestBody EmailChangeForm changeForm,
                                @AuthenticationPrincipal UserDetails currentDetails){
        String username = currentDetails.getUsername();
        User actualUser = userService.findByUsername(username).
                orElseThrow(() -> new UserNotFoundException("Username" + username + "not found"));
        String newToken = tokenService.generateTokenForEmailChange(actualUser.getEmail(), changeForm.newEmail(),15L,
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
        User user;
        if (decider.isInputEmail(subject)) {
            user = userService.findByEmail(subject).
                    orElseThrow(() -> new UserNotFoundException("Email not found"));
        }
        else{
            user = userService.findByUsername(subject).
                    orElseThrow(() -> new UserNotFoundException("Username not found"));
        }

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
