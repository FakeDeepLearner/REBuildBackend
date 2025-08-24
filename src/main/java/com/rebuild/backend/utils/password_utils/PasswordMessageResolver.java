package com.rebuild.backend.utils.password_utils;

import org.passay.MessageResolver;
import org.passay.RuleResultDetail;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PasswordMessageResolver implements MessageResolver {

    private final Map<String, String> errorMessages = new HashMap<>();

    public PasswordMessageResolver() {
        errorMessages.put("TOO_SHORT",
                String.format("The password must be at least %d characters long",
                PasswordProps.MIN_SIZE.value));
        errorMessages.put("INSUFFICIENT_UPPERCASE",
                String.format("The password must contain at least %d uppercase letters",
                PasswordProps.MIN_UPPERCASE.value));
        errorMessages.put("INSUFFICIENT_LOWERCASE",
                String.format("The password must contain at least %d lowercase letters",
                PasswordProps.MIN_UPPERCASE.value));
        errorMessages.put("INSUFFICIENT_DIGIT",
                String.format("The password must contain at least %d digits",
                PasswordProps.MIN_DIGIT.value));
        errorMessages.put("INSUFFICIENT_SPECIAL",
                String.format("The password must contain at least %d special characters",
                PasswordProps.MIN_SPECIAL_CHARACTER.value));
        //If we can't have any spaces, we add the necessary error message.
        if(PasswordProps.CAN_HAVE_SPACES.value == 0){
            errorMessages.put("PROHIBITED_WHITESPACE", "The password may not contain spaces");
        }

    }

    @Override
    public String resolve(RuleResultDetail ruleResultDetail) {
        String errorCode = ruleResultDetail.getErrorCode();
        return errorMessages.getOrDefault(errorCode, "UNKNOWN_ERROR");
    }
}
