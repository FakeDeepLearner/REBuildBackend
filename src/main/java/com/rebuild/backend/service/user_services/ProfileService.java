package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.error_dtos.OptionalValueAndErrorResult;
import com.rebuild.backend.model.forms.profile_forms.*;
import com.rebuild.backend.repository.ProfileRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.rebuild.backend.model.forms.dtos.error_dtos.OptionalValueAndErrorResult.*;

@Service
@Transactional
/*
* The "of" and "empty" methods used throughout this class are the ones that are in OptionalValueAndErrorResult.
* They have been statically imported just above the class definition, mostly to save the inconvenience of typing the class name every time
* */
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Autowired
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public OptionalValueAndErrorResult<UserProfile> createFullProfileFor(FullProfileForm profileForm, User creatingUser) {
        ProfileHeader profileHeader = new ProfileHeader(profileForm.phoneNumber(),
                profileForm.name(),
                profileForm.email());
        ProfileEducation newEducation = new ProfileEducation(profileForm.schoolName(),
                profileForm.relevantCoursework());
        UserProfile newProfile = new UserProfile(profileHeader, newEducation, profileForm.experiences(),
                profileForm.sections());
        try {
            newProfile.setUser(creatingUser);
            creatingUser.setProfile(newProfile);
            UserProfile savedProfile = profileRepository.save(newProfile);
            return of(savedProfile);
        }
        catch(DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            creatingUser.setProfile(null);
            newProfile.setUser(null);
            if (cause instanceof ConstraintViolationException violationException){
                switch (violationException.getConstraintName()){
                    case "uk_profile_sections":
                        return of("The profile sections can't have more than 1 section with the same title");
                    case "uk_profile_experiences":
                        return of("The profile experiences can't have more than 1 experience with the same company");
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return empty();
    }

    public UserProfile changePageSize(UserProfile profile, int newPageSize){
        profile.setForumPageSize(newPageSize);
        return profileRepository.save(profile);
    }

    public UserProfile updateProfileHeader(UserProfile userProfile,
                                           ProfileHeaderForm headerForm) {
        ProfileHeader newHeader = new ProfileHeader(headerForm.number(), headerForm.name(), headerForm.email());
        userProfile.setHeader(newHeader);
        newHeader.setProfile(userProfile);
        return profileRepository.save(userProfile);
    }

    public UserProfile updateProfileEducation(UserProfile userProfile,
                                              ProfileEducationForm educationForm) {
        ProfileEducation newEducation = new ProfileEducation(educationForm.schoolName(),
                educationForm.relevantCoursework());
        userProfile.setEducation(newEducation);
        newEducation.setProfile(userProfile);
        return profileRepository.save(userProfile);
    }

    public OptionalValueAndErrorResult<UserProfile> updateProfileExperiences(UserProfile profile,
                                                List<ProfileExperienceForm> newExperiences){
        List<ProfileExperience> oldExperiences = profile.getExperienceList();
        List<ProfileExperience> transformedExperiences = newExperiences.stream().
                map((rawExperience) -> {
                    Duration experienceDuration = Duration.between(rawExperience.startDate(),
                            rawExperience.endDate());
                    ProfileExperience newExperience =
                            new ProfileExperience(rawExperience.companyName(), rawExperience.technologies(),
                            experienceDuration, rawExperience.bullets());
                    newExperience.setProfile(profile);
                    return newExperience;
                }).toList();
        try {
            profile.setExperienceList(transformedExperiences);
            UserProfile savedProfile = profileRepository.save(profile);
            return of(savedProfile);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            profile.setExperienceList(oldExperiences);
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_profile_experiences")){
                return of(profile, "The new experiences can't have more than 1 experience with the same company");
            }
        }
        return of(profile, "An unexpected error has occurred");
    }

    public OptionalValueAndErrorResult<UserProfile> updateProfileSections(UserProfile profile,
                                             List<ProfileSectionForm> sectionForms){
        List<ProfileSection> oldSections = profile.getSections();
        List<ProfileSection> transformedSections = sectionForms.stream().
                map((rawSection) -> {
                    ProfileSection newSection =
                            new ProfileSection(rawSection.title(), rawSection.bullets());
                    newSection.setProfile(profile);
                    return newSection;
                }).toList();
        try {
            profile.setSections(transformedSections);
            UserProfile savedProfile = profileRepository.save(profile);
            return of(savedProfile);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            profile.setSections(oldSections);
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_profile_sections")){
                return of(profile, "The new profile sections can't have more than 1 section with the same title");
            }
        }
        return of(profile, "An unexpected error has occurred");
    }

    public void deleteProfile(UUID profile_id){
        profileRepository.deleteById(profile_id);
    }

    public void deleteProfileExperiences(UUID profile_id){
        profileRepository.deleteProfileExperiencesById(profile_id);
    }

    public void deleteProfileEducation(UUID profile_id){
        profileRepository.deleteProfileEducationById(profile_id);
    }

    public void deleteProfileSections(UUID profile_id){
        profileRepository.deleteProfileSectionsById(profile_id);
    }

    public void deleteProfileHeader(UUID profile_id){
        profileRepository.deleteProfileHeaderById(profile_id);
    }
    
    public UserProfile deleteSpecificProfileExperience(UserProfile profile, UUID experience_id){
        profile.getExperienceList().
                removeIf(profileExperience ->
                profileExperience.getId().equals(experience_id)
        );
        return profileRepository.save(profile);
    }

    public UserProfile deleteSpecificSection(UserProfile profile, UUID experience_id){
        profile.getSections().
                removeIf(section ->
                        section.getId().equals(experience_id)
                );
        return profileRepository.save(profile);
    }


    public OptionalValueAndErrorResult<UserProfile> updateEntireProfile(UserProfile updatingProfile, FullProfileForm profileForm){
        List<ProfileExperience> oldExperiences = updatingProfile.getExperienceList();
        List<ProfileSection> oldSections = updatingProfile.getSections();
        ProfileHeader oldHeader = updatingProfile.getHeader();
        ProfileEducation oldEducation = updatingProfile.getEducation();
        try {
            updatingProfile.setExperienceList(profileForm.experiences());
            updatingProfile.setHeader(new ProfileHeader(profileForm.phoneNumber(),
                    profileForm.name(), profileForm.email()));
            updatingProfile.setEducation(new ProfileEducation(profileForm.schoolName(),
                    profileForm.relevantCoursework()));
            updatingProfile.setSections(profileForm.sections());
            UserProfile savedProfile = profileRepository.save(updatingProfile);
            return of(savedProfile);
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
                        return of(updatingProfile,
                                "The new profile sections can't have more than 1 section with the same title");
                    case "uk_profile_experiences":
                        return of(updatingProfile,
                                "The new profile experiences can't have more " +
                                        "than experience with the same company");
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return of(updatingProfile, "An unexpected error has occurred");

    }

}
