package com.rebuild.backend.model.constraints.resume_and_profile;

import com.rebuild.backend.model.constraints.password.constraints_and_validators.PasswordsMatchConstraint;
import com.rebuild.backend.model.forms.profile_forms.ProfileExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;
import java.time.LocalDateTime;


public class DateConsistencyValidator implements ConstraintValidator<PasswordsMatchConstraint, Object> {
    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        if(o instanceof ProfileExperienceForm form){
            LocalDateTime endTime =  LocalDateTime.from(form.endDate());
            LocalDateTime startTime = LocalDateTime.from(form.startDate());
            return endTime.isAfter(startTime);
        }
        if(o instanceof ExperienceForm form){
            LocalDateTime endTime =  LocalDateTime.from(form.endDate());
            LocalDateTime startTime = LocalDateTime.from(form.startDate());
            return endTime.isAfter(startTime);
        }
        return false;
    }
}
