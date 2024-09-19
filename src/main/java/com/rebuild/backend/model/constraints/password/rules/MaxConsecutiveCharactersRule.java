package com.rebuild.backend.model.constraints.password.rules;

import org.passay.PasswordData;
import org.passay.Rule;
import org.passay.RuleResult;
import org.passay.RuleResultDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class MaxConsecutiveCharactersRule implements Rule {

    private final int maxConsecutiveCharacters;

    private final String errorMessage;

    @Autowired
    public MaxConsecutiveCharactersRule(@Value("${password-rules.consecutive-character-limit}")
                                            int maxConsecutiveCharacters) {
        this.maxConsecutiveCharacters = maxConsecutiveCharacters;
        this.errorMessage = String.format("The password can't contain more than %d characters in a row",
                maxConsecutiveCharacters);
    }

    @Override
    public RuleResult validate(PasswordData passwordData) {
        String password = passwordData.getPassword();
        Pattern pattern = Pattern.compile(String.format("[a-zA-Z]{%d,}", maxConsecutiveCharacters + 1));
        if(pattern.matcher(password).find()){
            RuleResult returnedResult = new RuleResult(false);
            String errorCode = "PASSWORD_CHARACTER_ERROR";
            returnedResult.getDetails().add(new RuleResultDetail(errorCode, Map.of(errorCode, errorMessage)));
            return returnedResult;
        }

        return new RuleResult(true);
    }

}
