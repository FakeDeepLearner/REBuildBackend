package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.token_exceptions.ActivationTokenEmailMismatchException;
import com.rebuild.backend.exceptions.token_exceptions.ActivationTokenExpiredException;
import com.rebuild.backend.exceptions.token_exceptions.ActivationTokenNotFoundException;
import com.rebuild.backend.model.entities.EnableAccountToken;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.responses.AccountActivationResponse;
import com.rebuild.backend.repository.EnableTokenRepository;
import com.rebuild.backend.service.EnableTokenService;
import com.rebuild.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
public class AccountActivationController {

    private final EnableTokenService tokenService;

    private final EnableTokenRepository tokenRepository;

    private final UserService userService;

    @Autowired
    public AccountActivationController(EnableTokenService tokenService,
                                       EnableTokenRepository tokenRepository,
                                       UserService userService) {
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;
        this.userService = userService;
    }

    private EnableAccountToken createActivationToken(String email){
        String randomToken = tokenService.generateRandomActivateToken();
        EnableAccountToken newToken = new EnableAccountToken(randomToken, email,
                LocalDateTime.now().plus(10, ChronoUnit.MINUTES));
        return tokenRepository.save(newToken);
    }


    @PostMapping("/api/activate")
    public void sendActivationEmail(@RequestBody String email){
        EnableAccountToken newToken = createActivationToken(email);
        tokenService.sendActivationEmail(email, newToken.getToken());
    }

    @GetMapping("/api/activate/{token}")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<AccountActivationResponse> activateAccount(@PathVariable String token){
        EnableAccountToken foundToken = tokenRepository.findByToken(token).orElseThrow(() ->
                new ActivationTokenNotFoundException("No such token found"));
        if(tokenService.checkTokenExpiry(foundToken)){
            throw new ActivationTokenExpiredException("This token has expired");
        }
        User actualUser = userService.findByEmail(foundToken.getEmailFor()).orElseThrow(() ->
                new ActivationTokenEmailMismatchException("A user with this email address hasn't been found"));
        actualUser.setEnabled(true);
        userService.save(actualUser);
        //Immediately remove the tokens once they are used
        tokenRepository.delete(foundToken);
        return redirectUserToLogin(actualUser, foundToken.getToken());
    }

    private ResponseEntity<AccountActivationResponse> redirectUserToLogin(User user, String activationToken){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/login");
        AccountActivationResponse body = new AccountActivationResponse(user.getEmail(), activationToken);
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(headers).body(body);

    }
}
