package com.rebuild.backend.utils.converters;

import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.utils.YearMonthStringOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ObjectConverter {

    private <I, O> List<O> convertToOutputList(List<I> inputList, Function<I, O> converter){
        return inputList.stream().map(converter).collect(Collectors.toList());
    }

    public List<Experience> extractProfileExperiences(List<ExperienceForm> profileExperienceForms,
                                                             UserProfile profile){
        return convertToOutputList(profileExperienceForms, rawForm ->
        {
                List<ExperienceType> experienceTypes = convertToExperienceTypes(rawForm.experienceTypeValues());
                Experience newExperience = new Experience(rawForm.companyName(),
                        rawForm.technologies(), rawForm.location(), experienceTypes,
                        YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                        YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                        rawForm.bullets());
                newExperience.setProfile(profile);
                return newExperience;
        });

    }

    public List<Experience> extractExperiences(List<ExperienceForm> experienceForms, Resume associatedResume){
        return convertToOutputList(experienceForms, rawForm ->
        {
            List<ExperienceType> experienceTypes = convertToExperienceTypes(rawForm.experienceTypeValues());
            Experience newExperience = new Experience(rawForm.companyName(),
                    rawForm.technologies(), rawForm.location(), experienceTypes,
                    YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                    YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                    rawForm.bullets());
            newExperience.setResume(associatedResume);
            return newExperience;
        });
    }

    public List<ExperienceType> convertToExperienceTypes(List<String> typesList)
    {
        return typesList.stream().
                map(ExperienceType::fromValue).
                toList();
    }
}
