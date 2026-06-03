package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeExperience;
import com.rebuild.backend.model.entities.resume_entities.ResumeProject;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.ProjectForm;
import com.rebuild.backend.model.responses.resume_responses.ResumeResponse;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.UUID;

@Service
public class ResumePostService {

    private final ResumeObtainer getUtility;

    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumePostService(ResumeObtainer getUtility, ResumeRepository resumeRepository) {
        this.getUtility = getUtility;
        this.resumeRepository = resumeRepository;
    }

    @Transactional
    public ResumeResponse createNewExperience(User changingUser, UUID resumeId,
                                              ExperienceForm experienceForm){
        Resume resume = getUtility.findByUserAndIdWithExperiences(changingUser, resumeId);
        YearMonth start = StringUtil.generateStartDate(experienceForm.startDate());
        YearMonth end = StringUtil.generateEndDate(experienceForm.endDate());
        ResumeExperience newResumeExperience = new ResumeExperience(experienceForm.companyName(),
                experienceForm.technologies(), experienceForm.location(), experienceForm.experienceType(),
                start, end, experienceForm.bullets());
        newResumeExperience.setResume(resume);

        resume.getResumeExperiences().add(newResumeExperience);

        Resume savedResume = resumeRepository.save(resume);

        return savedResume.toResponse();
    }

    @Transactional
    public ResumeResponse createNewProject(User changingUser, UUID resumeId, ProjectForm projectForm){

        Resume resume = getUtility.findByUserAndIdWithProjects(changingUser, resumeId);
        YearMonth start = StringUtil.generateStartDate(projectForm.startDate());
        YearMonth end = StringUtil.generateEndDate(projectForm.endDate());
        ResumeProject newResumeProject = new ResumeProject(projectForm.projectName(), projectForm.technologyList(),
                start, end, projectForm.bullets());
        newResumeProject.setResume(resume);
        resume.getResumeProjects().add(newResumeProject);

        Resume savedResume = resumeRepository.save(resume);

        return savedResume.toResponse();

    }
}
