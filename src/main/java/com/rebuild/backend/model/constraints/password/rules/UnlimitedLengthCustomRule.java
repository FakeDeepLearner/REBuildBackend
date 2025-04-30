package com.rebuild.backend.model.constraints.password.rules;

import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.RuleResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class UnlimitedLengthCustomRule extends LengthRule {

    public UnlimitedLengthCustomRule(int minLength){
        super(minLength, Integer.MAX_VALUE);
    }

    @Override
    public RuleResult validate(PasswordData passwordData) {
        RuleResult result = new RuleResult();
        int length = passwordData.getPassword().length();
        if (length < this.getMinimumLength()) {
            result.addError("TOO_SHORT", this.createRuleResultDetailParameters());
        }
        result.setMetadata(this.createRuleResultMetadata(passwordData));
        return result;
    }

    @Override
    protected Map<String, Object> createRuleResultDetailParameters() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("minimumLength", this.getMinimumLength());
        return m;
    }
}
