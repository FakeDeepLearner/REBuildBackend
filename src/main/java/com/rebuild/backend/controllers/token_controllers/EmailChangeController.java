package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.token_exceptions.email_change_tokens.EmailTokenExpiredException;
import com.rebuild.backend.exceptions.token_exceptions.email_change_tokens.EmailTokenMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.email_change_tokens.EmailTokenNotFoundException;
import com.rebuild.backend.model.entities.enums.TokenBlacklistPurpose;
import com.rebuild.backend.model.entities.enums.TokenType;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.auth_forms.EmailChangeForm;
import com.rebuild.backend.model.responses.EmailChangeResponse;
import com.rebuild.backend.service.user_services.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import com.rebuild.backend.utils.RedirectionUtility;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final RedirectionUtility redirectionUtility;


    @Autowired
    public EmailChangeController(UserService userService,
                                 JWTTokenService tokenService,
                                 TokenBlacklistService blacklistService, RedirectionUtility redirectionUtility) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.blacklistService = blacklistService;
        this.redirectionUtility = redirectionUtility;
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
    public ResponseEntity<EmailChangeResponse> changeUserEmail(@RequestParam(required = false) String token){
        if (token == null){
            throw new EmailTokenNotFoundException("There is no token in the url");
        }
        //Here, "subject" is the current email address of the user
        String subject = tokenService.extractSubject(token);
        String newMail = tokenService.extractNewMail(token);
        if(!tokenService.tokenNonExpired(token)){
            throw new EmailTokenExpiredException("This token has expired, please request a new one",
                    subject, newMail);
        }

        User user = userService.findByEmail(subject).orElseThrow(() -> new EmailTokenMismatchException("" +
                "A user with this email address hasn't been found"));

        userService.changeEmail(user.getId(), newMail);
        userService.invalidateAllSessions(user.getUsername());
        blacklistService.blacklistTokenFor(token, TokenBlacklistPurpose.EMAIL_CHANGE);
        return redirectionUtility.redirectUserToLogin(subject, newMail);
    }


}
