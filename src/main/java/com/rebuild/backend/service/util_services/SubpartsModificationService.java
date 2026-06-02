package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.resume_responses.*;
import com.rebuild.backend.utils.BulletsUtil;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.model.forms.resume_forms.ProjectForm;
import com.rebuild.backend.repository.resume_repositories.*;
import com.rebuild.backend.service.resume_services.ResumeObtainer;
import com.rebuild.backend.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubpartsModificationService {

    private final ResumeObtainer getUtility;

    private final ExperienceRepository experienceRepository;

    private final ProjectRepository projectRepository;

    @Autowired
    public SubpartsModificationService(ResumeObtainer getUtility,
                                       ExperienceRepository experienceRepository,
                                       ProjectRepository projectRepository) {
        this.getUtility = getUtility;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
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

    @Transactional
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
