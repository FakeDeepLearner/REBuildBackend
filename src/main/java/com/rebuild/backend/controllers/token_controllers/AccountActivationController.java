package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenExpiredException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenNotFoundException;
import com.rebuild.backend.model.entities.enums.TokenBlacklistPurpose;
import com.rebuild.backend.model.entities.enums.TokenType;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.jwt_tokens_dto.AccountActivationDTO;
import com.rebuild.backend.model.responses.AccountActivationResponse;
import com.rebuild.backend.service.user_services.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import com.rebuild.backend.utils.RedirectionUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
public class AccountActivationController {


    private final JWTTokenService tokenService;

    private final UserService userService;

    private final TokenBlacklistService blacklistService;

    private final AuthenticationManager authManager;

    private final RedirectionUtility redirectionUtility;

    @Autowired
    public AccountActivationController(JWTTokenService tokenService,
                                       UserService userService,
                                       TokenBlacklistService blacklistService,
                                       AuthenticationManager authManager, RedirectionUtility redirectionUtility) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.blacklistService = blacklistService;
        this.authManager = authManager;
        this.redirectionUtility = redirectionUtility;
    }


    @PostMapping("/api/activate")
    public void sendActivationEmail(@RequestBody AccountActivationDTO activationForm){
        String newToken = tokenService.generateTokenForAccountActivation(activationForm.email(),
                activationForm.timeCount(), activationForm.timeUnit(),
                TokenType.ACTIVATE_ACCOUNT.typeName, activationForm.remember(), activationForm.password());
        tokenService.sendProperEmail(newToken, activationForm.timeCount(), activationForm.timeUnit());
    }

    @GetMapping("/api/activate")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<AccountActivationResponse> activateAccount(@RequestParam(required = false) String token){
        if(token == null){
            throw new ActivationTokenNotFoundException("There is no token in the url");
        }
        String userEmail = tokenService.extractSubject(token);
        boolean remembered = tokenService.extractRemember(token);
        String enteredPassword = tokenService.extractPassword(token);
        if(!tokenService.tokenNonExpired(token)){
            throw new ActivationTokenExpiredException("This link has expired, please click this " +
                    "button to request a new token", userEmail, remembered, enteredPassword);
        }
        User actualUser = userService.findByEmail(userEmail).orElseThrow(() ->
                new ActivationTokenEmailMismatchException("A user with this email address hasn't been found"));
        actualUser.setEnabled(true);
        userService.save(actualUser);
        userService.invalidateAllSessions(actualUser.getUsername());
        blacklistService.blacklistTokenFor(token, TokenBlacklistPurpose.ACCOUNT_ACTIVATION);

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail, enteredPassword)
        );
        String accessToken = tokenService.generateAccessToken(auth);
        String refreshToken = tokenService.generateRefreshToken(auth);
        return redirectionUtility.redirectUserToLogin(actualUser, remembered, accessToken, refreshToken);
    }
}
