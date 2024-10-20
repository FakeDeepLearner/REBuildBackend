package com.rebuild.backend.service.user_services;

import com.rebuild.backend.exceptions.conflict_exceptions.UniqueProfileExperiencesException;
import com.rebuild.backend.exceptions.conflict_exceptions.UniqueProfileSectionsException;
import com.rebuild.backend.exceptions.profile_exceptions.NoProfileException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeCompanyConstraintException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeSectionConstraintException;
import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.users.User;
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

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Autowired
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public UserProfile createFullProfileFor(FullProfileForm profileForm, User creatingUser) {
        ProfileHeader profileHeader = new ProfileHeader(profileForm.phoneNumber(),
                profileForm.name(),
                profileForm.email());
        ProfileEducation newEducation = new ProfileEducation(profileForm.schoolName(),
                profileForm.relevantCoursework());
        try {
            UserProfile newProfile = new UserProfile(profileHeader, newEducation, profileForm.experiences(),
                    profileForm.sections());
            newProfile.setUser(creatingUser);
            creatingUser.setProfile(newProfile);
            return profileRepository.save(newProfile);
        }
        catch(DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                switch (violationException.getConstraintName()){
                    case "uk_profile_sections": throw new UniqueProfileSectionsException("The profile sections can't have more " +
                            "than 1 section with the same title");
                    case "uk_profile_experiences": throw new UniqueProfileExperiencesException("The profile experiences can't have more " +
                            "than experience with the same company");
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return null;
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

    public UserProfile updateProfileExperiences(UserProfile profile,
                                                List<ProfileExperienceForm> newExperiences){
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
            return profileRepository.save(profile);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_profile_experiences")){
                throw new UniqueProfileExperiencesException("The new profile experiences can't have more than 1 " +
                        "experience with the same company");
            }
        }
        return profile;
    }

    public UserProfile updateProfileSections(UserProfile profile,
                                             List<ProfileSectionForm> sectionForms){
        List<ProfileSection> transformedSections = sectionForms.stream().
                map((rawSection) -> {
                    ProfileSection newSection =
                            new ProfileSection(rawSection.title(), rawSection.bullets());
                    newSection.setProfile(profile);
                    return newSection;
                }).toList();
        try {
            profile.setSections(transformedSections);
            return profileRepository.save(profile);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_profile_sections")){
                throw new UniqueProfileSectionsException("The new profile sections can't have more than 1 section with the same title");
            }
        }
        return profile;
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


    public UserProfile updateEntireProfile(UserProfile updatingProfile, FullProfileForm profileForm){
        try {
            updatingProfile.setExperienceList(profileForm.experiences());
            updatingProfile.setHeader(new ProfileHeader(profileForm.phoneNumber(),
                    profileForm.name(), profileForm.email()));
            updatingProfile.setEducation(new ProfileEducation(profileForm.schoolName(),
                    profileForm.relevantCoursework()));
            updatingProfile.setSections(profileForm.sections());
            return profileRepository.save(updatingProfile);
        }
        catch(DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                switch (violationException.getConstraintName()){
                    case "uk_profile_sections": throw new UniqueProfileSectionsException("The new profile sections can't have more " +
                            "than 1 section with the same title");
                    case "uk_profile_experiences": throw new UniqueProfileExperiencesException("The new profile experiences can't have more " +
                            "than experience with the same company");
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return updatingProfile;

    }

}
