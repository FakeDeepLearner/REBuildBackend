package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.enums.EmailOTPGenerationPurpose;
import com.rebuild.backend.model.forms.OTPVerificationEmailDTO;
import com.rebuild.backend.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReactivationOTPController {

    private final OTPService otpService;

    @Autowired
    public ReactivationOTPController(OTPService otpService) {
        this.otpService = otpService;
    }

    @GetMapping("/api/get_otp/reactivate/{email}")
    @ResponseStatus(HttpStatus.OK)
    public int getEmailOtpForUnlock(@PathVariable String email){
        return otpService.generateOtpFor(email, EmailOTPGenerationPurpose.REACTIVATE_CREDENTIALS);
    }

    @PostMapping("/api/verify_otp/reactivate")
    @ResponseStatus(HttpStatus.OK)
    public void verifyOtp(@RequestBody OTPVerificationEmailDTO verificationEmailDTO){
        otpService.validateOtpFor(verificationEmailDTO.email(), verificationEmailDTO.enteredOtp(),
                EmailOTPGenerationPurpose.REACTIVATE_CREDENTIALS);
    }
}
