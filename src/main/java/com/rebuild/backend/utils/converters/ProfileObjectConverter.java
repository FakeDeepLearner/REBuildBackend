package com.rebuild.backend.utils.converters;

import com.google.api.client.util.PemReader;
import com.rebuild.backend.model.entities.profile_entities.ProfileEducation;
import com.rebuild.backend.model.entities.profile_entities.ProfileExperience;
import com.rebuild.backend.model.entities.profile_entities.ProfileHeader;
import com.rebuild.backend.model.entities.profile_entities.ProfileSection;
import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.ResumeSection;
import org.springframework.stereotype.Component;

@Component
public class ProfileObjectConverter{

    public Experience convertToExperience(ProfileExperience profileExperience){
        return new Experience(profileExperience.getCompanyName(), profileExperience.getTechnologyList(),
                profileExperience.getLocation(),
                profileExperience.getStartDate(), profileExperience.getEndDate(), profileExperience.getBullets());
    }

    public Header convertToHeader(ProfileHeader profileHeader){
        return new Header(profileHeader.getNumber(), profileHeader.getFirstName(), profileHeader.getLastName(),
                profileHeader.getEmail());
    }


    public Education convertToEducation(ProfileEducation profileEducation){
        return new Education(profileEducation.getSchoolName(), profileEducation.getRelevantCoursework(),
                profileEducation.getLocation(),
                profileEducation.getStartDate(), profileEducation.getEndDate());
    }

    public ResumeSection convertToSection(ProfileSection profileSection){
        return new ResumeSection(profileSection.getTitle(), profileSection.getBullets());
    }
}
