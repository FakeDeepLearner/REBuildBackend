package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.token_exceptions.ActivationTokenExpiredException;
import com.rebuild.backend.model.entities.EnableAccountToken;
import com.rebuild.backend.model.entities.ResetPasswordToken;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.AccountActivationOrResetForm;
import com.rebuild.backend.model.forms.PasswordResetForm;
import com.rebuild.backend.model.responses.PasswordResetResponse;
import com.rebuild.backend.repository.ResetTokenRepository;
import com.rebuild.backend.service.ResetTokenService;
import com.rebuild.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ResetPasswordController {

    private final ResetTokenService tokenService;

    private final ResetTokenRepository tokenRepository;

    private final UserService userService;


    @Autowired
    public ResetPasswordController(ResetTokenService tokenService,
                                   ResetTokenRepository tokenRepository,
                                   UserService userService) {
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;
        this.userService = userService;
    }

    @PostMapping("/api/reset")
    public void sendResetEmail(@RequestBody AccountActivationOrResetForm resetForm){
        ResetPasswordToken newToken = tokenService.createResetToken(resetForm.email(),
                resetForm.timeCount(), resetForm.timeUnit());
        tokenService.sendResetEmail(resetForm.email(), newToken.getActualToken(),
                resetForm.timeCount(), resetForm.timeUnit());
    }

    //TODO: Put actual exceptions in the orElseThrow methods
    @PostMapping("/api/reset/{token}")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<PasswordResetResponse> changeUserPassword(@PathVariable String token,
                                             @Valid @RequestBody PasswordResetForm resetForm){
        ResetPasswordToken foundToken = tokenRepository.findByActualToken(token).
                orElseThrow();
        if (tokenService.checkTokenExpiry(foundToken)){
            tokenRepository.delete(foundToken);
            //TODO: Change this once a more streamlined exception process for 2 tokens is built
            throw new ActivationTokenExpiredException("This reset token has expired", token);
        }
        User foundUser = userService.findByEmail(foundToken.getEmailFor()).orElseThrow();
        String oldPassword = foundUser.getPassword();
        //Since findByEmail returns a reference, the change in the password is automatically reflected in foundUser
        userService.changePassword(foundUser.getId(), resetForm.newPassword());
        tokenRepository.delete(foundToken);
        return redirectUserToLogin(foundUser, oldPassword);


    }


    private ResponseEntity<PasswordResetResponse> redirectUserToLogin(User user,
                                                                      String oldPassword){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/login");
        PasswordResetResponse body = new PasswordResetResponse(oldPassword, user.getPassword());
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);
    }

}
