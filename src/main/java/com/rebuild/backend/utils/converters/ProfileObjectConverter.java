package com.rebuild.backend.utils.converters;

import com.rebuild.backend.model.entities.profile_entities.ProfileEducation;
import com.rebuild.backend.model.entities.profile_entities.ProfileExperience;
import com.rebuild.backend.model.entities.profile_entities.ProfileHeader;
import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import org.springframework.stereotype.Component;

@Component
public class ProfileObjectConverter{

    public Experience convertToExperience(ProfileExperience profileExperience){
        return new Experience(profileExperience.getCompanyName(), profileExperience.getTechnologyList(),
                profileExperience.getTimePeriod(), profileExperience.getBullets());
    }

    public Header convertToHeader(ProfileHeader profileHeader){
        return new Header(profileHeader.getNumber(), profileHeader.getName(),
                profileHeader.getEmail());
    }


    public Education convertToEducation(ProfileEducation profileEducation){
        return new Education(profileEducation.getSchoolName(), profileEducation.getRelevantCoursework());
    }
}
