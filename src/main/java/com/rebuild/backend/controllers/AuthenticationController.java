package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.forms.LoginForm;
import com.rebuild.backend.model.forms.SignupForm;
import com.rebuild.backend.model.responses.AuthResponse;
import com.rebuild.backend.service.JWTTokenService;
import com.rebuild.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
        tokenService.addTokenPair(accessToken, refreshToken);
        return new AuthResponse(accessToken, refreshToken);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public ResponseEntity<User> processSignup(@Valid @RequestBody SignupForm signupForm){
        User createdUser =
                userService.createNewUser(signupForm.username(), signupForm.password(), signupForm.email());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Location", "/home/" + createdUser.getId());
        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(responseHeaders).body(createdUser);
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
