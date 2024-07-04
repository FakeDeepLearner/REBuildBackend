package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenExpiredException;
import com.rebuild.backend.model.entities.TokenBlacklistPurpose;
import com.rebuild.backend.model.entities.TokenType;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.AccountActivationOrResetForm;
import com.rebuild.backend.model.forms.PasswordResetForm;
import com.rebuild.backend.model.responses.PasswordResetResponse;
import com.rebuild.backend.service.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ResetPasswordController {



    private final UserService userService;

    private final JWTTokenService tokenService;

    private final TokenBlacklistService blacklistService;

    @Autowired
    public ResetPasswordController(
            UserService userService, JWTTokenService tokenService,
            TokenBlacklistService blacklistService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.blacklistService = blacklistService;
    }

    @PostMapping("/api/reset")
    public void sendResetEmail(@RequestBody AccountActivationOrResetForm resetForm){
        String newToken = tokenService.generateTokenGivenEmailAndExpiration(resetForm.email(),
                null ,resetForm.timeCount(), resetForm.timeUnit(), TokenType.CHANGE_PASSWORD.typeName);
        tokenService.sendProperEmail(newToken, resetForm.timeCount(), resetForm.timeUnit());
    }

    @PostMapping("/api/reset")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<PasswordResetResponse> changeUserPassword(@RequestParam String token,
                                             @Valid @RequestBody PasswordResetForm resetForm){
        String userEmail = tokenService.extractSubject(token);
        if (!tokenService.tokenNonExpired(token)){
            throw new ResetTokenExpiredException("This reset token has expired", userEmail);
        }
        User foundUser = userService.findByEmail(userEmail).orElseThrow(
                () -> new ResetTokenEmailMismatchException("A user with this email address hasn't been found")
        );
        String oldPassword = foundUser.getPassword();
        //Since findByEmail returns a reference, the change in the password is automatically reflected in foundUser
        userService.changePassword(foundUser.getId(), resetForm.newPassword());
        userService.invalidateAllSessions(foundUser.getUsername());
        blacklistService.blacklistTokenFor(token, TokenBlacklistPurpose.PASSWORD_CHANGE);
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
