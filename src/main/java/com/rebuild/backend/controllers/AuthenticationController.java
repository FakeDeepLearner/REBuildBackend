package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.service.token_services.OTPService;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.service.user_services.UserService;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationManager authManager;

    private final UserService userService;

    private final OTPService otpService;

    @Autowired
    public AuthenticationController(AuthenticationManager authManager,
                                    UserService userService,
                                    OTPService otpService) {
        this.authManager = authManager;
        this.userService = userService;
        this.otpService = otpService;
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


    private VerificationCheck verifyEnteredCode(SignupForm signupForm, String enteredOTP){
        if(signupForm.otpChannel().equals("sms")){
            return otpService.validateEnteredOTP(signupForm.phoneNumber(), enteredOTP);
        }
        else {
            return otpService.validateEnteredOTP(signupForm.email(), enteredOTP);
        }
    }

    @PostMapping("/signup/initialize")
    public ResponseEntity<String> initializeSignup(@Valid @RequestBody SignupForm signupForm){

        //Do preliminary checks. If any of them fail, abort the signup immediately
        if (!signupForm.password().equals(signupForm.repeatedPassword())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match");
        }

        Optional<User> foundUser = userService.findByEmail(signupForm.email());

        if(foundUser.isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This email is already in use");
        }


        if(signupForm.otpChannel().equals("sms") && signupForm.phoneNumber() == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have selected for a code to be sent to " +
                    "your phone, but you haven't entered a phone number");
        }

        switch(signupForm.otpChannel())
        {
            case "sms":
                otpService.generateOTPCode(signupForm.phoneNumber(), "sms");
                break;
            case "email":
                otpService.generateOTPCode(signupForm.email(), "email");
                break;

        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("A one-time code has been sent to your " +
                "desired channel, please enter it to finalize the signup process");
    }


    private ResponseEntity<String> signUpNewUser(SignupForm signupForm, HttpServletRequest request)
    {
        //This block of code deals with creating the user in the database.
        try {
            User createdUser = userService.createNewUser(signupForm);
        }
        catch (DataIntegrityViolationException integrityViolationException){
            Throwable cause = integrityViolationException.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                String violatedConstraint = violationException.getConstraintName();
                switch (violatedConstraint){
                    case "uk_email" -> {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("This email already exists");

                    }
                    case "uk_phone_number" -> {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("This phone number already exists");

                    }
                    case "uk_forum_username" -> {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("This forum username already exists");
                    }

                    //This should never happen
                    case null -> {}

                    default -> throw new IllegalStateException("Unexpected value: " + violatedConstraint);
                }
            }
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

    @PostMapping("/signup/finalize")
    public ResponseEntity<String> finalizeSignup(@Valid @RequestBody SignupForm signupForm, HttpServletRequest request,
                                                 @RequestParam String enteredOtpCode){


        VerificationCheck verificationCheck = verifyEnteredCode(signupForm, enteredOtpCode);

        String status = verificationCheck.getStatus();

        switch (status){
            case "approved" -> {
                return signUpNewUser(signupForm, request);
            }

            case "failed" -> {
                return ResponseEntity.badRequest().body("You have entered the wrong code");
            }

            case "expired" -> {
                //We send the notification again via the same channel that the user used to originally obtain it.
                String oldChannel = verificationCheck.getChannel().toString();
                if (oldChannel.equals("sms"))
                {
                    otpService.generateOTPCode(signupForm.phoneNumber(), "sms");
                }
                else if(oldChannel.equals("email"))
                {
                    otpService.generateOTPCode(signupForm.email(), "email");
                }
                return ResponseEntity.status(HttpStatus.GONE).
                        body("The passcode that you requested has expired, " +
                        "we have sent you a new one, please enter it");
            }

            case "max_attempts_reached" -> {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).
                        body("You have reached the maximum number of attempts.");
            }

        }
        return ResponseEntity.internalServerError().body("An unexpected error has occurred");

    }

}
