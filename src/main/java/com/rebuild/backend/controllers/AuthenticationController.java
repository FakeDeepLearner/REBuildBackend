package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.CredentialValidationDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/auth")
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

    @PostMapping("/login/initialize")
    public ResponseEntity<?> initializeLogin(@Valid @RequestBody LoginForm loginForm,
                                             @RequestParam(name = "g-recaptcha-response") String userResponse,
                                             HttpServletRequest request) {
        if (userService.captchaFailed(userResponse, request.getRemoteAddr())) {
            return ResponseEntity.badRequest().body("Invalid captcha response, please try again");
        }

        CredentialValidationDTO credentialValidationDTO = userService.validateLoginCredentials(loginForm);

        if (credentialValidationDTO.resentOtp())
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Your account has been " +
                    "locked due to prolonged inactivity. A code has been sent to your email or your phone number. " +
                    "Please enter it to unlock your account");
        }
        Bucket userBucket = userService.returnUserBucket(credentialValidationDTO.userEmail());

        ConsumptionProbe probe = userBucket.tryConsumeAndReturnRemaining(1L);

        if (probe.isConsumed()){
            boolean userCanLogin = credentialValidationDTO.canLogin();
            if (!userCanLogin){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid username or password");
            }

            //If we have consumed the probe, we send a code to the user's preferred channel
            otpService.generateOTPCode(loginForm.emailOrPhone(), credentialValidationDTO.userChannel());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body("A code has been sent to the email or " +
                    "phone number that you entered. Please enter that code to finalize your login");
        }


        // If the user has hit the rate limit, then we don't bother with
        // sending and verifying that code.
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


    @PostMapping("/login/finalize")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> finalizeLogin(@Valid @RequestBody LoginForm form,
                                               @RequestParam(name = "remember-me") boolean remember,
    @RequestParam(name = "code") String enteredOtpCode,
    HttpServletRequest request){
        VerificationCheck verificationCheck = verifyEnteredCode(form, enteredOtpCode);

        String status = verificationCheck.getStatus();

        switch (status){
            case "approved" -> {
                loginHelper(form, request);
                return ResponseEntity.ok().body("Login successful, redirecting you to your home page");
            }

            case "failed" -> {
                return ResponseEntity.badRequest().body("You have entered the wrong code");
            }

            case "expired" -> {
                //We send the notification again via the same channel that the user used to originally obtain it.
                String oldChannel = verificationCheck.getChannel().toString();
                otpService.generateOTPCode(form.emailOrPhone(), oldChannel);
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

    @PostMapping("/unlock_account")
    public ResponseEntity<?> unlockAccount(@Valid @RequestBody LoginForm form,
                                           @RequestParam String enteredOTPCode, HttpServletRequest request)
    {
        VerificationCheck verificationCheck = verifyEnteredCode(form, enteredOTPCode);
        String status = verificationCheck.getStatus();

        switch (status){
            case "approved" -> {
                userService.unlockUser(form.emailOrPhone());
                loginHelper(form, request);
                return ResponseEntity.ok().body("Account unlocked successfully, logging you in");
            }

            case "failed" -> {
                return ResponseEntity.badRequest().body("You have entered the wrong code");
            }

            case "expired" -> {
                //We send the notification again via the same channel that the user used to originally obtain it.
                String oldChannel = verificationCheck.getChannel().toString();
                otpService.generateOTPCode(form.emailOrPhone(), oldChannel);
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


    private void loginHelper(LoginForm form, HttpServletRequest request){
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.emailOrPhone(), form.password())
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

    private VerificationCheck verifyEnteredCode(LoginForm signupForm, String enteredOTP){
        return otpService.validateEnteredOTP(signupForm.emailOrPhone(), enteredOTP);
    }

    @PostMapping("/signup/initialize")
    public ResponseEntity<String> initializeSignup(@Valid @RequestBody SignupForm signupForm,
                                                   @RequestParam(name = "g-recaptcha-response") String userResponse,
                                                   HttpServletRequest request) throws IOException, InterruptedException {
        
        if (userService.captchaFailed(userResponse, request.getRemoteAddr())) {
            return ResponseEntity.badRequest().body("Invalid captcha response, please try again");
        }

        //Do preliminary checks. If any of them fail, abort the signup immediately
        if (!signupForm.password().equals(signupForm.repeatedPassword())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match");
        }

        if (signupForm.password().length() < 10)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body("Password is too short, please ensure it has a length of at least 10");
        }

        if (!signupForm.forcePassword() && userService.passwordFoundInDataBreach(signupForm.password()))
        {
            return ResponseEntity.badRequest().body("The password you entered was found in a data breach." +
                    "We strongly recommend that you choose a different one.");
        }


        if((signupForm.otpChannel().equals("sms") || signupForm.otpChannel().equals("call")) && signupForm.phoneNumber() == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have selected for a code to be sent to " +
                    "your phone, but you haven't entered a phone number");
        }

        switch(signupForm.otpChannel())
        {
            case "sms", "call":
                otpService.generateOTPCode(signupForm.phoneNumber(), signupForm.otpChannel());
                break;
            case "email":
                otpService.generateOTPCode(signupForm.email(), "email");
                break;

        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("A one-time code has been sent to your " +
                "desired channel, please enter it to finalize the signup process");
    }


    private ResponseEntity<?> signUpNewUser(SignupForm signupForm, HttpServletRequest request,
                                            MultipartFile pictureFile)
    {
        //This block of code deals with creating the user in the database.
        try {
            User createdUser = userService.createNewUser(signupForm, pictureFile);
            // This block of code is used to automatically authenticate the user that just signed up,
            // ensuring a seamless UX
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signupForm.email(), signupForm.password())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
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
        catch (IOException ioException){
            return ResponseEntity.status(500).body("An unexpected error has occurred, please try again later. The error is:\n "
            + ioException.getMessage());
        }

        return null;
    }

    @PostMapping(value = "/signup/finalize", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> finalizeSignup(@Valid @RequestPart(name = "meta") SignupForm signupForm,
                                            HttpServletRequest request,
                                            @RequestPart(name = "file") MultipartFile profilePicture,
                                            @RequestParam String enteredOtpCode){

        VerificationCheck verificationCheck = verifyEnteredCode(signupForm, enteredOtpCode);

        String status = verificationCheck.getStatus();

        switch (status){
            case "approved" -> {
                return signUpNewUser(signupForm, request, profilePicture);
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
