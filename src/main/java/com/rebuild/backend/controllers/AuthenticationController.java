package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.responses.PasswordFeedbackResponse;
import com.rebuild.backend.service.token_services.OTPService;
import com.rebuild.backend.model.forms.auth_forms.LoginForm;
import com.rebuild.backend.model.forms.auth_forms.SignupForm;
import com.rebuild.backend.service.user_services.UserService;
import com.sendgrid.Response;
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

        Bucket userBucket = userService.returnUserBucket(credentialValidationDTO.userEmail());

        ConsumptionProbe probe = userBucket.tryConsumeAndReturnRemaining(1L);

        if (probe.isConsumed()){
            boolean userCanLogin = credentialValidationDTO.canLogin();
            if (!userCanLogin){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid username or password");
            }

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

        loginHelper(form, request);
        return ResponseEntity.ok().body("Login successful, redirecting you to your home page");

    }

    private void loginHelper(LoginForm form, HttpServletRequest request){
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.emailOrPhone(), form.password())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true);
    }


    @PostMapping("/signup/initialize")
    public ResponseEntity<String> initializeSignup(@Valid @RequestBody SignupForm signupForm,
                                                   @RequestParam(name = "g-recaptcha-response") String userResponse,
                                                   HttpServletRequest request) throws IOException, InterruptedException {
        
        if (userService.captchaFailed(userResponse, request.getRemoteAddr())) {
            return ResponseEntity.badRequest().body("Invalid captcha response, please try again");
        }

        ResponseEntity<String> passwordCheckResponse = userService.doPreliminaryPasswordChecks(signupForm);

        if (passwordCheckResponse != null)
        {
            return passwordCheckResponse;
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
                                            @RequestParam String enteredOtpCode) {

        return signUpNewUser(signupForm, request, profilePicture);

    }

}
