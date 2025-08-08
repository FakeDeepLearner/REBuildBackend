package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.conflict_exceptions.AccountCreationException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.service.user_services.UserService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
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
    public ResponseEntity<String> processLogin(@Valid @RequestBody LoginForm form, HttpServletRequest request){

        Bucket userBucket = userService.returnUserBucket(form.email());

        ConsumptionProbe probe = userBucket.tryConsumeAndReturnRemaining(1L);

        if (probe.isConsumed()){
            boolean userCanLogin = userService.validateLoginCredentials(form);
            if (!userCanLogin){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid username or password");
            }
            loginHelper(form, request);

            return ResponseEntity.ok("Login successful");
        }

        // We only hit the database if the user has enough attempts remaining to log in in the first place.
        // If they don't, we don't waste time for a login attempt that will fail anyway
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


    private void loginHelper(LoginForm form, HttpServletRequest request){
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.email(), form.password())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true);
    }


    @PostMapping("/signup")
    public ResponseEntity<String> processSignup(@Valid @RequestBody SignupForm signupForm, HttpServletRequest request){

        //This block of code deals with creating the user in the database.
        OptionalValueAndErrorResult<User> creationResult =
                userService.createNewUser(signupForm);
        if(creationResult.optionalResult().isEmpty()){
            if(creationResult.optionalError().isEmpty()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                        body("An unexpected error has occurred");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(creationResult.optionalError().get());
        }

        // This block of code is used to automatically authenticate the user that just signed up,
        // ensuring a seamless UX
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signupForm.email(), signupForm.password())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true);

        return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully");

    }

}
