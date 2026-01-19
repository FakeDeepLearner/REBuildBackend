package com.rebuild.backend.service.user_services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.forms.profile_forms.ProfilePreferencesForm;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.service.util_services.SubpartsModificationUtility;
import com.rebuild.backend.utils.converters.YearMonthStringOperations;
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
    public UserProfile createFullProfileFor(FullInformationForm profileForm, ProfilePreferencesForm preferencesForm,
                                            User updatingUser, MultipartFile pictureFile) throws IOException {
        UserProfile profile = profileRepository.findByUserWithAllData(updatingUser);
        UserProfile updatedProfile = getUserProfile(profile, profileForm, preferencesForm, pictureFile);
        return profileRepository.save(updatedProfile);
    }

    private UserProfile getUserProfile(UserProfile profile,
                                       FullInformationForm profileForm, ProfilePreferencesForm preferencesForm,
                                       MultipartFile pictureFile) throws IOException {
        YearMonth startDate  = YearMonthStringOperations.getYearMonth(profileForm.educationForm().startDate());
        YearMonth endDate  = YearMonthStringOperations.getYearMonth(profileForm.educationForm().endDate());

        UserProfile updatedProfile = updateHeaderAndEducation(profile, profileForm, startDate, endDate);
        List<Experience> experiences = extractProfileExperiences(profileForm.experiences(), updatedProfile);
        List<Project> projects = extractProfileProjects(profileForm.projects(), updatedProfile);
        updatedProfile.setExperienceList(experiences);
        updatedProfile.setProjectList(projects);

        ProfileSettings newSettings = new ProfileSettings(preferencesForm.publicPostHistory(),
                preferencesForm.publicCommentHistory(), preferencesForm.messagesFromFriendsOnly());
        newSettings.setAssociatedProfile(updatedProfile);
        updatedProfile.setSettings(newSettings);


        modifyProfilePictureOf(updatedProfile.getUser(), pictureFile);
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
        profile.setHeader(newHeader);
        newHeader.setProfile(profile);
    }


    public void modifyProfileEducation(UserProfile profile, Education newEducation)
    {
        profile.setEducation(newEducation);
        newEducation.setProfile(profile);
    }



    public UserProfile modifyProfilePictureOf(User chngingUser, MultipartFile pictureFile) throws IOException
    {
        UserProfile profile = profileRepository.findByUser(chngingUser);
        if (!pictureFile.isEmpty())
        {
            if(profile.getProfilePicture() != null)
            {
                profilePictureRepository.deleteProfilePictureByPublic_id(profile.getProfilePicture().getPublic_id());
                cloudinary.uploader().destroy(profile.getProfilePicture().getPublic_id(),
                        ObjectUtils.asMap("type", "private"));
            }


            @SuppressWarnings("JvmTaintAnalysis")
            Map uploadResult = cloudinary.uploader().upload(FileCopyUtils.
                            copyToByteArray(pictureFile.getInputStream()),
                    ObjectUtils.asMap("type", "private"));
            ProfilePicture profilePicture = new ProfilePicture((String) uploadResult.get("public_id"),
                    (String) uploadResult.get("asset_id"));
            profile.setProfilePicture(profilePicture);
            profilePicture.setAssociatedProfile(profile);
        }
        return profile;
    }

    @Transactional
    public UserProfile updateProfileHeader(HeaderForm headerForm, User user) {
        return modificationUtility.modifyProfileHeader(headerForm, user);
    }

    @Transactional
    public UserProfile updateProfileEducation(EducationForm educationForm, User user) {
        return modificationUtility.modifyProfileEducation(educationForm, user);
    }

    public UserProfile updateProfileExperience(ExperienceForm experienceForm, User user,
                                              UUID experienceId) {
        return modificationUtility.modifyProfileExperience(experienceForm, experienceId, user);
    }


    public UserProfile updateProfileProject(ProjectForm projectForm, User user, UUID projectId) {
        return modificationUtility.modifyProfileProject(projectForm, projectId, user);
    }

    @Transactional
    public UserProfile updateProfileExperiences(User updatingUser,
                                                List<ExperienceForm> newExperiences){
        UserProfile profile = profileRepository.findByUser(updatingUser);

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
    public void deleteProfile(User deletingUser){
        UserProfile profile = profileRepository.findByUser(deletingUser);
        profileRepository.delete(profile);
    }

    @Transactional
    public UserProfile deleteProfileExperiences(User deletingUser){
        UserProfile profile = profileRepository.findByUser(deletingUser);
        profile.setExperienceList(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteProfileProjects(User deletingUser){
        UserProfile profile = profileRepository.findByUser(deletingUser);
        profile.setExperienceList(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteProfileEducation(User deletingUser){
        UserProfile profile = profileRepository.findByUser(deletingUser);
        profile.setEducation(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteProfileHeader(User deletingUser){
        UserProfile profile = profileRepository.findByUser(deletingUser);
        profile.setHeader(null);
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteSpecificProfileExperience(User deletingUser, UUID experience_id){
        UserProfile profile = profileRepository.findByUserWithExperiences(deletingUser);
        profile.getExperienceList().
                removeIf(profileExperience ->
                profileExperience.getId().equals(experience_id)
        );
        return profileRepository.save(profile);
    }

    @Transactional
    public UserProfile deleteSpecificProfileProject(User deletingUser, UUID project_id){
        UserProfile profile = profileRepository.findByUserWithProjects(deletingUser);
        profile.getProjectList().
                removeIf(profileProject -> profileProject.getId().equals(project_id));
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

    private List<Project> extractProfileProjects(List<ProjectForm> projectForms, UserProfile profile){
        return projectForms.stream().map(rawForm -> {
            Project newProject = new Project(rawForm.projectName(), rawForm.technologyList(),
                    YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                    YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                    rawForm.bullets());
            newProject.setProfile(profile);
            return newProject;
            }
        ).toList();
    }

}
