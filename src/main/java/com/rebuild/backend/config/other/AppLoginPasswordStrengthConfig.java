package com.rebuild.backend.config.other;

import com.rebuild.backend.model.constraints.password.rules.IllegalWhitespacesRule;
import com.rebuild.backend.model.constraints.password.rules.UnlimitedLengthCustomRule;
import com.rebuild.backend.utils.password_utils.PasswordMessageResolver;
import com.rebuild.backend.utils.password_utils.PasswordProps;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AppLoginPasswordStrengthConfig {

    private final PasswordMessageResolver resolver;



    @Autowired
    public AppLoginPasswordStrengthConfig(PasswordMessageResolver resolver) {
        this.resolver = resolver;

    }

    @Bean
    public PasswordValidator appLoginValidator(){
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


}
