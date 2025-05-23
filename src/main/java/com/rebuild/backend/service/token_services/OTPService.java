package com.rebuild.backend.service.token_services;

import com.rebuild.backend.config.properties.TwilioCredentials;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OTPService {

    private final TwilioCredentials twilioCredentials;

    @Autowired
    public OTPService(TwilioCredentials twilioCredentials) {
        this.twilioCredentials = twilioCredentials;
    }

    public void generateSMSOTP(String phoneNumber){

        Verification newVerification = Verification.creator(twilioCredentials.verifyServiceSid(),
                phoneNumber, "sms").create();
    }

    public VerificationCheck validateEnteredOTP(String phoneNumber, String enteredOTP){
        return VerificationCheck.creator(twilioCredentials.verifyServiceSid()).
                setTo(phoneNumber).setCode(enteredOTP).create();
    }

}
