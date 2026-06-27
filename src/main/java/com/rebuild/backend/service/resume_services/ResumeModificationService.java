package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.model.forms.resume_forms.ProjectForm;
import com.rebuild.backend.model.responses.resume_responses.ResumeResponse;
import com.rebuild.backend.repository.resume_repositories.ExperienceRepository;
import com.rebuild.backend.repository.resume_repositories.ProjectRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.utils.BulletsUtil;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.exceptions.BelongingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.DocFlavor;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ResumeModificationService {

    private final ExperienceRepository experienceRepository;

    private final ProjectRepository projectRepository;

    private final ResumeRepository resumeRepository;

    private final ResumeObtainer getUtility;

    @Autowired
    public ResumeModificationService(ExperienceRepository experienceRepository,
                                     ProjectRepository projectRepository,
                                     ResumeRepository resumeRepository, ResumeObtainer getUtility) {
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.resumeRepository = resumeRepository;
        this.getUtility = getUtility;
    }

    
    public ResumeResponse changeHeaderInfo(HeaderForm headerForm, UUID resumeID, User user){
        Resume changingResume = getUtility.findByUserResumeId(user, resumeID);

        // If the user has left the relevant field empty, default to their account information
        // (If that is also empty, the value at the Header will be empty)
        String headerNumber = StringUtil.inputOrAlternative(headerForm.number(), user.getPhoneNumber());
        String headerName = StringUtil.inputOrAlternative(headerForm.name(), user.getName());;
        String headerEmail = StringUtil.inputOrAlternative(headerForm.email(), user.getEmail());;

        ResumeHeader newResumeHeader = new ResumeHeader(headerNumber, headerName,
                headerEmail, headerForm.links());

        changingResume.setResumeHeader(newResumeHeader);
        newResumeHeader.setResume(changingResume);
        Resume savedResume  = resumeRepository.save(changingResume);
        return savedResume.toResponse();
    }

    
    public ResumeResponse changeEducationInfo(EducationForm educationForm,
                                              UUID resumeID, User user){
        Resume changingResume = getUtility.findByUserResumeId(user, resumeID);

        ResumeEducation newResumeEducation = new ResumeEducation(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(), StringUtil.generateStartDate(educationForm.startDate()),
                StringUtil.generateEndDate(educationForm.endDate()));
        changingResume.setResumeEducation(newResumeEducation);
        newResumeEducation.setResume(changingResume);

        Resume savedResume = resumeRepository.save(changingResume);

        return savedResume.toResponse();
    }

    
    public ResumeResponse modifyResumeExperience(ExperienceForm experienceForm, UUID experienceId,
                                                 UUID resumeId, User changingUser) {
        Optional<ResumeExperience> changingExperience = experienceRepository.findByIdAndResume_IdAndResume_User(experienceId,
                resumeId, changingUser);

        if (changingExperience.isEmpty()) {
            throw new BelongingException("ResumeExperience with this id either does " +
                    "not exist or does not belong to this resume");
        }

        ResumeExperience resumeExperience = changingExperience.get();

        modifyExperience(resumeExperience, experienceForm);
        ResumeExperience savedResumeExperience = experienceRepository.save(resumeExperience);

        Resume savedResume = savedResumeExperience.getResume();

        return savedResume.toResponse();
    }

    
    public ResumeResponse modifyResumeProject(ProjectForm projectForm, UUID projectId,
                                              UUID resumeId, User changingUser) {

        Optional<ResumeProject> changingProject = projectRepository.findByIdAndResume_IdAndResume_User(
                projectId, resumeId, changingUser
        );

        if (changingProject.isEmpty()) {
            throw new BelongingException("ResumeProject with this id either does not " +
                    "exist or does not belong to this resume");
        }

        ResumeProject resumeProject = changingProject.get();
        modifyProject(resumeProject, projectForm);
        ResumeProject savedResumeProject = projectRepository.save(resumeProject);

        Resume savedResume = savedResumeProject.getResume();
        return savedResume.toResponse();
    }


    private void modifyExperience(ResumeExperience changingResumeExperience, ExperienceForm experienceForm) {
        YearMonth start = StringUtil.generateStartDate(experienceForm.startDate());
        YearMonth end = StringUtil.generateEndDate(experienceForm.endDate());
        changingResumeExperience.setLocation(experienceForm.location());
        changingResumeExperience.setEndDate(end);
        changingResumeExperience.setStartDate(start);
        changingResumeExperience.setBullets(BulletsUtil.createExperienceBullets(experienceForm.bullets(),
                changingResumeExperience));
        changingResumeExperience.setTechnologyList(experienceForm.technologies());
        changingResumeExperience.setCompanyName(experienceForm.companyName());
        changingResumeExperience.setExperienceType(experienceForm.experienceType());
    }


    private void modifyProject(ResumeProject changingResumeProject, ProjectForm projectForm) {
        YearMonth start = StringUtil.generateStartDate(projectForm.startDate());
        YearMonth end = StringUtil.generateEndDate(projectForm.endDate());
        changingResumeProject.setStartDate(start);
        changingResumeProject.setEndDate(end);

        changingResumeProject.setBullets(BulletsUtil.createProjectBullets(projectForm.bullets(), changingResumeProject));
        changingResumeProject.setProjectName(projectForm.projectName());
        changingResumeProject.setTechnologyList(projectForm.technologyList());
    }
}
