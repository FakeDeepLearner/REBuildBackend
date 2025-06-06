package com.rebuild.backend.config.twilio;

import com.rebuild.backend.config.properties.TwilioCredentials;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitialization {
    private final TwilioCredentials twilioCredentials;

    @Autowired
    public TwilioInitialization(TwilioCredentials twilioCredentials) {
        this.twilioCredentials = twilioCredentials;
    }

    @PostConstruct
    public void twilioInit(){
        Twilio.init(twilioCredentials.accountSid(), twilioCredentials.authToken());
    }
}
