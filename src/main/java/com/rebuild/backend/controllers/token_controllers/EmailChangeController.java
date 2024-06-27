package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.model.entities.TokenType;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.EmailChangeForm;
import com.rebuild.backend.model.responses.AccountActivationResponse;
import com.rebuild.backend.model.responses.EmailChangeResponse;
import com.rebuild.backend.service.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RestController
public class EmailChangeController {

    private final UserService userService;

    private final JWTTokenService tokenService;

    @Autowired
    public EmailChangeController(UserService userService,
                                 JWTTokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/api/change_email")
    public void sendEmailChange(@RequestBody EmailChangeForm changeForm,
                                @AuthenticationPrincipal UserDetails currentDetails){
        String username = currentDetails.getUsername();
        User actualUser = userService.findByUsername(username).
                orElseThrow(() -> new UserNotFoundException("Username" + username + "not found"));
        String newToken = tokenService.generateTokenGivenEmailAndExpiration(actualUser.getEmail(),
                changeForm.newEmail(), 15, ChronoUnit.MINUTES, TokenType.CHANGE_EMAIL.typeName);
        tokenService.sendProperEmail(newToken, 15L, ChronoUnit.MINUTES);
    }

    @GetMapping("/api/change_email")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<EmailChangeResponse> changeUserEmail(@RequestParam String token){
        String oldMail = tokenService.extractSubject(token);
        String newMail = tokenService.extractNewMail(token);
        User actualUser = userService.findByEmail(oldMail).
                orElseThrow(() -> new UserNotFoundException("Email not found"));
        userService.changeEmail(actualUser.getId(), newMail);
        return redirectUserToLogin(oldMail, newMail);
    }

    private ResponseEntity<EmailChangeResponse> redirectUserToLogin(String oldEmail, String newEmail){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/login");
        EmailChangeResponse body = new EmailChangeResponse(oldEmail, newEmail);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);

    }

}
