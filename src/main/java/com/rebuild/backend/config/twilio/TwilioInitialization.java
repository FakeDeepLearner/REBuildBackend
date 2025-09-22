package com.rebuild.backend.config.twilio;

import com.sendgrid.SendGrid;
import com.twilio.Twilio;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitialization {

    @PostConstruct
    public void twilioInit(Dotenv dotenv){
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));
    }

    @Bean
    public SendGrid sendGrid(Dotenv dotenv){
        return new SendGrid(dotenv.get("TWILIO_SENDGRID_KEY"));
    }

}
