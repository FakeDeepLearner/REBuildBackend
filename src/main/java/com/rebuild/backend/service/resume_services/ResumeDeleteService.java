package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.resume_responses.ResumeResponse;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ResumeDeleteService {
    private final ResumeObtainer getUtility;

    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumeDeleteService(ResumeObtainer getUtility, ResumeRepository resumeRepository) {
        this.getUtility = getUtility;
        this.resumeRepository = resumeRepository;
    }

    
    public void deleteById(User deletingUser, UUID id){
        Resume resume = getUtility.findByUserResumeId(deletingUser, id);
        resumeRepository.delete(resume);
    }

    
    public ResumeResponse deleteEducation(User changingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        resume.setResumeEducation(null);
        return resumeRepository.save(resume).toResponse();
    }

    
    public ResumeResponse deleteExperience(User changingUser, UUID resumeId, UUID experienceId){
        Resume resume = getUtility.findByUserAndIdWithExperiences(changingUser, resumeId);
        resume.getResumeExperiences().removeIf(experience -> experience.getId().equals(experienceId));
        return resumeRepository.save(resume).toResponse();
    }


    
    public ResumeResponse deleteProject(User changingUser, UUID resumeId, UUID projectId){
        Resume resume = getUtility.findByUserAndIdWithProjects(changingUser, resumeId);
        resume.getResumeProjects().removeIf(project -> project.getId().equals(projectId));
        return resumeRepository.save(resume).toResponse();
    }


    
    public ResumeResponse deleteHeader(User changingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(changingUser, resumeId);
        resume.setResumeHeader(null);
        return resumeRepository.save(resume).toResponse();
    }

    
    public ResumeResponse deleteAllExperiences(User deletingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(deletingUser, resumeId);
        resume.setResumeExperiences(null);
        return resumeRepository.save(resume).toResponse();
    }

    
    public ResumeResponse deleteAllProjects(User deletingUser, UUID resumeId){
        Resume resume = getUtility.findByUserResumeId(deletingUser, resumeId);
        resume.setResumeProjects(null);
        return resumeRepository.save(resume).toResponse();
    }
}
