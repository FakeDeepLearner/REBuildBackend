package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.utils.BulletsUtil;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.model.forms.resume_forms.ProjectForm;
import com.rebuild.backend.model.responses.resume_responses.EducationResponse;
import com.rebuild.backend.model.responses.resume_responses.ExperienceResponse;
import com.rebuild.backend.model.responses.resume_responses.HeaderResponse;
import com.rebuild.backend.model.responses.resume_responses.ProjectResponse;
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

    private final HeaderRepository headerRepository;

    private final EducationRepository educationRepository;

    @Autowired
    public SubpartsModificationService(ResumeObtainer getUtility,
                                       ExperienceRepository experienceRepository,
                                       ProjectRepository projectRepository,
                                       HeaderRepository headerRepository,
                                       EducationRepository educationRepository) {
        this.getUtility = getUtility;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.headerRepository = headerRepository;
        this.educationRepository = educationRepository;
    }

    @Transactional
    public HeaderResponse modifyResumeHeader(HeaderForm headerForm,
                                             UUID resumeId, User changingUser) {
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);

        ResumeHeader newResumeHeader = new ResumeHeader(headerForm.number(), headerForm.name(),
                headerForm.email(), headerForm.links());

        changingResume.setResumeHeader(newResumeHeader);
        newResumeHeader.setResume(changingResume);
        ResumeHeader savedResumeHeader = headerRepository.save(newResumeHeader);
        return savedResumeHeader.toResponse();
    }

    @Transactional
    public ExperienceResponse modifyResumeExperience(ExperienceForm experienceForm, UUID experienceId,
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
        return savedResumeExperience.toResponse();
    }

    @Transactional
    public ProjectResponse modifyResumeProject(ProjectForm projectForm, UUID projectId,
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
        return savedResumeProject.toResponse();
    }

    @Transactional
    public EducationResponse modifyResumeEducation(EducationForm educationForm,
                                                   UUID resumeId, User changingUser) {
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);

        ResumeEducation newResumeEducation = new ResumeEducation(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(), StringUtil.generateYearMonthValue(educationForm.startDate()),
                StringUtil.generateYearMonthValue(educationForm.endDate()));
        changingResume.setResumeEducation(newResumeEducation);
        newResumeEducation.setResume(changingResume);

        ResumeEducation savedResumeEducation =  educationRepository.save(newResumeEducation);

        return savedResumeEducation.toResponse();
    }


    private void modifyExperience(ResumeExperience changingResumeExperience, ExperienceForm experienceForm) {
        YearMonth start = StringUtil.generateYearMonthValue(experienceForm.startDate());
        YearMonth end = StringUtil.generateYearMonthValue(experienceForm.endDate());
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
        YearMonth start = StringUtil.generateYearMonthValue(projectForm.startDate());
        YearMonth end = StringUtil.generateYearMonthValue(projectForm.endDate());
        changingResumeProject.setStartDate(start);
        changingResumeProject.setEndDate(end);

        changingResumeProject.setBullets(BulletsUtil.createProjectBullets(projectForm.bullets(), changingResumeProject));
        changingResumeProject.setProjectName(projectForm.projectName());
        changingResumeProject.setTechnologyList(projectForm.technologyList());
    }

}
