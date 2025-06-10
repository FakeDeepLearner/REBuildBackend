package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.conflict_exceptions.AccountCreationException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.service.user_services.UserService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
public class AuthenticationController {

    private final AuthenticationManager authManager;

    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthenticationManager authManager,
                                    UserService userService) {
        this.authManager = authManager;
        this.userService = userService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> processLogin(@Valid @RequestBody LoginForm form){
        userService.validateLoginCredentials(form);
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.email(), form.password()));

        Bucket userBucket = userService.returnUserBucket(form.email());

        ConsumptionProbe probe = userBucket.tryConsumeAndReturnRemaining(1L);

        if (probe.isConsumed()){
            return ResponseEntity.ok("Login successful");
        }
        else{
            long remainingTokens = probe.getRemainingTokens();
            double resetNanos = probe.getNanosToWaitForReset();

            int resetSeconds =  (int) Math.ceil(resetNanos / 1_000_000_000);

            int minutesRemaining = Math.floorDiv(resetSeconds, 60);
            int secondsRemaining = resetSeconds - minutesRemaining * 60;

            if (remainingTokens == 0){
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many login attempts, " +
                        "please retry in " + minutesRemaining + " minutes and " + secondsRemaining + " seconds");
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong credentials, " +
                        remainingTokens + " attempts left");
            }
        }
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
        return ResponseEntity.ok(creationResult.optionalResult().get());
    }

}
