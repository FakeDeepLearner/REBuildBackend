package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.model.forms.profile_forms.*;
import com.rebuild.backend.repository.ProfileRepository;
import com.rebuild.backend.utils.YearMonthStringOperations;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.CONFLICT;

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
    public OptionalValueAndErrorResult<UserProfile> createFullProfileFor(FullProfileForm profileForm,
                                                                         User creatingUser) {
        UserProfile newProfile = getUserProfile(profileForm);
        try {
            newProfile.setUser(creatingUser);
            creatingUser.setProfile(newProfile);
            UserProfile savedProfile = profileRepository.save(newProfile);
            return OptionalValueAndErrorResult.of(savedProfile, CREATED);
        }
        catch(DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            creatingUser.setProfile(null);
            newProfile.setUser(null);
            if (cause instanceof ConstraintViolationException violationException){
                switch (violationException.getConstraintName()){
                    case "uk_profile_sections":
                        return OptionalValueAndErrorResult.of(newProfile,
                                "The profile sections can't have more than 1 section with the same title", CONFLICT);
                    case "uk_profile_experiences":
                        return OptionalValueAndErrorResult.of(
                                "The profile experiences can't have more than 1 experience with the same company", CONFLICT);
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return OptionalValueAndErrorResult.empty();
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
    public OptionalValueAndErrorResult<UserProfile> updateProfileExperiences(UserProfile profile,
                                                List<ProfileExperienceForm> newExperiences){
        List<Experience> oldExperiences = profile.getExperienceList();

        List<Experience> transformedExperiences = newExperiences.stream().
                map((rawExperienceData) -> {
                    YearMonth startDate = YearMonthStringOperations.getYearMonth(rawExperienceData.startDate());
                    YearMonth endDate = YearMonthStringOperations.getYearMonth(rawExperienceData.endDate());
                    return new Experience(rawExperienceData.companyName(), rawExperienceData.technologies(),
                    rawExperienceData.location(),
                    startDate, endDate, rawExperienceData.bullets());
                }).toList();
        try {
            profile.setExperienceList(transformedExperiences);
            UserProfile savedProfile = profileRepository.save(profile);
            return OptionalValueAndErrorResult.of(savedProfile, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            profile.setExperienceList(oldExperiences);
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_profile_experiences")){
                return OptionalValueAndErrorResult.of(profile,
                        "The new experiences can't have more than 1 experience with the same company", CONFLICT);
            }
        }
        return OptionalValueAndErrorResult.of(profile, "An unexpected error has occurred", INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public OptionalValueAndErrorResult<UserProfile> updateProfileSections(UserProfile profile,
                                             List<ProfileSectionForm> sectionForms){
        List<ResumeSection> oldSections = profile.getSections();
        List<ResumeSection> transformedSections = objectConverter.extractProfileSections(sectionForms);
        try {
            profile.setSections(transformedSections);
            UserProfile savedProfile = profileRepository.save(profile);
            return OptionalValueAndErrorResult.of(savedProfile, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            profile.setSections(oldSections);
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_profile_sections")){
                return OptionalValueAndErrorResult.of(profile,
                        "The new profile sections can't have more than 1 section with the same title", CONFLICT);
            }
        }
        return OptionalValueAndErrorResult.of(profile, "An unexpected error has occurred",
                INTERNAL_SERVER_ERROR);
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
    public OptionalValueAndErrorResult<UserProfile> updateEntireProfile(UserProfile updatingProfile,
                                                                        FullProfileForm profileForm){
        List<Experience> oldExperiences = updatingProfile.getExperienceList();
        List<ResumeSection> oldSections = updatingProfile.getSections();
        Header oldHeader = updatingProfile.getHeader();
        Education oldEducation = updatingProfile.getEducation();
        YearMonth startDate = YearMonthStringOperations.getYearMonth(profileForm.educationForm().startDate());
        YearMonth endDate = YearMonthStringOperations.getYearMonth(profileForm.educationForm().endDate());
        try {
            updatingProfile.setExperienceList(objectConverter.extractProfileExperiences(profileForm.experienceForms(),
                    updatingProfile));
            updatingProfile.setHeader(new Header(profileForm.headerForm().number(),
                    profileForm.headerForm().firstName(),
                    profileForm.headerForm().lastName(), profileForm.headerForm().email()));
            updatingProfile.setEducation(new Education(profileForm.educationForm().schoolName(),
                    profileForm.educationForm().relevantCoursework(),
                    profileForm.educationForm().location(), startDate, endDate));
            updatingProfile.setSections(objectConverter.extractProfileSections(profileForm.sectionForms()));
            UserProfile savedProfile = profileRepository.save(updatingProfile);
            return OptionalValueAndErrorResult.of(savedProfile, OK);
        }
        catch(DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            updatingProfile.setExperienceList(oldExperiences);
            updatingProfile.setSections(oldSections);
            updatingProfile.setHeader(oldHeader);
            updatingProfile.setEducation(oldEducation);
            if (cause instanceof ConstraintViolationException violationException){
                switch (violationException.getConstraintName()){
                    case "uk_profile_sections":
                        return OptionalValueAndErrorResult.of(updatingProfile,
                                "The new profile sections can't have more than 1 section with the same title",
                                CONFLICT);
                    case "uk_profile_experiences":
                        return OptionalValueAndErrorResult.of(updatingProfile,
                                "The new profile experiences can't have more " +
                                        "than experience with the same company", CONFLICT);
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return OptionalValueAndErrorResult.of(updatingProfile, "An unexpected error has occurred", INTERNAL_SERVER_ERROR);

    }

}
