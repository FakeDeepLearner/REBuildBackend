package com.rebuild.backend.controllers.otp_and_email;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.auth_forms.EmailChangeForm;
import com.rebuild.backend.model.forms.auth_forms.PasswordResetForm;
import com.rebuild.backend.service.token_services.OTPService;
import com.rebuild.backend.service.user_services.UserService;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
//TODO: Handle Verification of the VerificationCheck returned by twilio.
public class MainOTPController {

    private final UserService userService;

    private final OTPService otpService;

    @Autowired
    public MainOTPController(UserService userService, OTPService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    private void sendOneTimePasscode(String phoneNumber){
        otpService.generateSMSOTP(phoneNumber);
    }

    @PostMapping("/send")
    public void sendOTPCode(@AuthenticationPrincipal User relevantUser){
        sendOneTimePasscode(relevantUser.stringifiedNumber());
    }

    @PostMapping("/verify/new_email}")
    public void validateOTPForNewEmail(@RequestBody EmailChangeForm emailChangeForm,
                                       @AuthenticationPrincipal User changingUser){
        VerificationCheck verificationCheck = otpService.validateEnteredOTP(changingUser.stringifiedNumber(),
                emailChangeForm.enteredOTP());

        userService.changeEmail(changingUser.getId(), emailChangeForm.newEmail());
    }

    @PostMapping("/verify/new_password}")
    public void validateOTPForNewPassword(@RequestBody PasswordResetForm passwordResetForm,
                                       @AuthenticationPrincipal User changingUser){
        VerificationCheck verificationCheck = otpService.validateEnteredOTP(changingUser.stringifiedNumber(),
                passwordResetForm.enteredOTP());

        userService.changePassword(changingUser.getId(), passwordResetForm.newPassword());
    }

    





}
