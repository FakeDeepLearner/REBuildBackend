package com.rebuild.backend.utils.converters;

import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.forms.profile_forms.ProfileExperienceForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileSectionEntryForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileSectionForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.ResumeSectionEntryForm;
import com.rebuild.backend.model.forms.resume_forms.SectionForm;
import com.rebuild.backend.utils.YearMonthStringOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private <I, O> List<O> convertToOutputList(List<I> inputList, Function<I, O> converter){
        return inputList.stream().map(converter).collect(Collectors.toList());
    }

    private <I, O, R> List<O> convertToOutputList(List<I> inputList, R root, BiFunction<I, R, O> converter){
        return inputList.stream().map(input -> converter.apply(input, root)).collect(Collectors.toList());
    }

    public List<ProfileSection> extractProfileSections(List<ProfileSectionForm> profileSectionForms){

        return convertToOutputList(profileSectionForms, rawForm -> {
            ProfileSection newSection = new ProfileSection(rawForm.title());
            List<ProfileSectionEntry> sectionEntries = extractProfileSectionEntries(rawForm.entryForms(),
                    newSection);
            newSection.setEntries(sectionEntries);
            return newSection;
        });
    }

    private List<ProfileSectionEntry> extractProfileSectionEntries(List<ProfileSectionEntryForm>
                                                                   entryForms, ProfileSection section){

        return convertToOutputList(entryForms, section, (rawForm, root) -> {
            ProfileSectionEntry newEntry =
                    new ProfileSectionEntry(rawForm.title(), rawForm.toolsUsed(),
                            rawForm.location(), rawForm.bullets());
            newEntry.setAssociatedSection(section);
            return newEntry;
        });
    }

    public List<ResumeSection> extractResumeSections(List<SectionForm> sectionForms, Resume associatedResume){

        return convertToOutputList(sectionForms, rawForm -> {
            ResumeSection newSection = new ResumeSection(rawForm.title());
            List<ResumeSectionEntry> sectionEntries = extractResumeSectionEntries(rawForm.entryForms(),
                    newSection);
            newSection.setEntries(sectionEntries);
            newSection.setResume(associatedResume);
            return newSection;
        });
    }

    public List<ResumeSectionEntry> extractResumeSectionEntries(List<ResumeSectionEntryForm>
                                                                 entryForms, ResumeSection section){

        return convertToOutputList(entryForms, section, (rawForm , root) -> {
            ResumeSectionEntry newEntry =
                    new ResumeSectionEntry(rawForm.title(), rawForm.toolsUsed(),
                            rawForm.location(), rawForm.bullets());
            newEntry.setAssociatedSection(section);
            return newEntry;
        });

    }

    public List<ProfileExperience> extractProfileExperiences(List<ProfileExperienceForm> profileExperienceForms,
                                                             UserProfile profile){
        return convertToOutputList(profileExperienceForms, rawForm -> {
            ProfileExperience newExperience = new ProfileExperience(rawForm.companyName(),
                    rawForm.technologies(), rawForm.location(),
                    YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                    YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                    rawForm.bullets());
            newExperience.setProfile(profile);
            return newExperience;
        });

    }

    public List<Experience> extractExperiences(List<ExperienceForm> experienceForms, Resume associatedResume){
        return convertToOutputList(experienceForms, rawForm -> {
            Experience newExperience  = new Experience(rawForm.companyName(),
                    rawForm.technologies(), rawForm.location(),
                    YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                    YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                    rawForm.bullets());
            newExperience.setResume(associatedResume);
            return newExperience;
        });
    }

    public VersionedHeader createVersionedHeader(Header originalHeader, boolean shouldBeNull,
                                                 ResumeVersion resumeVersion){
        if(shouldBeNull){
            return null;
        }

        VersionedHeader newHeader = new VersionedHeader(originalHeader.getNumber(), originalHeader.getFirstName(),
                originalHeader.getLastName(), originalHeader.getEmail());
        newHeader.setAssociatedVersion(resumeVersion);
        return newHeader;
    }


    public VersionedEducation createVersionedEducation(Education originalEducation, boolean shouldBeNull,
                                                       ResumeVersion resumeVersion){
        if(shouldBeNull){
            return null;
        }

        VersionedEducation newEducation = new VersionedEducation(originalEducation.getSchoolName(), originalEducation.getRelevantCoursework(),
                originalEducation.getLocation(), originalEducation.getStartDate(), originalEducation.getEndDate());
        newEducation.setAssociatedVersion(resumeVersion);
        return newEducation;
    }

    public List<VersionedExperience> createVersionedExperiences(List<Experience> originalExperiences,
                                                                boolean shouldBeNull,
                                                                ResumeVersion resumeVersion){
        if(shouldBeNull){
            return null;
        }

        return originalExperiences.stream().map(
                rawExperience -> {
                    VersionedExperience newExperience = new VersionedExperience(
                            rawExperience.getCompanyName(),
                            rawExperience.getTechnologyList(),
                            rawExperience.getLocation(),
                            rawExperience.getStartDate(),
                            rawExperience.getEndDate(),
                            rawExperience.getBullets()
                    );
                    newExperience.setAssociatedVersion(resumeVersion);
                    return newExperience;
                }
        ).toList();
    }

    private List<VersionedSectionEntry> createVersionedEntries(List<ResumeSectionEntry> rawEntries,
                                                               VersionedSection section){
        return rawEntries.stream().map(
                rawEntry -> {
                    VersionedSectionEntry newEntry =  new VersionedSectionEntry(
                            rawEntry.getTitle(), rawEntry.getToolsUsed(),
                            rawEntry.getLocation(), rawEntry.getBullets()
                    );
                    newEntry.setAssociatedSection(section);
                    return newEntry;
                }
        ).toList();
    }

    public List<VersionedSection> createVersionedSections(List<ResumeSection> originalSections,
                                                          boolean shouldBeNull,
                                                          ResumeVersion resumeVersion){
        if(shouldBeNull){
            return null;
        }

        return originalSections.stream().map(
                rawSection -> {
                    VersionedSection newSection = new VersionedSection(
                            rawSection.getTitle()
                    );
                    newSection.setEntries(createVersionedEntries(rawSection.getEntries(), newSection));
                    newSection.setAssociatedVersion(resumeVersion);
                    return newSection;
                }
        ).toList();
    }
}
