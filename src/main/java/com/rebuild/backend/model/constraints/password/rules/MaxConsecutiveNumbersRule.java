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
public class MaxConsecutiveNumbersRule implements Rule {
    private final int maxConsecutiveNumbers;

    private final String errorMessage;

    @Autowired
    public MaxConsecutiveNumbersRule(@Value("${password-rules.consecutive-numbers-limit}")
                                        int maxConsecutiveNumbers) {
        this.maxConsecutiveNumbers = maxConsecutiveNumbers;
        this.errorMessage = String.format("The password can't contain more than %d numbers in a row",
                maxConsecutiveNumbers);
    }

    @Override
    public RuleResult validate(PasswordData passwordData) {
        String password = passwordData.getPassword();
        Pattern pattern = Pattern.compile(String.format("\\d{%d,}", maxConsecutiveNumbers + 1));
        if(pattern.matcher(password).find()){
            RuleResult returnedResult = new RuleResult(false);
            String errorCode = "PASSWORD_NUMBER_ERROR";
            returnedResult.getDetails().add(new RuleResultDetail(errorCode, Map.of(errorCode, errorMessage)));
            return returnedResult;
        }

        return new RuleResult(true);
    }

}
