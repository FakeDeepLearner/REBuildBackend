package com.rebuild.backend.service.token_services;

import com.rebuild.backend.model.entities.users.SentVerificationRecord;
import com.rebuild.backend.repository.user_repositories.SentVerificationRecordRepository;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;


@Service
public class OTPService {

    private final SentVerificationRecordRepository recordRepository;
    
    private final Dotenv dotenv;

    @Autowired
    public OTPService(SentVerificationRecordRepository recordRepository, Dotenv dotenv) {
        this.recordRepository = recordRepository;
        this.dotenv = dotenv;
    }

    private void recordSentVerification(Verification sentVerification)
    {
        String channel = sentVerification.getChannel().toString();

        String to = sentVerification.getTo();

        ZonedDateTime timestamp = sentVerification.getDateCreated();

        SentVerificationRecord newVerificationRecord = new SentVerificationRecord(channel, to, timestamp);

        recordRepository.save(newVerificationRecord);
    }

    public void generateOTPCode(String phoneOrEmail, String channel){

        Verification newVerification = Verification.creator(dotenv.get("TWILIO_VERIFY_SERVICE_SID"),
                phoneOrEmail, channel).create();

        recordSentVerification(newVerification);
    }

    public VerificationCheck validateEnteredOTP(String phoneOrEmail, String enteredOTP){
        return VerificationCheck.creator(dotenv.get("TWILIO_VERIFY_SERVICE_SID")).
                setTo(phoneOrEmail).setCode(enteredOTP).create();
    }

}
