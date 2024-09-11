package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenExpiredException;
import com.rebuild.backend.exceptions.token_exceptions.reset_tokens.ResetTokenNotFoundException;
import com.rebuild.backend.model.entities.enums.TokenBlacklistPurpose;
import com.rebuild.backend.model.entities.enums.TokenType;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.dtos.jwt_tokens_dto.AccountActivationDTO;
import com.rebuild.backend.model.forms.auth_forms.PasswordResetForm;
import com.rebuild.backend.model.responses.PasswordResetResponse;
import com.rebuild.backend.service.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import com.rebuild.backend.utils.RedirectionUtility;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ResetPasswordController {



    private final UserService userService;

    private final JWTTokenService tokenService;

    private final TokenBlacklistService blacklistService;
    private final RedirectionUtility redirectionUtility;

    @Autowired
    public ResetPasswordController(
            UserService userService, JWTTokenService tokenService,
            TokenBlacklistService blacklistService, RedirectionUtility redirectionUtility) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.blacklistService = blacklistService;
        this.redirectionUtility = redirectionUtility;
    }

    @PostMapping("/api/reset")
    public void sendResetEmail(@RequestBody AccountActivationDTO resetForm){
        String newToken = tokenService.generateTokenForPasswordReset(
                resetForm.email(), resetForm.timeCount(), resetForm.timeUnit(),
                TokenType.CHANGE_PASSWORD.typeName
        );
        tokenService.sendProperEmail(newToken, resetForm.timeCount(), resetForm.timeUnit());
    }

    @PostMapping("/api/reset")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<PasswordResetResponse> changeUserPassword(@RequestParam(required = false) String token,
                                             @Valid @RequestBody PasswordResetForm resetForm){
        if(token == null){
            throw new ResetTokenNotFoundException("There is no token in the url");
        }
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
        return redirectionUtility.redirectUserToLogin(foundUser, oldPassword);
    }

}
