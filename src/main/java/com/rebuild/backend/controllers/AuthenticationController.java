package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.AccountActivationDTO;
import com.rebuild.backend.model.forms.LoginForm;
import com.rebuild.backend.model.forms.SignupForm;
import com.rebuild.backend.model.responses.AuthResponse;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.temporal.ChronoUnit;

@RestController
public class AuthenticationController {

    private final JWTTokenService tokenService;

    private final AuthenticationManager authManager;

    private final UserService userService;

    @Autowired
    public AuthenticationController(JWTTokenService tokenService,
                                    AuthenticationManager authManager,
                                    UserService userService) {
        this.tokenService = tokenService;
        this.authManager = authManager;
        this.userService = userService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AuthResponse> processLogin(@Valid @RequestBody LoginForm form){
        userService.validateLoginCredentials(form);
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.email(), form.password()));
        String accessToken = tokenService.generateAccessToken(auth);
        String refreshToken = tokenService.generateRefreshToken(auth);
        tokenService.addTokenPair(accessToken, refreshToken);
        AuthResponse responseBody = new AuthResponse(accessToken, refreshToken);
        if(form.remember()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, accessToken);
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(responseBody);
        }
        else{
            return ResponseEntity.ok(responseBody);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> processSignup(@Valid @RequestBody SignupForm signupForm){

        User createdUser =
                userService.createNewUser(signupForm.password(), signupForm.email());
        AccountActivationDTO form  =
                new AccountActivationDTO(createdUser.getEmail(), signupForm.password(), 20L, ChronoUnit.MINUTES,
                         signupForm.remember());
        HttpEntity<AccountActivationDTO> body = new HttpEntity<>(form);
        return new RestTemplate().exchange("/api/activate", HttpMethod.POST, body, Void.TYPE);
    }

    @PostMapping("/api/refresh_token")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public AuthResponse generateRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = tokenService.issueNewAccessToken(request);
        String refreshToken = tokenService.extractTokenFromRequest(request);
        tokenService.addTokenPair(newAccessToken, refreshToken);
        String originalUrl = request.getContextPath();
        //Redirect back to where the request originally came from.
        response.setStatus(303);
        response.addHeader("Location", originalUrl);
        return new AuthResponse(newAccessToken, refreshToken);
    }

}
