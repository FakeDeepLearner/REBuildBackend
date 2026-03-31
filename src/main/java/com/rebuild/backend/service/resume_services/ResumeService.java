package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.resume_forms.*;

import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.service.util_services.SubpartsModificationService;
import com.rebuild.backend.utils.database_utils.YearMonthStringOperations;
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

    private final SubpartsModificationService modificationUtility;

    private final ResumeObtainer getUtility;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         SubpartsModificationService modificationUtility,
                         ResumeObtainer getUtility) {
        this.resumeRepository = resumeRepository;
        this.modificationUtility = modificationUtility;
        this.getUtility = getUtility;
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
    public Experience changeExperienceInfo(ExperienceForm experienceForm, UUID experienceID, UUID resumeId,
                                           User user){
        return modificationUtility.modifyResumeExperience(experienceForm, experienceID, resumeId, user);

    }

    public Project changeProjectInfo(ProjectForm projectForm, UUID projectID, UUID resumeID, User user){
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
    public Resume copyResume(User user, UUID resumeId, String name){
        Resume copiedResume = getUtility.findByUserAndIdWithAllInfo(user, resumeId);
        if(name.equals(copiedResume.getName())){
            throw new RuntimeException("The new resume must have a different name than the original one.");
        }
        Resume newResume = new Resume(copiedResume, name);
        return resumeRepository.save(newResume);

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
