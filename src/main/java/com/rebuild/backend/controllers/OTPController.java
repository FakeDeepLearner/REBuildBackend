package com.rebuild.backend.controllers;

import com.rebuild.backend.model.forms.OTPVerificationEmailDTO;
import com.rebuild.backend.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class OTPController {

    private final OTPService otpService;

    @Autowired
    public OTPController(OTPService otpService) {
        this.otpService = otpService;
    }

    @GetMapping("/api/get_otp/{email}")
    @ResponseStatus(HttpStatus.OK)
    public int getEmailOtp(@PathVariable String email){
        return otpService.generateOtpFor(email);
    }

    @PostMapping("/api/verify_otp")
    @ResponseStatus(HttpStatus.OK)
    public void verifyOtp(@RequestBody OTPVerificationEmailDTO verificationEmailDTO){
        otpService.validateOtpFor(verificationEmailDTO.email(), verificationEmailDTO.enteredOtp());
    }
}
