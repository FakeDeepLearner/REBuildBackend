package com.rebuild.backend.service;

import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.entities.profile_entities.ProfileEducation;
import com.rebuild.backend.model.entities.profile_entities.ProfileExperience;
import com.rebuild.backend.model.entities.profile_entities.ProfileHeader;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.profile_forms.FullProfileForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileEducationForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileExperienceForm;
import com.rebuild.backend.model.forms.profile_forms.ProfileHeaderForm;
import com.rebuild.backend.repository.ProfileRepository;
import com.rebuild.backend.utils.ProfileObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileObjectConverter converter;

    @Autowired
    public ProfileService(ProfileRepository profileRepository,
                          ProfileObjectConverter converter) {
        this.profileRepository = profileRepository;
        this.converter = converter;
    }

    public UserProfile createProfile(FullProfileForm profileForm) {
        ProfileHeader profileHeader = new ProfileHeader(profileForm.phoneNumber(),
                profileForm.name(),
                profileForm.email());
        ProfileEducation newEducation = new ProfileEducation(profileForm.schoolName(),
                profileForm.relevantCoursework());

        return new UserProfile(profileHeader, newEducation, profileForm.experiences());
    }

    public UserProfile updateProfileHeader(UserProfile userProfile,
                                           ProfileHeaderForm headerForm) {
        ProfileHeader newHeader = new ProfileHeader(headerForm.number(), headerForm.name(), headerForm.email());
        userProfile.setHeader(newHeader);
        return profileRepository.save(userProfile);
    }

    public UserProfile updateProfileEducation(UserProfile userProfile,
                                              ProfileEducationForm educationForm) {
        ProfileEducation newEducation = new ProfileEducation(educationForm.schoolName(),
                educationForm.relevantCoursework());
        userProfile.setEducation(newEducation);
        return profileRepository.save(userProfile);
    }

    public UserProfile updateProfileExperiences(UserProfile profile,
                                                List<ProfileExperienceForm> newExperiences){
        List<ProfileExperience> transformedExperiences = newExperiences.stream().
                map((rawExperience) -> {
                    Duration experienceDuration = Duration.between(rawExperience.startDate(),
                            rawExperience.endDate());
                    return new ProfileExperience(rawExperience.companyName(), rawExperience.technologies(),
                            experienceDuration, rawExperience.bullets());
                }).toList();
        profile.setExperienceList(transformedExperiences);
        return profileRepository.save(profile);
    }

    public void deleteProfile(UserProfile profileToDelete){
        profileRepository.delete(profileToDelete);
    }


}
