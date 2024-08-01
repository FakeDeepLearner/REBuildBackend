package com.rebuild.backend.config.other;

import com.rebuild.backend.config.properties.PasswordCharacterAndNumberLimits;
import com.rebuild.backend.model.constraints.password.rules.MaxConsecutiveCharactersRule;
import com.rebuild.backend.model.constraints.password.rules.MaxConsecutiveNumbersRule;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class PasswordStrengthConfig {

    private final PasswordCharacterAndNumberLimits limits;

    @Autowired
    public PasswordStrengthConfig(PasswordCharacterAndNumberLimits limits) {
        this.limits = limits;
    }

    @Bean
    public PasswordValidator validator(){
        return new PasswordValidator(Arrays.asList(
                new MaxConsecutiveCharactersRule(limits.consecutiveCharacterLimit()),
                new MaxConsecutiveNumbersRule(limits.consecutiveNumbersLimit())
        ));
    }
}
