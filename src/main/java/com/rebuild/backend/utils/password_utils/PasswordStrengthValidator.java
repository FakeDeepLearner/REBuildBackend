package com.rebuild.backend.utils.password_utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PasswordStrengthValidator implements ConstraintValidator<PasswordStrengthConstraint, String> {

    private final PasswordValidator validator;

    @Autowired
    public PasswordStrengthValidator(@Qualifier(value = "appLoginValidator") PasswordValidator validator) {
        this.validator = validator;
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        RuleResult validationResult = validator.validate(new PasswordData(password));
        if(validationResult.isValid()){
            return true;
        }
        //We will define our own messages and add them to the context
        constraintValidatorContext.disableDefaultConstraintViolation();
        validationResult.getDetails().forEach((resultDetail) -> {
            String fullErrorMessage = (String) resultDetail.getParameters().get(resultDetail.getErrorCode());
            constraintValidatorContext.buildConstraintViolationWithTemplate(fullErrorMessage).
                    addConstraintViolation();
        });
        return false;

    }
}
