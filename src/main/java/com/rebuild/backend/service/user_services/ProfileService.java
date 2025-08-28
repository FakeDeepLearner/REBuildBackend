package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.profile_forms.*;
import com.rebuild.backend.repository.ProfileRepository;
import com.rebuild.backend.utils.YearMonthStringOperations;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    private final ObjectConverter objectConverter;

    @Autowired
    public ProfileService(ProfileRepository profileRepository,
                          ObjectConverter objectConverter) {
        this.profileRepository = profileRepository;
        this.objectConverter = objectConverter;
    }

    @Transactional
    public UserProfile createFullProfileFor(FullProfileForm profileForm, User creatingUser) {
        UserProfile newProfile = getUserProfile(profileForm);
        newProfile.setUser(creatingUser);
        creatingUser.setProfile(newProfile);
        return profileRepository.save(newProfile);
    }


    private UserProfile getUserProfile(FullProfileForm profileForm) {
        YearMonth startDate  = YearMonthStringOperations.getYearMonth(profileForm.educationForm().startDate());
        YearMonth endDate  = YearMonthStringOperations.getYearMonth(profileForm.educationForm().endDate());
        Header profileHeader = new Header(profileForm.headerForm().number(),
                profileForm.headerForm().firstName(),
                profileForm.headerForm().lastName(),
                profileForm.headerForm().email());
        Education newEducation = new Education(profileForm.educationForm().schoolName(),
                profileForm.educationForm().relevantCoursework(),
                profileForm.educationForm().location(), startDate, endDate);
        List<ResumeSection> sections = objectConverter.extractProfileSections(profileForm.sectionForms());

        UserProfile newProfile =  new UserProfile(profileHeader, newEducation,
                new ArrayList<>(),
                sections);
        List<Experience> experiences = objectConverter.
                extractProfileExperiences(profileForm.experienceForms(), newProfile);
        newProfile.setExperienceList(experiences);
        return newProfile;
    }

    @Transactional
    public UserProfile changePageSize(UserProfile profile, int newPageSize){
        profile.setForumPageSize(newPageSize);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile updateProfileHeader(UserProfile userProfile,
                                           ProfileHeaderForm headerForm) {
        Header oldHeader = userProfile.getHeader();
        oldHeader.setFirstName(headerForm.firstName());
        oldHeader.setLastName(headerForm.lastName());
        oldHeader.setEmail(headerForm.email());
        oldHeader.setNumber(headerForm.number());
        return profileRepository.save(userProfile);
    }

    @Transactional
    public UserProfile updateProfileEducation(UserProfile userProfile,
                                              ProfileEducationForm educationForm) {
        Education oldEducation = userProfile.getEducation();

        YearMonth startDate = YearMonthStringOperations.getYearMonth(educationForm.startDate());
        YearMonth endDate = YearMonthStringOperations.getYearMonth(educationForm.endDate());
        oldEducation.setSchoolName(educationForm.schoolName());
        oldEducation.setRelevantCoursework(educationForm.relevantCoursework());
        oldEducation.setLocation(educationForm.location());
        oldEducation.setStartDate(startDate);
        oldEducation.setEndDate(endDate);
        return profileRepository.save(userProfile);
    }

    @Transactional
    public UserProfile updateProfileExperiences(UserProfile profile,
                                                List<ProfileExperienceForm> newExperiences){

        List<Experience> transformedExperiences = newExperiences.stream().
                map((rawExperienceData) -> {
                    YearMonth startDate = YearMonthStringOperations.getYearMonth(rawExperienceData.startDate());
                    YearMonth endDate = YearMonthStringOperations.getYearMonth(rawExperienceData.endDate());
                    return new Experience(rawExperienceData.companyName(), rawExperienceData.technologies(),
                    rawExperienceData.location(),
                    startDate, endDate, rawExperienceData.bullets());
                }).toList();

        profile.setExperienceList(transformedExperiences);
        return profileRepository.save(profile);

    }

    @Transactional
    public UserProfile updateProfileSections(UserProfile profile,
                                             List<ProfileSectionForm> sectionForms){
        List<ResumeSection> transformedSections = objectConverter.extractProfileSections(sectionForms);
        profile.setSections(transformedSections);
        return profileRepository.save(profile);


    }

    @Transactional
    public void deleteProfile(UUID profile_id){
        profileRepository.deleteById(profile_id);
    }

    @Transactional
    public UserProfile deleteProfileExperiences(UserProfile profile){
        profile.setExperienceList(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteProfileEducation(UserProfile profile){
        profile.setEducation(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteProfileSections(UserProfile profile){
        profile.setSections(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteProfileHeader(UserProfile profile){
        profile.setHeader(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteSpecificProfileExperience(UserProfile profile, UUID experience_id){
        profile.getExperienceList().
                removeIf(profileExperience ->
                profileExperience.getId().equals(experience_id)
        );
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteSpecificSection(UserProfile profile, UUID experience_id){
        profile.getSections().
                removeIf(section ->
                        section.getId().equals(experience_id)
                );
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile updateEntireProfile(UserProfile updatingProfile, FullProfileForm profileForm){
        YearMonth startDate = YearMonthStringOperations.getYearMonth(profileForm.educationForm().startDate());
        YearMonth endDate = YearMonthStringOperations.getYearMonth(profileForm.educationForm().endDate());

        updatingProfile.setExperienceList(objectConverter.extractProfileExperiences(profileForm.experienceForms(),
                updatingProfile));
        updatingProfile.setHeader(new Header(profileForm.headerForm().number(),
                profileForm.headerForm().firstName(),
                profileForm.headerForm().lastName(), profileForm.headerForm().email()));
        updatingProfile.setEducation(new Education(profileForm.educationForm().schoolName(),
                profileForm.educationForm().relevantCoursework(),
                profileForm.educationForm().location(), startDate, endDate));
        updatingProfile.setSections(objectConverter.extractProfileSections(profileForm.sectionForms()));
        return profileRepository.save(updatingProfile);
    }

}
