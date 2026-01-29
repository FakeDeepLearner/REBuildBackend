package com.rebuild.backend.service.token_services;

import com.rebuild.backend.model.entities.users.SentVerificationRecord;
import com.rebuild.backend.repository.user_repositories.SentVerificationRecordRepository;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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


}
