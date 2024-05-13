package com.rebuild.backend.controller;

import com.rebuild.backend.model.forms.LoginForm;
import com.rebuild.backend.service.JWTTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final JWTTokenService tokenService;

    private final AuthenticationManager authManager;

    @Autowired
    public AuthenticationController(JWTTokenService tokenService, AuthenticationManager authManager) {
        this.tokenService = tokenService;
        this.authManager = authManager;
    }

    @PostMapping("/generatetoken")
    public String tokenFor(@RequestBody LoginForm form){
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(
                form.emailOrUsername(), form.password()));
        return tokenService.generateJWTToken(auth);

    }
}
