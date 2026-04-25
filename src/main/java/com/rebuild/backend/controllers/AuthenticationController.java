package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.UserAuthException;
import com.rebuild.backend.model.forms.auth_forms.*;
import com.rebuild.backend.model.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.responses.MFAEnrolmentResponse;
import com.rebuild.backend.model.responses.RecoveryCodeVerificationResponse;
import com.rebuild.backend.service.auth_services.RecoveryCodeHelperService;
import com.rebuild.backend.service.auth_services.TOTPCodeService;
import com.rebuild.backend.service.auth_services.UserAuthenticationHelperService;
import com.rebuild.backend.service.user_services.EmailAndPasswordChangeService;
import com.rebuild.backend.service.user_services.UserService;
import com.sendgrid.helpers.mail.Mail;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authManager;

    private final UserService userService;

    private final UserAuthenticationHelperService authenticationHelperService;

    private final TOTPCodeService totpCodeService;

    private final RecoveryCodeHelperService recoveryCodeHelperService;

    private final EmailAndPasswordChangeService emailAndPasswordChangeService;

    @Autowired
    public AuthenticationController(AuthenticationManager authManager,
                                    UserService userService,
                                    UserAuthenticationHelperService authenticationHelperService,
                                    TOTPCodeService totpCodeService,
                                    RecoveryCodeHelperService recoveryCodeHelperService, EmailAndPasswordChangeService emailAndPasswordChangeService) {
        this.authManager = authManager;
        this.userService = userService;
        this.authenticationHelperService = authenticationHelperService;
        this.totpCodeService = totpCodeService;
        this.recoveryCodeHelperService = recoveryCodeHelperService;
        this.emailAndPasswordChangeService = emailAndPasswordChangeService;
    }

    @PostMapping("/login/initialize")
    public ResponseEntity<String> initializeLogin(@Valid @RequestBody LoginForm loginForm,
                                             @RequestParam(name = "g-recaptcha-response") String userResponse,
                                             HttpServletRequest request) {
        if (authenticationHelperService.captchaFailed(userResponse, request.getRemoteAddr())) {
            return ResponseEntity.badRequest().body("Invalid captcha response, please try again");
        }

        CredentialValidationDTO credentialValidationDTO = authenticationHelperService.validateLoginCredentials(loginForm);
        if (credentialValidationDTO == null)
        {
            throw new UserAuthException(HttpStatus.NOT_FOUND, "No user with this email or phone number exists");
        }
        Bucket userBucket = authenticationHelperService.returnUserBucket(credentialValidationDTO.foundUser());

        ConsumptionProbe probe = userBucket.tryConsumeAndReturnRemaining(1L);

        if (probe.isConsumed()){

            boolean userCanLogin = credentialValidationDTO.canLogin();
            if (!userCanLogin){
                throw new UserAuthException(HttpStatus.CONFLICT, "Invalid username or password");
            }

            if (!credentialValidationDTO.enrolledInMFA())
            {
                loginHelper(loginForm, request);
                return ResponseEntity.ok("Login successful");
            }

            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Please open your " +
                    "authenticator app and enter the code there");
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
                throw new UserAuthException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts, " +
                        "please retry in " + minutesRemaining + " minutes and " + secondsRemaining + " seconds");
            }
            else{
                throw new UserAuthException(HttpStatus.UNAUTHORIZED, "Wrong credentials, " +
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

        if (totpCodeService.otpMatches(form, enteredOtpCode))
        {
            loginHelper(form, request);
            return ResponseEntity.ok().body("Login successful, redirecting you to your home page.");
        }
        else {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Incorrect code");
        }
    }

    @PostMapping("/login/recovery_code")
    public ResponseEntity<String> loginWithRecoveryCode(@Valid @RequestBody LoginForm form,
                                                        @RequestParam(name = "code") String enteredCode,
                                                        HttpServletRequest request)
    {
        RecoveryCodeVerificationResponse verificationResponse =
                recoveryCodeHelperService.verifyRecoveryCode(form.emailOrPhone(), enteredCode);

        if (verificationResponse.userOutOfCodes())
        {
            throw new UserAuthException(HttpStatus.CONFLICT, "The request cannot be completed because you" +
                    "have no valid codes left to use. We have generated new codes for you, displayed below. You do not need to " +
                    "restart the authentication process.\n\n" + verificationResponse.newCodes());
        }


        if (verificationResponse.codeIsCorrect())
        {
            loginHelper(form, request);
            return ResponseEntity.ok("Emergency code used successfully, redirecting you to home page");
        }
        else {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Incorrect recovery code");
        }
    }

    private void loginHelper(LoginForm form, HttpServletRequest request){
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.emailOrPhone(), form.password())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true);
    }

    private User signUpNewUser(SignupForm signupForm, HttpServletRequest request,
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
            return createdUser;
        }
        catch (DataIntegrityViolationException integrityViolationException){
            Throwable cause = integrityViolationException.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                String violatedConstraint = violationException.getConstraintName();
                switch (violatedConstraint){
                    case "uk_email" -> throw new UserAuthException(HttpStatus.CONFLICT, "This email already exists");
                    case "uk_phone_number" -> throw new UserAuthException(HttpStatus.CONFLICT, "This phone number already exists");
                    case "uk_forum_username" -> throw new UserAuthException(HttpStatus.CONFLICT, "This username already exists");

                    //This should never happen
                    case null -> throw new RuntimeException();

                    default -> throw new IllegalStateException("Unexpected value: " + violatedConstraint);
                }
            }
        }
        return null;
    }

    @PostMapping(value = "/signup", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public User signUpUser(@Valid @RequestPart(name = "meta") SignupForm signupForm,
                                            HttpServletRequest request,
                                            @RequestPart(name = "file") MultipartFile profilePicture,
                                            @RequestParam(name = "g-recaptcha-response") String userResponse)
            throws IOException, InterruptedException {
        if (authenticationHelperService.captchaFailed(userResponse, request.getRemoteAddr())) {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Invalid captcha response");
        }

        boolean passwordCheckSuccessful = authenticationHelperService.doPreliminaryPasswordChecks(signupForm);

        if (passwordCheckSuccessful)
        {
            return signUpNewUser(signupForm, request, profilePicture);
        }
        return null;

    }


    @PostMapping("/mfa/initialize")
    @ResponseStatus(HttpStatus.OK)
    public MFAEnrolmentResponse startMFAEnrolment(@AuthenticationPrincipal User startingUser,
                                               @RequestBody String enteredPassword)
    {
        MFAEnrolmentResponse enrolmentResponse = totpCodeService.startMFAEnrolment(startingUser, enteredPassword);

        if (enrolmentResponse == null)
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Incorrect password");
        }
        return enrolmentResponse;
    }

    @PostMapping("/mfa/finalize")
    @ResponseStatus(HttpStatus.OK)
    public User finalizeMFAEnrolment(@AuthenticationPrincipal User user,
                                                       @RequestBody MFAEnrolmentForm enrolmentForm)
    {
        return totpCodeService.enrolUserInMFA(user, enrolmentForm);
    }


    @GetMapping("/mfa/regenerate_codes")
    public List<String> regenerateRecoveryCodes(@AuthenticationPrincipal User user)
    {
        return recoveryCodeHelperService.regenerateRecoveryCodesFor(user);
    }


    @PostMapping("/initialize_password_reset")
    public ResponseEntity<String> sendPasswordResetEmail(@RequestBody String enteredEmail)
    {
        boolean _ = emailAndPasswordChangeService.sendPasswordChangeEmail(enteredEmail);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("A link to change your password " +
                "has been sent to your email");

    }

    @PostMapping("/finalize_password_reset")
    public ResponseEntity<String> changePassword(@RequestParam(name = "token") String token,
                                                 @RequestBody PasswordResetForm passwordResetForm)
    {
        boolean _ = emailAndPasswordChangeService.processPasswordChange(token, passwordResetForm);
        return ResponseEntity.ok("Your password has been updated");
    }


    @PostMapping("initialize_email_change")
    public ResponseEntity<String> sendEmailChangeEmail(@RequestBody EmailChangeInitialForm form)
    {
        boolean _ = emailAndPasswordChangeService.sendEmailChange(form.oldEmail(), form.newEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).
                body("A link to change your email has been sent to your current email address");
    }

    @PostMapping("/finalize_email_change")
    public ResponseEntity<String> processEmailChange(@RequestParam(name = "token") String token,
                                                     @RequestBody EmailChangeConfirmationForm confirmationForm)
    {
        boolean _ = emailAndPasswordChangeService.processEmailChange(token, confirmationForm);
        return ResponseEntity.ok("Your email address has been successfully updated");
    }


}
