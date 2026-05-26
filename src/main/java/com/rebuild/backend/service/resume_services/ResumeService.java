package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.model.forms.resume_forms.*;

import com.rebuild.backend.model.responses.resume_responses.*;
import com.rebuild.backend.repository.resume_repositories.ExperienceRepository;
import com.rebuild.backend.repository.resume_repositories.ProjectRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.service.util_services.SubpartsModificationService;
import com.rebuild.backend.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private final SubpartsModificationService modificationUtility;

    private final ResumeObtainer getUtility;

    private final ExperienceRepository experienceRepository;

    private final ProjectRepository projectRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         SubpartsModificationService modificationUtility,
                         ResumeObtainer getUtility, ExperienceRepository experienceRepository,
                         ProjectRepository projectRepository) {
        this.resumeRepository = resumeRepository;
        this.modificationUtility = modificationUtility;
        this.getUtility = getUtility;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public HeaderResponse changeHeaderInfo(HeaderForm headerForm, UUID resumeID, User user){
        return modificationUtility.modifyResumeHeader(headerForm, resumeID, user);
    }

    @Transactional
    public ResumeResponse createNewResumeFor(String resume_name, User user){
        Optional<Resume> foundResume = resumeRepository.findByUserAndName(user, resume_name);
        if (foundResume.isPresent())
        {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a resume with this name");
        }

        Resume newResume = new Resume(resume_name, user);
        user.getResumes().add(newResume);
        return resumeRepository.save(newResume).toResponse();

    }

    public ResumeResponse findByUserAndResumeId(User user, UUID resumeID){
        Resume foundResume = getUtility.findByUserResumeId(user, resumeID);
        return foundResume.toResponse();
    }

    @Transactional
    public ExperienceResponse changeExperienceInfo(ExperienceForm experienceForm, UUID experienceID, UUID resumeId,
                                                   User user){
        return modificationUtility.modifyResumeExperience(experienceForm, experienceID, resumeId, user);

    }

    public ProjectResponse changeProjectInfo(ProjectForm projectForm, UUID projectID, UUID resumeID, User user){
        return modificationUtility.modifyResumeProject(projectForm, projectID, resumeID, user);
    }

    @Transactional
    public EducationResponse changeEducationInfo(EducationForm educationForm,
                                                 UUID resumeID, User user){
       return modificationUtility.modifyResumeEducation(educationForm, resumeID, user);
    }

    @Transactional
    public ExperienceResponse createNewExperience(User changingUser, UUID resumeId,
                                      ExperienceForm experienceForm){
        Resume resume = getUtility.findByUserAndIdWithExperiences(changingUser, resumeId);
        YearMonth start = StringUtil.generateYearMonthValue(experienceForm.startDate());
        YearMonth end = StringUtil.generateYearMonthValue(experienceForm.endDate());
        ResumeExperience newResumeExperience = new ResumeExperience(experienceForm.companyName(),
                experienceForm.technologies(), experienceForm.location(), experienceForm.experienceType(),
                start, end, experienceForm.bullets());
        newResumeExperience.setResume(resume);

        resume.getResumeExperiences().add(newResumeExperience);

        ResumeExperience savedResumeExperience = experienceRepository.save(newResumeExperience);

        return savedResumeExperience.toResponse();

    }

    public ProjectResponse createNewProject(User changingUser, UUID resumeId, ProjectForm projectForm){

        Resume resume = getUtility.findByUserAndIdWithProjects(changingUser, resumeId);
        YearMonth start = StringUtil.generateYearMonthValue(projectForm.startDate());
        YearMonth end = StringUtil.generateYearMonthValue(projectForm.endDate());
        ResumeProject newResumeProject = new ResumeProject(projectForm.projectName(), projectForm.technologyList(),
                start, end, projectForm.bullets());
        newResumeProject.setResume(resume);
        resume.getResumeProjects().add(newResumeProject);

        ResumeProject savedResumeProject = projectRepository.save(newResumeProject);

        return savedResumeProject.toResponse();

    }

    @Transactional
    public void deleteById(User deletingUser, UUID id){
        Resume resume = getUtility.findByUserResumeId(deletingUser, id);
        resumeRepository.delete(resume);
    }

    @Transactional
    public ResumeResponse deleteEducation(User changingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        resume.setResumeEducation(null);
        return resumeRepository.save(resume).toResponse();
    }

    @Transactional
    public ResumeResponse deleteExperience(User changingUser, UUID resumeId, UUID experienceId){
        Resume resume = getUtility.findByUserAndIdWithExperiences(changingUser, resumeId);
        resume.getResumeExperiences().removeIf(experience -> experience.getId().equals(experienceId));
        return resumeRepository.save(resume).toResponse();
    }


    @Transactional
    public ResumeResponse deleteProject(User changingUser, UUID resumeId, UUID projectId){
        Resume resume = getUtility.findByUserAndIdWithProjects(changingUser, resumeId);
        resume.getResumeProjects().removeIf(project -> project.getId().equals(projectId));
        return resumeRepository.save(resume).toResponse();
    }


    @Transactional
    public ResumeResponse deleteHeader(User changingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        resume.setResumeHeader(null);
        return resumeRepository.save(resume).toResponse();
    }

    @Transactional
    public ResumeResponse fullUpdate(User updatingUser, UUID resumeID,
                             FullInformationForm resumeForm) {
        Resume resume = getUtility.findByUserResumeId(updatingUser, resumeID);


        //We can't modify the resume's fields directly here, as that would also modify the variables that
        // we declared outside the try block, causing a bug.
        ResumeHeader newResumeHeader = new ResumeHeader(resumeForm.headerForm().number(),
                resumeForm.headerForm().name(), resumeForm.headerForm().email(),
                resumeForm.headerForm().links());
        resume.setResumeHeader(newResumeHeader);

        ResumeEducation newResumeEducation = new ResumeEducation(resumeForm.educationForm().schoolName(),
                resumeForm.educationForm().relevantCoursework(),
                resumeForm.educationForm().location(),
                StringUtil.generateYearMonthValue(resumeForm.educationForm().startDate()),
                StringUtil.generateYearMonthValue(resumeForm.educationForm().endDate()));
        resume.setResumeEducation(newResumeEducation);

        resume.setResumeProjects(extractProjects(resumeForm.projects(), resume));

        resume.setResumeExperiences(extractExperiences(resumeForm.experiences(), resume));

        return resumeRepository.save(resume).toResponse();

    }

    @Transactional
    public Resume changeName(User changingUser, UUID resumeId, String newName){
        Optional<Resume> foundResume = resumeRepository.findByUserAndName(changingUser, newName);
        if (foundResume.isPresent())
        {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a resume with this name");
        }
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);
        changingResume.setName(newName);
        return resumeRepository.save(changingResume);

    }

    @Transactional
    public Resume copyResume(User user, UUID resumeId, String name){

        Optional<Resume> foundResume = resumeRepository.findByUserAndName(user, name);
        if (foundResume.isPresent())
        {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a resume with this name");
        }

        Resume copiedResume = getUtility.findByUserAndIdWithAllInfo(user, resumeId);

        Resume newResume = new Resume(copiedResume, name);
        return resumeRepository.save(newResume);

    }

    private List<ResumeExperience> extractExperiences(List<ExperienceForm> experienceForms, Resume associatedResume){
        return experienceForms.stream().map( rawForm -> {
                    ResumeExperience newResumeExperience = new ResumeExperience(rawForm.companyName(),
                            rawForm.technologies(), rawForm.location(), rawForm.experienceType(),
                            StringUtil.generateYearMonthValue(rawForm.startDate()),
                            StringUtil.generateYearMonthValue(rawForm.endDate()),
                            rawForm.bullets());
                    newResumeExperience.setResume(associatedResume);
                    return newResumeExperience;
                }
            ).toList();

    }


    private List<ResumeProject> extractProjects(List<ProjectForm> projectForms, Resume resume){
        return projectForms.stream().map(rawForm -> {
                    ResumeProject newResumeProject = new ResumeProject(rawForm.projectName(), rawForm.technologyList(),
                            StringUtil.generateYearMonthValue(rawForm.startDate()),
                            StringUtil.generateYearMonthValue(rawForm.endDate()),
                            rawForm.bullets());
                    newResumeProject.setResume(resume);
                    return newResumeProject;
                }
        ).toList();
    }

}
