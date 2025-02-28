package com.rebuild.backend.controllers;

import com.rebuild.backend.config.properties.AppUrlBase;
import com.rebuild.backend.exceptions.conflict_exceptions.AccountCreationException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.model.forms.dtos.jwt_tokens_dto.AccountActivationDTO;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.service.token_services.JWTTokenService;
import com.rebuild.backend.service.user_services.UserService;
import com.rebuild.backend.utils.password_utils.RandomPasswordGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@RestController
public class AuthenticationController {

    private final JWTTokenService tokenService;

    private final AuthenticationManager authManager;

    private final UserService userService;

    private final RandomPasswordGenerator passwordGenerator;

    private final AppUrlBase urlBase;

    @Autowired
    public AuthenticationController(JWTTokenService tokenService,
                                    AuthenticationManager authManager,
                                    UserService userService,
                                    RandomPasswordGenerator passwordGenerator, AppUrlBase urlBase) {
        this.tokenService = tokenService;
        this.authManager = authManager;
        this.userService = userService;
        this.passwordGenerator = passwordGenerator;
        this.urlBase = urlBase;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> processLogin(@Valid @RequestBody LoginForm form){
        userService.validateLoginCredentials(form);
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.email(), form.password()));
        String accessToken = tokenService.generateAccessToken(auth);
        String refreshToken = tokenService.generateRefreshToken(auth);
        Duration tokenExpiryDuration = tokenService.getExpiryDuration(refreshToken);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken).
                secure(true).
                httpOnly(true).
                path("/").
                sameSite("Strict").
                maxAge(tokenExpiryDuration).
                build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(accessToken);

    }

    @PostMapping("/signup")
    public ResponseEntity<?> processSignup(@Valid @RequestBody SignupForm signupForm){

        OptionalValueAndErrorResult<User> creationResult =
                userService.createNewUser(signupForm);
        if(creationResult.optionalResult().isEmpty()){
            if(creationResult.optionalError().isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error has occurred");
            }
            throw new AccountCreationException(creationResult.optionalError().get());
        }

        AccountActivationDTO form  =
                new AccountActivationDTO(creationResult.optionalResult().get().getEmail(), signupForm.password(),
                        20L, ChronoUnit.MINUTES,
                         signupForm.remember());
        String urlToMakePost = UriComponentsBuilder.
                fromHttpUrl(urlBase.baseUrl() + "/api/activate").toUriString();
        URI postingURI = URI.create(urlToMakePost);
        RequestEntity<AccountActivationDTO> request = RequestEntity.post(postingURI).body(form);

        return new RestTemplate().exchange(request, Void.TYPE);
    }

    @PostMapping("/api/refresh_token")
    @ResponseStatus(HttpStatus.SEE_OTHER)
    public void refreshExpiredToken(HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = tokenService.issueNewAccessToken(request);
        String originalUrl = request.getRequestURL().toString();
        //Redirect back to where the request originally came from.
        response.setStatus(HttpStatus.SEE_OTHER.value());
        response.addHeader("Location", originalUrl);
        response.addHeader("Authorization", "Bearer " + newAccessToken);
    }


    @GetMapping("/api/random_password")
    @ResponseStatus(HttpStatus.OK)
    public String generateRandomPassword(){
        //The generator already has all the data it needs
        return passwordGenerator.generateRandom();
    }

}
