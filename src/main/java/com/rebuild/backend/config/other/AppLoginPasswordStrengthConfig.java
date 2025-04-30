package com.rebuild.backend.config.other;

import com.rebuild.backend.config.properties.PasswordProperties;
import com.rebuild.backend.model.constraints.password.rules.IllegalWhitespacesRule;
import com.rebuild.backend.model.constraints.password.rules.UnlimitedLengthCustomRule;
import com.rebuild.backend.utils.password_utils.PasswordMessageResolver;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AppLoginPasswordStrengthConfig {

    private final PasswordProperties properties;

    private final PasswordMessageResolver resolver;

    @Autowired
    public AppLoginPasswordStrengthConfig(PasswordProperties properties, PasswordMessageResolver resolver) {
        this.properties = properties;
        this.resolver = resolver;
    }

    @Bean
    public PasswordValidator appLoginValidator(){
        List<Rule> defaultRules = Arrays.asList(
                new CharacterRule(EnglishCharacterData.UpperCase, properties.minUppercase()),
                new CharacterRule(EnglishCharacterData.LowerCase, properties.minLowercase()),
                new CharacterRule(EnglishCharacterData.Special, properties.minSpecialCharacter()),
                new CharacterRule(EnglishCharacterData.Digit, properties.minDigit()),
                new UnlimitedLengthCustomRule(properties.minSize())
        );

        if(!properties.canContainSpaces()){
            defaultRules.add(new IllegalWhitespacesRule());
        }


        return new PasswordValidator(resolver, defaultRules);
    }


}
