package com.rebuild.backend.controllers.otp_and_email;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.auth_forms.EmailChangeForm;
import com.rebuild.backend.model.forms.auth_forms.PasswordResetForm;
import com.rebuild.backend.service.token_services.OTPService;
import com.rebuild.backend.service.user_services.UserService;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class MainOTPController {

    private final UserService userService;

    private final OTPService otpService;

    @Autowired
    public MainOTPController(UserService userService, OTPService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    private void sendOneTimePasscode(String emailOrPhone, String channel){
        otpService.generateOTPCode(emailOrPhone, channel);
    }

    @PostMapping("/send_code/sms")
    public void sendOTPCodeToPhone(@AuthenticationPrincipal User relevantUser){
        sendOneTimePasscode(relevantUser.getPhoneNumber(), "sms");
    }

    @PostMapping("/send_code/email")
    public void sendOTPCodeToEmail(@AuthenticationPrincipal User relevantUser){
        sendOneTimePasscode(relevantUser.getPhoneNumber(), "email");
    }

    @PostMapping("/verify/new_email")
    public ResponseEntity<String> validateOTPForNewEmail(@RequestBody EmailChangeForm emailChangeForm,
                                                 @AuthenticationPrincipal User changingUser){

        if (!emailChangeForm.newEmail().equals(emailChangeForm.newEmailConfirmation()))
        {
            return ResponseEntity.badRequest().body("The provided emails do not match");
        }
        VerificationCheck verificationCheck = otpService.validateEnteredOTP(changingUser.getPhoneNumber(),
                emailChangeForm.enteredOTP());

        String status = verificationCheck.getStatus();

        switch (status){
            case "approved" -> {
                userService.changeEmail(changingUser, emailChangeForm.newEmail());
                return ResponseEntity.ok().build();
            }

            case "failed" -> {
                return ResponseEntity.badRequest().body("You have entered the wrong code");
            }

            case "expired" -> {
                //We send the notification again via the same channel that the user used to originally obtain it.
                String oldChannel = verificationCheck.getChannel().toString();
                sendOneTimePasscode(changingUser.getPhoneNumber(), oldChannel);
                return ResponseEntity.status(HttpStatus.GONE).body("The passcode that you requested has expired, " +
                        "we have sent you a new one.");
            }

            case "max_attempts_reached" -> {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).
                        body("You have reached the maximum number of attempts.");
            }

        }


        //Should never get here
        return ResponseEntity.internalServerError().build();
    }

    @PostMapping("/verify/new_password")
    public ResponseEntity<String> validateOTPForNewPassword(@RequestBody PasswordResetForm passwordResetForm,
                                       @AuthenticationPrincipal User changingUser){

        if (!passwordResetForm.newPassword().equals(passwordResetForm.confirmNewPassword()))
        {
            return ResponseEntity.badRequest().body("The provided passwords do not match");
        }
        VerificationCheck verificationCheck = otpService.validateEnteredOTP(changingUser.getPhoneNumber(),
                passwordResetForm.enteredOTP());

        String status = verificationCheck.getStatus();

        switch (status){
            case "approved" -> {
                userService.changePassword(changingUser, passwordResetForm.newPassword());
                return ResponseEntity.ok().build();
            }

            case "failed" -> {
                return ResponseEntity.badRequest().body("You have entered the wrong code");
            }

            case "expired" -> {
                //We send the notification again via the same channel that the user used to originally obtain it.
                String oldChannel = verificationCheck.getChannel().toString();
                sendOneTimePasscode(changingUser.getForumUsername(), oldChannel);
                return ResponseEntity.status(HttpStatus.GONE).body("The passcode that you requested has expired, " +
                        "we have sent you a new one.");
            }

            case "max_attempts_reached" -> {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).
                        body("You have reached the maximum number of attempts.");
            }

        }


        //Should never get here
        return ResponseEntity.internalServerError().build();
    }

    





}
