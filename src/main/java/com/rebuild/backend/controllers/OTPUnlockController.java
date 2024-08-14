package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.enums.EmailOTPGenerationPurpose;
import com.rebuild.backend.model.forms.OTPVerificationEmailDTO;
import com.rebuild.backend.service.OTPService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class OTPUnlockController {

    private final OTPService otpService;

    @Autowired
    public OTPUnlockController(OTPService otpService) {
        this.otpService = otpService;
    }

    @GetMapping("/api/get_otp/unlock/{email}")
    @ResponseStatus(HttpStatus.OK)
    public int getEmailOtpForUnlock(@PathVariable String email){
        return otpService.generateOtpFor(email, EmailOTPGenerationPurpose.ACCOUNT_UNLOCK);
    }


    @PostMapping("/api/verify_otp/unlock")
    @ResponseStatus(HttpStatus.FOUND)
    public void verifyOtp(@RequestBody OTPVerificationEmailDTO verificationEmailDTO,
                          HttpServletResponse response) throws IOException {
        otpService.validateOtpFor(verificationEmailDTO.email(), verificationEmailDTO.enteredOtp(),
                EmailOTPGenerationPurpose.ACCOUNT_UNLOCK);
        response.sendRedirect("/login");

    }
}
