package com.rebuild.backend.service.token_services;

import com.rebuild.backend.model.entities.users.SentVerificationRecord;
import com.rebuild.backend.repository.SentVerificationRecordRepository;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class OTPService {

    private final SentVerificationRecordRepository recordRepository;

    @Autowired
    public OTPService(SentVerificationRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    private void recordSentVerification(Verification sentVerification)
    {
        String channel = sentVerification.getChannel().toString();

        String to = sentVerification.getTo();

        LocalDateTime timestamp = sentVerification.getDateCreated().toLocalDateTime();

        SentVerificationRecord newVerificationRecord = new SentVerificationRecord(channel, to, timestamp);

        recordRepository.save(newVerificationRecord);
    }

    public void generateOTPCode(String phoneOrEmail, String channel){

        Verification newVerification = Verification.creator(System.getenv("TWILIO_VERIFY_SERVICE_SID"),
                phoneOrEmail, channel).create();

        recordSentVerification(newVerification);
    }

    public VerificationCheck validateEnteredOTP(String phoneOrEmail, String enteredOTP){
        return VerificationCheck.creator(System.getenv("TWILIO_VERIFY_SERVICE_SID")).
                setTo(phoneOrEmail).setCode(enteredOTP).create();
    }

}
