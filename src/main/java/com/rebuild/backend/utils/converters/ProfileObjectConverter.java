package com.rebuild.backend.utils.converters;

import com.google.api.client.util.PemReader;
import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import org.springframework.stereotype.Component;

import java.util.List;

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
        List<ProfileSectionEntry> entries = profileSection.getEntries();
        List<ResumeSectionEntry> convertedEntries = entries.stream()
                .map(this::convertToResumeSectionEntry).toList();
        return new ResumeSection(profileSection.getTitle(), convertedEntries);
    }

    private ResumeSectionEntry convertToResumeSectionEntry(ProfileSectionEntry profileSectionEntry){
        return new ResumeSectionEntry(profileSectionEntry.getTitle(),
                profileSectionEntry.getToolsUsed(), profileSectionEntry.getLocation(),
                profileSectionEntry.getBullets());
    }
}
