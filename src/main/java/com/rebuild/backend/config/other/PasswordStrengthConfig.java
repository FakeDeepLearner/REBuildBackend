package com.rebuild.backend.config.other;

import com.rebuild.backend.config.properties.PasswordProperties;
import com.rebuild.backend.model.constraints.password.rules.IllegalWhitespacesRule;
import com.rebuild.backend.model.constraints.password.rules.MaxConsecutiveCharactersRule;
import com.rebuild.backend.model.constraints.password.rules.MaxConsecutiveNumbersRule;
import com.rebuild.backend.utils.PasswordMessageResolver;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class PasswordStrengthConfig {

    private final PasswordProperties properties;

    private final PasswordMessageResolver resolver;

    @Autowired
    public PasswordStrengthConfig(PasswordProperties properties, PasswordMessageResolver resolver) {
        this.properties = properties;
        this.resolver = resolver;
    }

    @Bean
    public PasswordValidator validator(){
        List<Rule> defaultRules = Arrays.asList(
                new MaxConsecutiveCharactersRule(properties.consecutiveCharacterLimit()),
                new MaxConsecutiveNumbersRule(properties.consecutiveNumbersLimit()),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new LengthRule(8, 30)
        );

        if(properties.canContainSpaces()){
            defaultRules.add(new IllegalWhitespacesRule());
        }


        return new PasswordValidator(resolver, defaultRules);
    }
}
