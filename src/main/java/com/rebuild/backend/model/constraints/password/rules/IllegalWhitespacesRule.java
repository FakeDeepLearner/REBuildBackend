package com.rebuild.backend.model.constraints.password.rules;

import org.passay.*;

public class IllegalWhitespacesRule implements Rule {
    @Override
    public RuleResult validate(PasswordData passwordData) {
        String password = passwordData.getPassword();
        if (password.contains(" ")) {
            RuleResult result = new RuleResult(false);
            result.getDetails().add(new RuleResultDetail("PROHIBITED_WHITESPACE", null));
            return result;
        }
        return new RuleResult(true);
    }


}
