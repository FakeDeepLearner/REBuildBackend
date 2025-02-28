package com.rebuild.backend.utils.converters;

import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.profile_forms.ProfileExperienceForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileSectionEntryForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileSectionForm;
import com.rebuild.backend.model.forms.resume_forms.ResumeSectionEntryForm;
import com.rebuild.backend.model.forms.resume_forms.SectionForm;
import com.rebuild.backend.utils.YearMonthStringOperations;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObjectConverter {

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

        ResumeSection newSection = new ResumeSection(profileSection.getTitle());
        List<ResumeSectionEntry> convertedEntries = entries.stream()
                .map(this::convertToResumeSectionEntry).toList();
        newSection.setEntries(convertedEntries);
        return newSection;
    }

    private ResumeSectionEntry convertToResumeSectionEntry(ProfileSectionEntry profileSectionEntry){
        return new ResumeSectionEntry(profileSectionEntry.getTitle(),
                profileSectionEntry.getToolsUsed(), profileSectionEntry.getLocation(),
                profileSectionEntry.getBullets());
    }

    public List<ProfileSection> extractProfileSections(List<ProfileSectionForm> profileSectionForms){

        return profileSectionForms.stream().map(
                rawForm -> {
                    ProfileSection newSection = new ProfileSection(rawForm.title());
                    List<ProfileSectionEntry> sectionEntries = extractProfileSectionEntries(rawForm.entryForms(),
                            newSection);
                    newSection.setEntries(sectionEntries);
                    return newSection;
                }
        ).toList();
    }

    private List<ProfileSectionEntry> extractProfileSectionEntries(List<ProfileSectionEntryForm>
                                                                   entryForms, ProfileSection section){
        return entryForms.stream().
                map(rawForm -> {
                    ProfileSectionEntry newEntry =
                            new ProfileSectionEntry(rawForm.title(), rawForm.toolsUsed(),
                                    rawForm.location(), rawForm.bullets());
                    newEntry.setAssociatedSection(section);
                    return newEntry;
                }).toList();
    }

    public List<ResumeSection> extractResumeSections(List<SectionForm> sectionForms){
        return sectionForms.stream().map(
                rawForm -> {
                    ResumeSection newSection = new ResumeSection(rawForm.title());
                    List<ResumeSectionEntry> sectionEntries = extractResumeSectionEntries(rawForm.entryForms(),
                            newSection);
                    newSection.setEntries(sectionEntries);
                    return newSection;
                }
        ).toList();
    }

    public List<ResumeSectionEntry> extractResumeSectionEntries(List<ResumeSectionEntryForm>
                                                                 entryForms, ResumeSection section){
        return entryForms.stream().
                map(rawForm -> {
                    ResumeSectionEntry newEntry =
                            new ResumeSectionEntry(rawForm.title(), rawForm.toolsUsed(),
                                    rawForm.location(), rawForm.bullets());
                    newEntry.setAssociatedSection(section);
                    return newEntry;
                }).toList();
    }

    public List<ProfileExperience> extractProfileExperiences(List<ProfileExperienceForm> profileExperienceForms,
                                                             UserProfile profile){
        return profileExperienceForms.stream().
                map(rawForm -> {
                    ProfileExperience newExperience = new ProfileExperience(rawForm.companyName(),
                            rawForm.technologies(), rawForm.location(),
                            YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                            YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                            rawForm.bullets());
                    newExperience.setProfile(profile);
                    return newExperience;
                }).toList();

    }
}
