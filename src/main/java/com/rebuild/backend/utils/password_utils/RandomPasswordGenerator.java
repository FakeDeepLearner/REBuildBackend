package com.rebuild.backend.utils.password_utils;

import com.rebuild.backend.config.properties.PasswordProperties;
import org.passay.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class RandomPasswordGenerator {
    private final PasswordValidator validator;

    private final PasswordProperties properties;

    private final List<CharacterRule> characterRules;

    public RandomPasswordGenerator(PasswordValidator validator,
                                   PasswordProperties properties) {
        this.validator = validator;
        this.properties = properties;
        //Filter out the CharacterRule objects, cast them to CharacterRule and then collect them in a list
        this.characterRules = validator.getRules().stream().
                filter(rule -> rule instanceof CharacterRule).
                map(rule -> (CharacterRule) rule).
                collect(Collectors.toList());
    }

    public String generateRandom(){
        PasswordGenerator generator = new PasswordGenerator();
        Random random = new Random();
        String generatedPassword;
        int randomLength;
        RuleResult passwordRuleResult;
        //Keep generating passwords as long as the one generated is invalid.
        do {
            randomLength = random.nextInt(properties.minSize(), properties.maxSize() + 1);
            generatedPassword = generator.generatePassword(randomLength, characterRules);
            passwordRuleResult = validator.validate(new PasswordData(generatedPassword));
        }while (!passwordRuleResult.isValid());
        return generatedPassword;
    }

}
