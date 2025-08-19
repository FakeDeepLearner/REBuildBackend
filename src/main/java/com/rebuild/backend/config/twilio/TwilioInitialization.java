package com.rebuild.backend.config.twilio;

import com.sendgrid.SendGrid;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitialization {

    @PostConstruct
    public void twilioInit(){
        Twilio.init(System.getenv("TWILIO_ACCOUNT_SID"), System.getenv("TWILIO_AUTH_TOKEN"));
    }

    @Bean
    public SendGrid sendGrid(){
        return new SendGrid(System.getenv("TWILIO_SENDGRID_KEY"));
    }

}
