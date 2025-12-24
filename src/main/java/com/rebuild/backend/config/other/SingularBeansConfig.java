package com.rebuild.backend.config.other;

import com.cloudinary.Cloudinary;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.rebuild.backend.utils.password_utils.IllegalWhitespacesRule;
import com.rebuild.backend.utils.password_utils.PasswordMessageResolver;
import com.rebuild.backend.utils.password_utils.PasswordProps;
import com.rebuild.backend.utils.password_utils.UnlimitedLengthCustomRule;
import com.sendgrid.SendGrid;
import com.twilio.Twilio;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.filter.UrlHandlerFilter;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SingularBeansConfig {

    @Bean
    public Dotenv dotenv()
    {
        return Dotenv.load();
    }

    @Bean
    public Cloudinary cloudinary(Dotenv dotenv) {
        return new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    }

    @Bean
    public BloomFilter<String> bloomFilter()
    {
        return BloomFilter.
                create(Funnels.stringFunnel(Charset.defaultCharset()),
                        1_000_000, 0.01);
    }

    @Bean
    public PasswordValidator appLoginValidator(PasswordMessageResolver resolver){
        List<Rule> defaultRules = Arrays.asList(
                new CharacterRule(EnglishCharacterData.UpperCase, PasswordProps.MIN_UPPERCASE.value),
                new CharacterRule(EnglishCharacterData.LowerCase, PasswordProps.MIN_LOWERCASE.value),
                new CharacterRule(EnglishCharacterData.Special, PasswordProps.MIN_SPECIAL_CHARACTER.value),
                new CharacterRule(EnglishCharacterData.Digit, PasswordProps.MIN_DIGIT.value),
                new UnlimitedLengthCustomRule(PasswordProps.MIN_SIZE.value)
        );

        //If we can't have any spaces in the password, we add an illegal whitespace rule.
        if(PasswordProps.CAN_HAVE_SPACES.value == 0){
            defaultRules.add(new IllegalWhitespacesRule());
        }


        return new PasswordValidator(resolver, defaultRules);
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor hibernateTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

    //This will redirect to the original endpoint if there is a trailing slash at the end of the request's URL.
    @Bean
    public UrlHandlerFilter handlerFilter()
    {

        return UrlHandlerFilter
                .trailingSlashHandler("/api/**", "/home/**").redirect(HttpStatus.PERMANENT_REDIRECT)
                .build();
    }


    @Bean(name = "processor")
    public MethodValidationPostProcessor processor(){
        return new MethodValidationPostProcessor();
    }


    @PostConstruct
    public void twilioInit(Dotenv dotenv){
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));
    }

    @Bean
    public SendGrid sendGrid(Dotenv dotenv){
        return new SendGrid(dotenv.get("TWILIO_SENDGRID_KEY"));
    }
}
