package com.rebuild.backend.controllers.otp_controllers;

import com.rebuild.backend.config.properties.TwilioCredentials;
import com.rebuild.backend.exceptions.not_found_exceptions.PhoneNumberMissingException;
import com.rebuild.backend.model.entities.enums.OTPGenerationPurpose;
import com.rebuild.backend.model.forms.dtos.otp_dto.OTPVerificationPhoneNumberDTO;
import com.rebuild.backend.service.token_services.OTPService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class SMSReactivationController {
    private final TwilioCredentials twilioCredentials;

    private final OTPService otpService;

    @Autowired
    public SMSReactivationController(TwilioCredentials twilioCredentials, OTPService otpService) {
        this.twilioCredentials = twilioCredentials;
        this.otpService = otpService;
    }


    @GetMapping("/api/get_otp/reactivate/{phone_number}")
    @ResponseStatus(HttpStatus.OK)
    public int getPhoneOtpForReactivation(@PathVariable String phone_number){
        if(phone_number == null){
            throw new PhoneNumberMissingException("Your request can't be processed because you do not " +
                    "currently have a registered phone number");
        }
        int otp = otpService.generateOtpFor(phone_number);
        Message.creator(
                new PhoneNumber(phone_number),
                new PhoneNumber(twilioCredentials.phoneNumber()),
                String.format("""
                        Your one time passcode for reactivating your account is %d.

                        Text STOP to opt out of these messages""", otp)
        ).create();
        return otp;
    }

    @PostMapping("/api/verify_otp/reactivate/phone_number")
    @ResponseStatus(HttpStatus.FOUND)
    public void verifyOtp(@RequestBody OTPVerificationPhoneNumberDTO phoneNumberDTO,
                          HttpServletResponse response)
            throws IOException {
        otpService.validateOtpFor(phoneNumberDTO.phoneNumber(), phoneNumberDTO.enteredOtp(),
                OTPGenerationPurpose.REACTIVATE_CREDENTIALS);
        response.sendRedirect("/login");
    }
}
