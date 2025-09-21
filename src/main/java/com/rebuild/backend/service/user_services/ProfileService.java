package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.repository.ProfileRepository;
import com.rebuild.backend.service.util_services.SubpartsModificationUtility;
import com.rebuild.backend.utils.YearMonthStringOperations;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    private final SubpartsModificationUtility modificationUtility;

    private final ObjectConverter objectConverter;

    private final UserService userService;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, SubpartsModificationUtility modificationUtility,
                          ObjectConverter objectConverter, UserService userService) {
        this.profileRepository = profileRepository;
        this.modificationUtility = modificationUtility;
        this.objectConverter = objectConverter;
        this.userService = userService;
    }

    @Transactional
    public UserProfile createFullProfileFor(FullInformationForm profileForm, User updatingUser,
                                            MultipartFile pictureFile) throws IOException {
        UserProfile updatedProfile = getUserProfile(updatingUser.getProfile(), profileForm, pictureFile);
        return profileRepository.save(updatedProfile);
    }


    private UserProfile getUserProfile(UserProfile profile, FullInformationForm profileForm, MultipartFile pictureFile) throws IOException {
        YearMonth startDate  = YearMonthStringOperations.getYearMonth(profileForm.educationForm().startDate());
        YearMonth endDate  = YearMonthStringOperations.getYearMonth(profileForm.educationForm().endDate());

        UserProfile updatedProfile = getUserProfile(profile, profileForm, startDate, endDate);
        List<Experience> experiences = objectConverter.
                extractProfileExperiences(profileForm.experiences(), updatedProfile);
        updatedProfile.setExperienceList(experiences);

        userService.modifyProfilePictureOf(updatedProfile, pictureFile);
        return updatedProfile;
    }

    private UserProfile getUserProfile(UserProfile profile, FullInformationForm profileForm, YearMonth startDate, YearMonth endDate) {
        Header profileHeader = new Header(profileForm.headerForm().number(),
                profileForm.headerForm().firstName(),
                profileForm.headerForm().lastName(),
                profileForm.headerForm().email());
        Education newEducation = new Education(profileForm.educationForm().schoolName(),
                profileForm.educationForm().relevantCoursework(),
                profileForm.educationForm().location(), startDate, endDate);

        profile.setEducation(newEducation);
        profile.setHeader(profileHeader);
        return profile;
    }

    @Transactional
    public UserProfile changePageSize(UserProfile profile, int newPageSize){
        profile.setForumPageSize(newPageSize);
        return profileRepository.save(profile);
    }

    @Transactional
    public Header updateProfileHeader(HeaderForm headerForm, UUID header_id) {
        return modificationUtility.modifyHeader(headerForm, header_id);
    }

    @Transactional
    public Education updateProfileEducation(EducationForm educationForm, UUID education_id) {
        return modificationUtility.modifyEducation(educationForm, education_id);
    }

    @Transactional
    public UserProfile updateProfileExperiences(UserProfile profile,
                                                List<ExperienceForm> newExperiences){

        List<Experience> transformedExperiences = newExperiences.stream().
                map((rawExperienceData) -> {
                    YearMonth startDate = YearMonthStringOperations.getYearMonth(rawExperienceData.startDate());
                    YearMonth endDate = YearMonthStringOperations.getYearMonth(rawExperienceData.endDate());
                    List<ExperienceType> experienceTypes = objectConverter.convertToExperienceTypes(rawExperienceData.experienceTypeValues());
                    return new Experience(rawExperienceData.companyName(), rawExperienceData.technologies(),
                    rawExperienceData.location(), experienceTypes,
                    startDate, endDate, rawExperienceData.bullets());
                }).toList();

        profile.setExperienceList(transformedExperiences);
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

}
