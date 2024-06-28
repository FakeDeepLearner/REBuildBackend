package com.rebuild.backend.controllers.token_controllers;

import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.activation_tokens.ActivationTokenExpiredException;
import com.rebuild.backend.model.entities.TokenType;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.AccountActivationOrResetForm;
import com.rebuild.backend.model.responses.AccountActivationResponse;
import com.rebuild.backend.service.UserService;
import com.rebuild.backend.service.token_services.JWTTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class AccountActivationController {


    private final JWTTokenService tokenService;

    private final UserService userService;

    @Autowired
    public AccountActivationController(JWTTokenService tokenService,
                                       UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }


    @PostMapping("/api/activate")
    public void sendActivationEmail(@RequestBody AccountActivationOrResetForm activationForm){
        String newToken = tokenService.generateTokenGivenEmailAndExpiration(activationForm.email(),
                null ,activationForm.timeCount(), activationForm.timeUnit(), TokenType.ACTIVATE_ACCOUNT.typeName);
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
        return redirectUserToLogin(actualUser, token);
    }

    private ResponseEntity<AccountActivationResponse> redirectUserToLogin(User user, String activationToken){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/login");
        AccountActivationResponse body = new AccountActivationResponse(user.getEmail(), activationToken);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);

    }
}
