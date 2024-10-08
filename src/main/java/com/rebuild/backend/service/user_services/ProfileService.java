package com.rebuild.backend.service.user_services;

import com.rebuild.backend.exceptions.profile_exceptions.NoProfileException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.profile_entities.ProfileEducation;
import com.rebuild.backend.model.entities.profile_entities.ProfileExperience;
import com.rebuild.backend.model.entities.profile_entities.ProfileHeader;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.profile_forms.FullProfileForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileEducationForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileExperienceForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileHeaderForm;
import com.rebuild.backend.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Autowired
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public UserProfile createProfileFor(FullProfileForm profileForm, User creatingUser) {
        ProfileHeader profileHeader = new ProfileHeader(profileForm.phoneNumber(),
                profileForm.name(),
                profileForm.email());
        ProfileEducation newEducation = new ProfileEducation(profileForm.schoolName(),
                profileForm.relevantCoursework());

        UserProfile newProfile = new UserProfile(profileHeader, newEducation, profileForm.experiences());
        newProfile.setUser(creatingUser);
        creatingUser.setProfile(newProfile);
        return profileRepository.save(newProfile);
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
        profile.setExperienceList(transformedExperiences);
        return profileRepository.save(profile);
    }

    public void deleteProfile(UUID user_id){
        profileRepository.deleteUserProfileByUserId(user_id);
    }

    public void deleteProfileExperiences(UUID user_id){
        profileRepository.deleteProfileExperiencesByUserId(user_id);
    }

    public void deleteProfileEducation(UUID user_id){
        profileRepository.deleteProfileEducationByUserId(user_id);
    }

    public void deleteProfileHeader(UUID user_id){
        profileRepository.deleteProfileHeaderByUserId(user_id);
    }
    
    public UserProfile deleteSpecificProfileExperience(UUID user_id, UUID experience_id){
        UserProfile profile = profileRepository.findByUserId(user_id).orElseThrow(() ->
                new NoProfileException("Profile not found for this user"));
        profile.getExperienceList().
                removeIf(profileExperience ->
                profileExperience.getId().equals(experience_id)
        );
        return profileRepository.save(profile);
    }


}
