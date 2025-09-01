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

    public Experience convertToExperience(Experience profileExperience){
        return new Experience(profileExperience.getCompanyName(), profileExperience.getTechnologyList(),
                profileExperience.getLocation(), profileExperience.getExperienceTypes(),
                profileExperience.getStartDate(), profileExperience.getEndDate(), profileExperience.getBullets());
    }

    public Header convertToHeader(Header profileHeader){
        return new Header(profileHeader.getNumber(), profileHeader.getFirstName(), profileHeader.getLastName(),
                profileHeader.getEmail());
    }


    public Education convertToEducation(Education profileEducation){
        return new Education(profileEducation.getSchoolName(), profileEducation.getRelevantCoursework(),
                profileEducation.getLocation(),
                profileEducation.getStartDate(), profileEducation.getEndDate());
    }

    private <I, O> List<O> convertToOutputList(List<I> inputList, Function<I, O> converter){
        return inputList.stream().map(converter).collect(Collectors.toList());
    }

    public List<Experience> extractProfileExperiences(List<ExperienceForm> profileExperienceForms,
                                                             UserProfile profile){
        return convertToOutputList(profileExperienceForms, rawForm ->
        {
                List<ExperienceType> experienceTypes = convertToExperienceTypes(rawForm.experienceTypeValues());
                return new Experience(rawForm.companyName(),
                rawForm.technologies(), rawForm.location(), experienceTypes,
                YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                rawForm.bullets());
        });

    }

    public List<Experience> extractExperiences(List<ExperienceForm> experienceForms, Resume associatedResume){
        return convertToOutputList(experienceForms, rawForm ->
        {
            List<ExperienceType> experienceTypes = convertToExperienceTypes(rawForm.experienceTypeValues());
            return new Experience(rawForm.companyName(),
                    rawForm.technologies(), rawForm.location(), experienceTypes,
                    YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                    YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                    rawForm.bullets());
        });
    }

    public Header createVersionedHeader(Header originalHeader, boolean shouldBeNull,
                                                 ResumeVersion resumeVersion){
        if(shouldBeNull){
            return null;
        }

        return new Header(originalHeader.getNumber(), originalHeader.getFirstName(),
                originalHeader.getLastName(), originalHeader.getEmail());
    }


    public Education createVersionedEducation(Education originalEducation, boolean shouldBeNull,
                                                       ResumeVersion resumeVersion){
        if(shouldBeNull){
            return null;
        }

        return new Education(originalEducation.getSchoolName(), originalEducation.getRelevantCoursework(),
                originalEducation.getLocation(), originalEducation.getStartDate(), originalEducation.getEndDate());
    }

    public List<Experience> createVersionedExperiences(List<Experience> originalExperiences,
                                                                boolean shouldBeNull,
                                                                ResumeVersion resumeVersion){
        if(shouldBeNull){
            return null;
        }

        return originalExperiences.stream().map(
                rawExperience -> new Experience(
                        rawExperience.getCompanyName(),
                        rawExperience.getTechnologyList(),
                        rawExperience.getLocation(),
                        rawExperience.getExperienceTypes(),
                        rawExperience.getStartDate(),
                        rawExperience.getEndDate(),
                        rawExperience.getBullets()
                )
        ).toList();
    }

    public List<ExperienceType> convertToExperienceTypes(List<String> typesList)
    {
        return typesList.stream().
                map(ExperienceType::fromValue).
                toList();
    }
}
