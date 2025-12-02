package com.rebuild.backend.service.user_services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.service.util_services.SubpartsModificationUtility;
import com.rebuild.backend.utils.YearMonthStringOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    private final SubpartsModificationUtility modificationUtility;

    private final ProfilePictureRepository profilePictureRepository;

    private final Cloudinary cloudinary;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, SubpartsModificationUtility modificationUtility,
                          ProfilePictureRepository profilePictureRepository, Cloudinary cloudinary) {
        this.profileRepository = profileRepository;
        this.modificationUtility = modificationUtility;
        this.profilePictureRepository = profilePictureRepository;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public UserProfile createFullProfileFor(FullInformationForm profileForm, User updatingUser,
                                            MultipartFile pictureFile, boolean messagesFromFriends) throws IOException {
        UserProfile updatedProfile = getUserProfile(updatingUser.getProfile(), profileForm, pictureFile,
                messagesFromFriends);
        return profileRepository.save(updatedProfile);
    }

    private UserProfile getProfileByIdAndUser(UUID id, User user) {
        return profileRepository.findByIdAndUser(id, user).orElseThrow(
                () -> new BelongingException("The profile either does not exist or does not belong to you.")
        );
    }


    private UserProfile getUserProfile(UserProfile profile,
                                       FullInformationForm profileForm, MultipartFile pictureFile,
                                       boolean messagesFromFriends) throws IOException {
        YearMonth startDate  = YearMonthStringOperations.getYearMonth(profileForm.educationForm().startDate());
        YearMonth endDate  = YearMonthStringOperations.getYearMonth(profileForm.educationForm().endDate());

        UserProfile updatedProfile = updateHeaderAndEducation(profile, profileForm, startDate, endDate);
        List<Experience> experiences = extractProfileExperiences(profileForm.experiences(), updatedProfile);
        updatedProfile.setExperienceList(experiences);
        updatedProfile.setMessagesFromFriendsOnly(messagesFromFriends);

        modifyProfilePictureOf(updatedProfile.getUser(), profile.getId(), pictureFile);
        return profileRepository.save(updatedProfile);
    }

    private UserProfile updateHeaderAndEducation(UserProfile profile, FullInformationForm profileForm,
                                                 YearMonth startDate, YearMonth endDate) {
        Header profileHeader = new Header(profileForm.headerForm().number(),
                profileForm.headerForm().firstName(),
                profileForm.headerForm().lastName(),
                profileForm.headerForm().email());
        Education newEducation = new Education(profileForm.educationForm().schoolName(),
                profileForm.educationForm().relevantCoursework(),
                profileForm.educationForm().location(), startDate, endDate);
        modifyProfileHeader(profile, profileHeader);
        modifyProfileEducation(profile, newEducation);
        return profile;
    }

    public void modifyProfileHeader(UserProfile profile, Header newHeader)
    {
        Header profileHeader = profile.getHeader();

        if (profileHeader == null) {
            profile.setHeader(newHeader);
            newHeader.setProfile(profile);
            return;
        }

        modificationUtility.modifyHeaderData(newHeader, profileHeader);
    }


    public void modifyProfileEducation(UserProfile profile, Education newEducation)
    {
        Education profileEducation = profile.getEducation();

        if (profileEducation == null) {
            profile.setEducation(newEducation);
            newEducation.setProfile(profile);
            return;
        }

        modificationUtility.modifyEducationData(newEducation, profileEducation);
    }



    public UserProfile modifyProfilePictureOf(User chngingUser, UUID profileId, MultipartFile pictureFile) throws IOException
    {
        UserProfile profile = getProfileByIdAndUser(profileId, chngingUser);
        if (!pictureFile.isEmpty())
        {
            if(profile.getProfilePicture() != null)
            {
                profilePictureRepository.deleteProfilePictureByPublic_id(profile.getProfilePicture().getPublic_id());
                cloudinary.uploader().destroy(profile.getProfilePicture().getPublic_id(),
                        ObjectUtils.emptyMap());
            }

            @SuppressWarnings("JvmTaintAnalysis")
            Map uploadResult = cloudinary.uploader().upload(FileCopyUtils.
                            copyToByteArray(pictureFile.getInputStream()),
                    ObjectUtils.emptyMap());
            ProfilePicture profilePicture = new ProfilePicture((String) uploadResult.get("public_id"),
                    (String) uploadResult.get("asset_id"), (String) uploadResult.get("secure_url"));
            profile.setProfilePicture(profilePicture);
            profilePicture.setAssociatedProfile(profile);
        }
        return profile;
    }

    @Transactional
    public UserProfile changePageSize(User changingUser, UUID profileId, int newPageSize){
        UserProfile profile = getProfileByIdAndUser(profileId, changingUser);
        profile.setForumPageSize(newPageSize);
        return profileRepository.save(profile);
    }

    @Transactional
    public Header updateProfileHeader(HeaderForm headerForm, UUID header_id, UUID profile_id, User user) {
        return modificationUtility.modifyProfileHeader(headerForm, header_id, profile_id, user);
    }

    @Transactional
    public Education updateProfileEducation(EducationForm educationForm, UUID education_id, UUID profile_id, User user) {
        return modificationUtility.modifyProfileEducation(educationForm, education_id, profile_id, user);
    }

    @Transactional
    public UserProfile updateProfileExperiences(UUID profileId, User updatingUser,
                                                List<ExperienceForm> newExperiences){
        UserProfile profile = getProfileByIdAndUser(profileId, updatingUser);

        List<Experience> transformedExperiences = newExperiences.stream().
                map((rawExperienceData) -> {
                    YearMonth startDate = YearMonthStringOperations.getYearMonth(rawExperienceData.startDate());
                    YearMonth endDate = YearMonthStringOperations.getYearMonth(rawExperienceData.endDate());
                    return new Experience(rawExperienceData.companyName(), rawExperienceData.technologies(),
                    rawExperienceData.location(), rawExperienceData.experienceType(),
                    startDate, endDate, rawExperienceData.bullets());
                }).toList();

        profile.setExperienceList(transformedExperiences);
        return profileRepository.save(profile);

    }

    @Transactional
    public void deleteProfile(User deletingUser, UUID profile_id){
        UserProfile profile = getProfileByIdAndUser(profile_id, deletingUser);
        profileRepository.delete(profile);
    }

    @Transactional
    public UserProfile deleteProfileExperiences(User deletingUser, UUID profileId){
        UserProfile profile = getProfileByIdAndUser(profileId, deletingUser);
        profile.setExperienceList(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteProfileEducation(User deletingUser, UUID profileId){
        UserProfile profile = getProfileByIdAndUser(profileId, deletingUser);
        profile.setEducation(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteProfileHeader(User deletingUser, UUID profileId){
        UserProfile profile = getProfileByIdAndUser(profileId, deletingUser);
        profile.setHeader(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteSpecificProfileExperience(User deletingUser, UUID profileId ,UUID experience_id){
        UserProfile profile = getProfileByIdAndUser(profileId, deletingUser);
        profile.getExperienceList().
                removeIf(profileExperience ->
                profileExperience.getId().equals(experience_id)
        );
        return profileRepository.save(profile);
    }


    private List<Experience> extractProfileExperiences(List<ExperienceForm> experienceForms, UserProfile profile){
        return experienceForms.stream().map( rawForm -> {
                    Experience newExperience = new Experience(rawForm.companyName(),
                            rawForm.technologies(), rawForm.location(), rawForm.experienceType(),
                            YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                            YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                            rawForm.bullets());
                    newExperience.setProfile(profile);
                    return newExperience;
                }
        ).toList();

    }

}
