package com.rebuild.backend.service;


import com.rebuild.backend.exceptions.resume_exceptions.MaxResumesReachedException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeCompanyConstraintException;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.repository.ResumeRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public Header changeHeaderInfo(UUID resID, String newName, String newEmail, PhoneNumber newPhoneNumber){
        Resume resume = findById(resID);
        Header newHeader = new Header(newPhoneNumber, newName, newEmail);
        resume.setHeader(newHeader);
        resumeRepository.save(resume);
        return newHeader;
    }

    public Resume createNewResumeFor(User user){
        if(user.maxResumeLimitReached()){
            throw new MaxResumesReachedException("You have reached the maximum number of resumes you can have" +
                    "as a free user. In order to create more resumes, please upgrade to the paid version");
        }
        else{
            Resume newResume = new Resume(user);
            user.getResumes().add(newResume);
            return resumeRepository.save(newResume);
        }

    }

    //TODO: Throw a proper exception here and handle it properly
    public Resume findById(UUID id){
        return resumeRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public Experience changeExperienceInfo(UUID resID, UUID expID,
                                           String newCompanyName,
                                           List<String> newTechnologies,
                                           Duration newDuration,
                                           List<String> newBullets){
        try {

            Resume resume = findById(resID);
            Experience newExperience = new Experience(newCompanyName, newTechnologies, newDuration, newBullets);
            List<Experience> experiences = resume.getExperiences();
            int indexCounter = 0;
            for(Experience experience : experiences){
                if(experience.getId().equals(expID)){
                    experiences.remove(indexCounter);
                    experiences.add(indexCounter, newExperience);
                    break;
                }
                else{
                    indexCounter += 1;
                }
            }

            resumeRepository.save(resume);
            return newExperience;
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException
                    && violationException.getConstraintName().equals("uk_resume_company")){
                throw new ResumeCompanyConstraintException("A resume can't have more than 1 experience with a company");
            }
            throw e;
        }
    }

    public Education changeEducationInfo(UUID resID, String newSchoolName, List<String> newCourseWork){
        Resume resume = findById(resID);
        Education education = new Education(newSchoolName, newCourseWork);
        resume.setEducation(education);
        resumeRepository.save(resume);
        return education;
    }

    public Header createNewHeader(UUID resID, String name, String email, PhoneNumber phoneNumber){
        Resume resume = findById(resID);
        Header newHeader = new Header(phoneNumber, name, email);
        resume.setHeader(newHeader);
        resumeRepository.save(resume);
        return newHeader;

    }

    public Experience createNewExperience(UUID resID, String companyName,
                                          List<String> technologies,
                                          Duration duration, List<String> bullets){
        try {
            Resume resume = findById(resID);
            Experience newExperience = new Experience(companyName, technologies, duration, bullets);
            resume.addExperience(newExperience);
            resumeRepository.save(resume);
            return newExperience;
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException
                    && violationException.getConstraintName().equals("uk_resume_company")){
                throw new ResumeCompanyConstraintException("A resume can't have more than 1 experience with a company");
            }
            throw e;
        }
    }

    public Education createNewEducation(UUID resID, String schoolName, List<String> courseWork){
        Resume resume = findById(resID);
        Education education = new Education(schoolName, courseWork);
        resume.setEducation(education);
        resumeRepository.save(resume);
        return education;
    }

    public Resume save(Resume resume){
        return resumeRepository.save(resume);
    }

    public void deleteById(UUID id){
        resumeRepository.deleteById(id);
    }

    public void deleteEducation(UUID resID){
        Resume resume = findById(resID);
        resume.setEducation(null);
        resumeRepository.save(resume);
    }

    public void deleteExperience(UUID resID, UUID expID){
        Resume resume = findById(resID);
        resume.getExperiences().removeIf(experience -> experience.getId().equals(expID));
        resumeRepository.save(resume);
    }

    public void deleteHeader(UUID resID){
        Resume resume = findById(resID);
        resume.setHeader(null);
        resumeRepository.save(resume);
    }

    public Resume setExperiences(UUID resID, List<Experience> newExperiences){
        Resume resume = findById(resID);
        resume.setExperiences(newExperiences);
        return resumeRepository.save(resume);
    }
}
