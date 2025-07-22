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

    public Experience convertToExperience(Experience profileExperience){
        return new Experience(profileExperience.getCompanyName(), profileExperience.getTechnologyList(),
                profileExperience.getLocation(),
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

    public ResumeSection convertToSection(ResumeSection profileSection){
        List<ResumeSectionEntry> entries = profileSection.getEntries();

        ResumeSection newSection = new ResumeSection(profileSection.getTitle());
        List<ResumeSectionEntry> convertedEntries = entries.stream()
                .map(this::convertToResumeSectionEntry).toList();
        newSection.setEntries(convertedEntries);
        return newSection;
    }

    private ResumeSectionEntry convertToResumeSectionEntry(ResumeSectionEntry profileSectionEntry){
        return new ResumeSectionEntry(profileSectionEntry.getTitle(),
                profileSectionEntry.getToolsUsed(), profileSectionEntry.getLocation(),
                profileSectionEntry.getStartDate(), profileSectionEntry.getEndDate(),
                profileSectionEntry.getBullets());
    }

    private <I, O> List<O> convertToOutputList(List<I> inputList, Function<I, O> converter){
        return inputList.stream().map(converter).collect(Collectors.toList());
    }

    private <I, O, R> List<O> convertToOutputList(List<I> inputList, R root, BiFunction<I, R, O> converter){
        return inputList.stream().map(input -> converter.apply(input, root)).collect(Collectors.toList());
    }

    public List<ResumeSection> extractProfileSections(List<ProfileSectionForm> profileSectionForms){

        return convertToOutputList(profileSectionForms, rawForm -> {
            ResumeSection newSection = new ResumeSection(rawForm.title());
            List<ResumeSectionEntry> sectionEntries = extractProfileSectionEntries(rawForm.entryForms(),
                    newSection);
            newSection.setEntries(sectionEntries);
            return newSection;
        });
    }

    private List<ResumeSectionEntry> extractProfileSectionEntries(List<ProfileSectionEntryForm>
                                                                   entryForms, ResumeSection section){

        return convertToOutputList(entryForms, section, (rawForm, root) -> {
            return new ResumeSectionEntry(rawForm.title(), rawForm.toolsUsed(),
                    rawForm.location(), YearMonthStringOperations.getYearMonth(rawForm.startTime()),
                    YearMonthStringOperations.getYearMonth(rawForm.endTime()),
                    rawForm.bullets());
        });
    }

    public List<ResumeSection> extractResumeSections(List<SectionForm> sectionForms, Resume associatedResume){

        return convertToOutputList(sectionForms, rawForm -> {
            ResumeSection newSection = new ResumeSection(rawForm.title());
            List<ResumeSectionEntry> sectionEntries = extractResumeSectionEntries(rawForm.entryForms(),
                    newSection);
            newSection.setEntries(sectionEntries);
            return newSection;
        });
    }

    public List<ResumeSectionEntry> extractResumeSectionEntries(List<ResumeSectionEntryForm>
                                                                 entryForms, ResumeSection section){

        return convertToOutputList(entryForms, section, (rawForm , root) -> {
            ResumeSectionEntry newEntry =
                    new ResumeSectionEntry(rawForm.title(), rawForm.toolsUsed(),
                            rawForm.location(),
                            YearMonthStringOperations.getYearMonth(rawForm.startTime()),
                            YearMonthStringOperations.getYearMonth(rawForm.endTime()),
                            rawForm.bullets());
            return newEntry;
        });

    }

    public List<Experience> extractProfileExperiences(List<ProfileExperienceForm> profileExperienceForms,
                                                             UserProfile profile){
        return convertToOutputList(profileExperienceForms, rawForm ->
                new Experience(rawForm.companyName(),
                rawForm.technologies(), rawForm.location(),
                YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                rawForm.bullets()));

    }

    public List<Experience> extractExperiences(List<ExperienceForm> experienceForms, Resume associatedResume){
        return convertToOutputList(experienceForms, rawForm -> new Experience(rawForm.companyName(),
                rawForm.technologies(), rawForm.location(),
                YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                rawForm.bullets()));
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
                        rawExperience.getStartDate(),
                        rawExperience.getEndDate(),
                        rawExperience.getBullets()
                )
        ).toList();
    }

    private List<ResumeSectionEntry> createVersionedEntries(List<ResumeSectionEntry> rawEntries,
                                                               ResumeSection section){
        return rawEntries.stream().map(
                rawEntry -> new ResumeSectionEntry(
                        rawEntry.getTitle(), rawEntry.getToolsUsed(),
                        rawEntry.getLocation(),
                        rawEntry.getStartDate(),
                        rawEntry.getEndDate(),
                        rawEntry.getBullets()
                )
        ).toList();
    }

    public List<ResumeSection> createVersionedSections(List<ResumeSection> originalSections,
                                                          boolean shouldBeNull,
                                                          ResumeVersion resumeVersion){
        if(shouldBeNull){
            return null;
        }

        return originalSections.stream().map(
                rawSection -> {
                    ResumeSection newSection = new ResumeSection(
                            rawSection.getTitle()
                    );
                    newSection.setEntries(createVersionedEntries(rawSection.getEntries(), newSection));
                    return newSection;
                }
        ).toList();
    }
}
