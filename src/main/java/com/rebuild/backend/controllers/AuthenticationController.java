package com.rebuild.backend.controllers;

import com.rebuild.backend.model.forms.LoginForm;
import com.rebuild.backend.model.responses.AuthResponse;
import com.rebuild.backend.service.JWTTokenService;
import com.rebuild.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
    public AuthResponse processLogin(@Valid @RequestBody LoginForm form){
        userService.validateLoginCredentials(form);
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.emailOrUsername(), form.password()));
        String accessToken = tokenService.generateAccessToken(auth);
        String refreshToken = tokenService.generateRefreshToken(auth);
        return new AuthResponse(accessToken, refreshToken);
    }
}
