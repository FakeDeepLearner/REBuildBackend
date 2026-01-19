package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.search_entities.ResumeSearchConfiguration;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.exceptions.PrefillException;
import com.rebuild.backend.model.forms.resume_forms.*;

import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeSearchRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.service.util_services.SubpartsModificationUtility;
import com.rebuild.backend.utils.ResumeObtainer;
import com.rebuild.backend.utils.converters.YearMonthStringOperations;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private final SubpartsModificationUtility modificationUtility;

    private final ResumeObtainer getUtility;

    private final ResumeSearchRepository resumeSearchRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         SubpartsModificationUtility modificationUtility,
                         ResumeObtainer getUtility, ResumeSearchRepository resumeSearchRepository, ProfileRepository profileRepository) {
        this.resumeRepository = resumeRepository;
        this.modificationUtility = modificationUtility;
        this.getUtility = getUtility;
        this.resumeSearchRepository = resumeSearchRepository;
        this.profileRepository = profileRepository;
    }

    public ResumeSearchConfiguration createSearchConfig(User authenticatedUser,
                                                        ResumeSpecsForm specsForm){
        ResumeSearchConfiguration newConfiguration = new ResumeSearchConfiguration(specsForm);
        newConfiguration.setUser(authenticatedUser);
        authenticatedUser.getResumeSearchConfigurations().add(newConfiguration);
        return resumeSearchRepository.save(newConfiguration);
    }

    @Transactional
    public Resume changeHeaderInfo(HeaderForm headerForm, UUID resumeID, User user){
        return modificationUtility.modifyResumeHeader(headerForm, resumeID, user);
    }

    @Transactional
    public Resume createNewResumeFor(String resume_name, User user){
        Resume newResume = new Resume(resume_name, user);
        user.getResumes().add(newResume);
        return resumeRepository.save(newResume);

    }

    public Resume findByUserAndResumeId(User user, UUID resumeID){
        return getUtility.findByUserResumeId(user, resumeID);
    }

    @Transactional
    public Resume changeExperienceInfo(ExperienceForm experienceForm, UUID experienceID, UUID resumeId,
                                           User user){
        return modificationUtility.modifyResumeExperience(experienceForm, experienceID, resumeId, user);

    }

    public Resume changeProjectInfo(ProjectForm projectForm, UUID projectID, UUID resumeID, User user){
        return modificationUtility.modifyResumeProject(projectForm, projectID, resumeID, user);
    }

    @Transactional
    public Resume changeEducationInfo(EducationForm educationForm,
                                      UUID resumeID, User user){
       return modificationUtility.modifyResumeEducation(educationForm, resumeID, user);
    }

    @Transactional
    public Resume createNewExperience(User changingUser, UUID resumeId,
                                      ExperienceForm experienceForm){
        Resume resume = getUtility.findByUserAndIdWithExperiences(changingUser, resumeId);
        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        Experience newExperience = new Experience(experienceForm.companyName(),
                experienceForm.technologies(), experienceForm.location(), experienceForm.experienceType(),
                start, end, experienceForm.bullets());
        newExperience.setResume(resume);

        resume.getExperiences().add(newExperience);

        return resumeRepository.save(resume);

    }

    public Resume createNewProject(User changingUser, UUID resumeId, ProjectForm projectForm){

        Resume resume = getUtility.findByUserAndIdWithProjects(changingUser, resumeId);
        YearMonth start = YearMonthStringOperations.getYearMonth(projectForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(projectForm.endDate());
        Project newProject = new Project(projectForm.projectName(), projectForm.technologyList(),
                start, end, projectForm.bullets());
        newProject.setResume(resume);
        resume.getProjects().add(newProject);

        return resumeRepository.save(resume);

    }

    @Transactional
    public void deleteById(User deletingUser, UUID id){
        Resume resume = getUtility.findByUserResumeId(deletingUser, id);
        resumeRepository.delete(resume);
    }

    @Transactional
    public Resume deleteEducation(User changingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        resume.setEducation(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteExperience(User changingUser, UUID resumeId, UUID experienceId){
        Resume resume = getUtility.findByUserAndIdWithExperiences(changingUser, resumeId);
        resume.getExperiences().removeIf(experience -> experience.getId().equals(experienceId));
        return resumeRepository.save(resume);
    }


    @Transactional
    public Resume deleteProject(User changingUser, UUID resumeId, UUID projectId){
        Resume resume = getUtility.findByUserAndIdWithProjects(changingUser, resumeId);
        resume.getProjects().removeIf(project -> project.getId().equals(projectId));
        return resumeRepository.save(resume);
    }


    @Transactional
    public Resume deleteHeader(User changingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        resume.setHeader(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume fullUpdate(User updatingUser, UUID resumeID,
                             FullInformationForm resumeForm) {
        Resume resume = getUtility.findByUserResumeId(updatingUser, resumeID);


        //We can't modify the resume's fields directly here, as that would also modify the variables that
        // we declared outside the try block, causing a bug.
        Header newHeader = new Header(resumeForm.headerForm().number(),
                resumeForm.headerForm().firstName(),
                resumeForm.headerForm().lastName(), resumeForm.headerForm().email());
        resume.setHeader(newHeader);

        Education newEducation = new Education(resumeForm.educationForm().schoolName(),
                resumeForm.educationForm().relevantCoursework(),
                resumeForm.educationForm().location(),
                YearMonthStringOperations.getYearMonth(resumeForm.educationForm().startDate()),
                YearMonthStringOperations.getYearMonth(resumeForm.educationForm().endDate()));
        resume.setEducation(newEducation);

        resume.setProjects(extractProjects(resumeForm.projects(), resume));

        resume.setExperiences(extractExperiences(resumeForm.experiences(), resume));

        return resumeRepository.save(resume);

    }

    @Transactional
    public Resume changeName(User changingUser, UUID resumeId, String newName){
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);
        changingResume.setName(newName);
        return resumeRepository.save(changingResume);

    }

    @Transactional
    public Resume copyResume(User user, UUID resumeId, ResumeCreationForm creationForm){
        Resume copiedResume = getUtility.findByUserAndIdWithAllInfo(user, resumeId);
        if(creationForm.newName().equals(copiedResume.getName())){
            throw new RuntimeException("The new resume must have a different name than the original one.");
        }
        Resume newResume = new Resume(copiedResume, creationForm);
        return resumeRepository.save(newResume);

    }


    public Resume prefillHeader(UUID resumeID, User authenticatedUser)
    {
        Resume associatedResume = getUtility.findByUserResumeId(authenticatedUser, resumeID);
        UserProfile profile = profileRepository.findByUserWithHeader(authenticatedUser);
        Header header = profile.getHeader();
        if(header == null){
            throw new PrefillException("Your profile does not have a header set");
        }
        Header newHeader = Header.copy(header);
        associatedResume.setHeader(newHeader);
        return resumeRepository.save(associatedResume);
    }

    public Resume prefillEducation(UUID resumeID, User authenticatedUser)
    {
        Resume associatedResume = getUtility.findByUserResumeId(authenticatedUser, resumeID);
        UserProfile profile = profileRepository.findByUserWithEducation(authenticatedUser);
        Education education = profile.getEducation();
        if(education == null){
            throw new PrefillException("Your profile does not have an education set");
        }
        Education newEducation = Education.copy(education);
        associatedResume.setEducation(newEducation);
        return resumeRepository.save(associatedResume);
    }


    public Resume prefillExperiencesList(UUID resumeID, User authenticatedUser) {

        Resume associatedResume = getUtility.findByUserResumeId(authenticatedUser, resumeID);
        UserProfile profile = profileRepository.findByUserWithExperiences(authenticatedUser);
        List<Experience> experienceList = profile.getExperienceList();
        if (experienceList == null) {
            throw new PrefillException("Your profile does not have experiences set");
        }
        List<Experience> newExperiences = experienceList.
                stream().map(Experience::copy).peek(experience -> {
                    experience.setResume(associatedResume);
                }).
                toList();
        associatedResume.setExperiences(newExperiences);
        return resumeRepository.save(associatedResume);
    }

    public Resume prefillProjectsList(UUID resumeID, User authenticatedUser) {

        Resume associatedResume = getUtility.findByUserResumeId(authenticatedUser, resumeID);
        UserProfile profile = profileRepository.findByUserWithProjects(authenticatedUser);
        List<Project> projectList = profile.getProjectList();
        if (projectList == null) {
            throw new PrefillException("Your profile does not have projects set");
        }
        List<Project> newProjects = projectList.
                stream().map(Project::copy).peek(project -> {
                    project.setResume(associatedResume);
                }).
                toList();
        associatedResume.setProjects(newProjects);
        return resumeRepository.save(associatedResume);
    }


    private List<Experience> extractExperiences(List<ExperienceForm> experienceForms, Resume associatedResume){
        return experienceForms.stream().map( rawForm -> {
                    Experience newExperience = new Experience(rawForm.companyName(),
                            rawForm.technologies(), rawForm.location(), rawForm.experienceType(),
                            YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                            YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                            rawForm.bullets());
                    newExperience.setResume(associatedResume);
                    return newExperience;
                }
            ).toList();

    }


    private List<Project> extractProjects(List<ProjectForm> projectForms, Resume resume){
        return projectForms.stream().map(rawForm -> {
                    Project newProject = new Project(rawForm.projectName(), rawForm.technologyList(),
                            YearMonthStringOperations.getYearMonth(rawForm.startDate()),
                            YearMonthStringOperations.getYearMonth(rawForm.endDate()),
                            rawForm.bullets());
                    newProject.setResume(resume);
                    return newProject;
                }
        ).toList();
    }

}
