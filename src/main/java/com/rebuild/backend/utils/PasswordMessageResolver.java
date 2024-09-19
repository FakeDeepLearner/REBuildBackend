package com.rebuild.backend.utils;

import com.rebuild.backend.config.properties.PasswordProperties;
import org.passay.MessageResolver;
import org.passay.RuleResultDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PasswordMessageResolver implements MessageResolver {

    private final Map<String, String> errorMessages = new HashMap<>();

    @Autowired
    public PasswordMessageResolver(PasswordProperties properties) {
        errorMessages.put("TOO_SHORT", String.format("The password must be at least %d characters long",
                properties.minSize()));
        errorMessages.put("TOO_LONG", String.format("The password must be at most %d characters long",
                properties.maxSize()));
        errorMessages.put("INSUFFICIENT_UPPERCASE", String.format("The password must contain at least %d uppercase letters",
                properties.minUppercase()));
        errorMessages.put("INSUFFICIENT_LOWERCASE", String.format("The password must contain at least %d lowercase letters",
                properties.minLowercase()));
        errorMessages.put("INSUFFICIENT_DIGIT", String.format("The password must contain at least %d digits",
                properties.minDigit()));
        errorMessages.put("INSUFFICIENT_SPECIAL", String.format("The password must contain at least %d special characters",
                properties.minSpecialCharacter()));
        errorMessages.put("PASSWORD_NUMBER_ERROR", "The password can't contain more " +
                "than %d numbers in a row".formatted(properties.consecutiveNumbersLimit()));
        if(properties.canContainSpaces()){
            errorMessages.put("PROHIBITED_WHITESPACE", "The password may not contain spaces");
        }

    }

    @Override
    public String resolve(RuleResultDetail ruleResultDetail) {
        String errorCode = ruleResultDetail.getErrorCode();
        return errorMessages.getOrDefault(errorCode, "UNKNOWN_ERROR");
    }
}
