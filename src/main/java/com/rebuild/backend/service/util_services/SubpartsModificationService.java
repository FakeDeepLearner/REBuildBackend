package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.BelongingException;
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

    private final ResumeRepository resumeRepository;

    private final ExperienceRepository experienceRepository;

    private final ProjectRepository projectRepository;

    private final HeaderRepository headerRepository;

    private final EducationRepository educationRepository;

    @Autowired
    public SubpartsModificationService(ResumeObtainer getUtility, ResumeRepository resumeRepository,
                                       ExperienceRepository experienceRepository,
                                       ProjectRepository projectRepository,
                                       HeaderRepository headerRepository, EducationRepository educationRepository) {
        this.getUtility = getUtility;
        this.resumeRepository = resumeRepository;
        this.experienceRepository = experienceRepository;
        this.projectRepository = projectRepository;
        this.headerRepository = headerRepository;
        this.educationRepository = educationRepository;
    }

    @Transactional
    public HeaderResponse modifyResumeHeader(HeaderForm headerForm,
                                             UUID resumeId, User changingUser) {
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);

        Header newHeader = new Header(headerForm.number(), headerForm.name(),
                headerForm.email(), headerForm.links());

        changingResume.setHeader(newHeader);
        newHeader.setResume(changingResume);
        Header savedHeader = headerRepository.save(newHeader);
        return savedHeader.toResponse();
    }

    @Transactional
    public ExperienceResponse modifyResumeExperience(ExperienceForm experienceForm, UUID experienceId,
                                                     UUID resumeId, User changingUser) {
        Optional<Experience> changingExperience = experienceRepository.findByIdAndResume_IdAndResume_User(experienceId,
                resumeId, changingUser);

        if (changingExperience.isEmpty()) {
            throw new BelongingException("Experience with this id either does " +
                    "not exist or does not belong to this resume");
        }

        Experience experience = changingExperience.get();

        modifyExperience(experience, experienceForm);
        Experience savedExperience = experienceRepository.save(experience);
        return savedExperience.toResponse();
    }

    @Transactional
    public ProjectResponse modifyResumeProject(ProjectForm projectForm, UUID projectId,
                                               UUID resumeId, User changingUser) {

        Optional<Project> changingProject = projectRepository.findByIdAndResume_IdAndResume_User(
                projectId, resumeId, changingUser
        );

        if (changingProject.isEmpty()) {
            throw new BelongingException("Project with this id either does not " +
                    "exist or does not belong to this resume");
        }

        Project project = changingProject.get();
        modifyProject(project, projectForm);
        Project savedProject = projectRepository.save(project);
        return savedProject.toResponse();
    }

    @Transactional
    public EducationResponse modifyResumeEducation(EducationForm educationForm,
                                                   UUID resumeId, User changingUser) {
        Resume changingResume = getUtility.findByUserResumeId(changingUser, resumeId);

        Education newEducation = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(), StringUtil.getYearMonth(educationForm.startDate()),
                StringUtil.getYearMonth(educationForm.endDate()));
        changingResume.setEducation(newEducation);
        newEducation.setResume(changingResume);

        Education savedEducation =  educationRepository.save(newEducation);

        return savedEducation.toResponse();
    }


    private void modifyExperience(Experience changingExperience, ExperienceForm experienceForm) {
        YearMonth start = StringUtil.getYearMonth(experienceForm.startDate());
        YearMonth end = StringUtil.getYearMonth(experienceForm.endDate());
        changingExperience.setLocation(experienceForm.location());
        changingExperience.setEndDate(end);
        changingExperience.setStartDate(start);
        changingExperience.setBullets(experienceForm.bullets());
        changingExperience.setTechnologyList(experienceForm.technologies());
        changingExperience.setCompanyName(experienceForm.companyName());
        changingExperience.setExperienceType(experienceForm.experienceType());
    }


    private void modifyProject(Project changingProject, ProjectForm projectForm) {
        YearMonth start = StringUtil.getYearMonth(projectForm.startDate());
        YearMonth end = StringUtil.getYearMonth(projectForm.endDate());
        changingProject.setStartDate(start);
        changingProject.setEndDate(end);

        changingProject.setBullets(projectForm.bullets());
        changingProject.setProjectName(projectForm.projectName());
        changingProject.setTechnologyList(projectForm.technologyList());
    }

}
