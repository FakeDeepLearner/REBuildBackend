package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenExpiredException;
import com.rebuild.backend.model.entities.enums.TokenBlacklistPurpose;
import com.rebuild.backend.model.entities.enums.TokenType;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.dto_forms.AccountActivationDTO;
import com.rebuild.backend.model.responses.AccountActivationResponse;
import com.rebuild.backend.service.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.token_services.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

    @Autowired
    public AccountActivationController(JWTTokenService tokenService,
                                       UserService userService,
                                       TokenBlacklistService blacklistService,
                                       AuthenticationManager authManager) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.blacklistService = blacklistService;
        this.authManager = authManager;
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
    public ResponseEntity<AccountActivationResponse> activateAccount(@RequestParam String token){
        String userEmail = tokenService.extractSubject(token);
        if(!tokenService.tokenNonExpired(token)){
            throw new ActivationTokenExpiredException("This link has expired, please click this " +
                    "button to request a new token", token);
        }
        User actualUser = userService.findByEmail(userEmail).orElseThrow(() ->
                new ActivationTokenEmailMismatchException("A user with this email address hasn't been found"));
        actualUser.setEnabled(true);
        userService.save(actualUser);
        userService.invalidateAllSessions(actualUser.getUsername());
        blacklistService.blacklistTokenFor(token, TokenBlacklistPurpose.ACCOUNT_ACTIVATION);

        boolean remembered = tokenService.extractRemember(token);
        String enteredPassword = tokenService.extractPassword(token);

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userEmail, enteredPassword)
        );
        String accessToken = tokenService.generateAccessToken(auth);
        String refreshToken = tokenService.generateRefreshToken(auth);
        tokenService.addTokenPair(accessToken, refreshToken);
        return redirectUserToLogin(actualUser, remembered, accessToken, refreshToken);
    }

    private ResponseEntity<AccountActivationResponse> redirectUserToLogin(User user,
                                                                          boolean remembered, String accessToken,
                                                                          String refreshToken){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/home/" + user.getId());
        if(remembered){
            headers.add(HttpHeaders.SET_COOKIE, accessToken);
        }
        AccountActivationResponse body = new AccountActivationResponse(user.getEmail(), accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);

    }
}
