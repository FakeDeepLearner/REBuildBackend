package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.exceptions.conflict_exceptions.DuplicateNameException;
import com.rebuild.backend.exceptions.resume_exceptions.MaxResumesReachedException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeCompanyConstraintException;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.resume_forms.FullResumeForm;
import com.rebuild.backend.repository.ResumeRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public Resume changeHeaderInfo(UUID resID, String newName, String newEmail, PhoneNumber newPhoneNumber){
        Resume resume = findById(resID);
        resume.getHeader().setEmail(newEmail);
        resume.getHeader().setNumber(newPhoneNumber);
        resume.getHeader().setName(newName);
        resume.setLastModifiedTime(LocalDateTime.now());
        return resumeRepository.save(resume);
    }

    public Resume createNewResumeFor(String resume_name, User user){
        if(user.maxResumeLimitReached()){
            throw new MaxResumesReachedException("You have reached the maximum number of resumes you can have" +
                    "as a free user. In order to create more resumes, please upgrade to the paid version");
        }
        else{
            try {
                Resume newResume = new Resume(resume_name, user);
                user.getResumes().add(newResume);
                return resumeRepository.save(newResume);
            }
            catch (DataIntegrityViolationException e){
                Throwable cause = e.getCause();
                if(cause instanceof ConstraintViolationException violationException &&
                violationException.getConstraintName().equals("uk_same_user_resume_name")){
                    throw new DuplicateNameException("You already have a resume with this name");
                }
            }

        }
        // Should never get there
        return null;
    }

    public boolean resumeBelongsToUser(UUID resumeID, UUID userID){
        return resumeRepository.countByIdAndUserId(resumeID, userID) > 0;
    }

    //TODO: Throw a proper exception here and handle it properly
    public Resume findById(UUID id){
        return resumeRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public Resume changeExperienceInfo(UUID resID, UUID expID,
                                           String newCompanyName,
                                           List<String> newTechnologies,
                                           Duration newDuration,
                                           List<String> newBullets){
        Resume resume = findById(resID);
        try {
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
            resume.setLastModifiedTime(LocalDateTime.now());
            return resumeRepository.save(resume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException
                    && Objects.equals(violationException.getConstraintName(), "uk_resume_company")){
                throw new ResumeCompanyConstraintException("A resume can't have more than 1 experience with a company");
            }
        }
        return resume;
    }

    public Resume changeEducationInfo(UUID resID, String newSchoolName, List<String> newCourseWork){
        Resume resume = findById(resID);
        resume.getEducation().setRelevantCoursework(newCourseWork);
        resume.getEducation().setSchoolName(newSchoolName);
        resume.setLastModifiedTime(LocalDateTime.now());
        return resumeRepository.save(resume);
    }

    public Header createNewHeader(UUID resID, String name, String email, PhoneNumber phoneNumber){
        Resume resume = findById(resID);
        Header newHeader = new Header(phoneNumber, name, email);
        resume.setHeader(newHeader);
        newHeader.setResume(resume);
        resume.setLastModifiedTime(LocalDateTime.now());
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
            newExperience.setResume(resume);
            resume.setLastModifiedTime(LocalDateTime.now());
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
        education.setResume(resume);
        resume.setLastModifiedTime(LocalDateTime.now());
        resumeRepository.save(resume);
        return education;
    }

    public ResumeSection createNewSection(UUID resID, String sectionTitle, List<String> sectionBullets){
        Resume resume = findById(resID);
        ResumeSection newSection = new ResumeSection(sectionTitle, sectionBullets);
        resume.addSection(newSection);
        newSection.setResume(resume);
        resume.setLastModifiedTime(LocalDateTime.now());
        resumeRepository.save(resume);
        return newSection;
    }

    public void deleteById(UUID id){
        resumeRepository.deleteById(id);
    }

    public void deleteEducation(UUID resID){
        Resume resume = findById(resID);
        resume.setEducation(null);
        resume.setLastModifiedTime(LocalDateTime.now());
        resumeRepository.save(resume);
    }

    public void deleteExperience(UUID resID, UUID expID){
        Resume resume = findById(resID);
        resume.getExperiences().removeIf(experience -> experience.getId().equals(expID));
        resume.setLastModifiedTime(LocalDateTime.now());
        resumeRepository.save(resume);
    }

    public void deleteSection(UUID resID, UUID sectionID){
        Resume resume = findById(resID);
        resume.getSections().removeIf(section -> section.getId().equals(sectionID));
        resume.setLastModifiedTime(LocalDateTime.now());
        resumeRepository.save(resume);
    }
    

    public void deleteHeader(UUID resID){
        Resume resume = findById(resID);
        resume.setHeader(null);
        resume.setLastModifiedTime(LocalDateTime.now());
        resumeRepository.save(resume);
    }

    public Resume setExperiences(UUID resID, List<Experience> newExperiences){
        Resume resume = findById(resID);
        resume.setExperiences(newExperiences);
        resume.setLastModifiedTime(LocalDateTime.now());
        return resumeRepository.save(resume);
    }

    public Resume setHeader(UUID resID, Header newHeader){
        Resume resume = findById(resID);
        resume.setHeader(newHeader);
        resume.setLastModifiedTime(LocalDateTime.now());
        return resumeRepository.save(resume);
    }

    public Resume setEducation(UUID resID, Education newEducation){
        Resume resume = findById(resID);
        resume.setEducation(newEducation);
        resume.setLastModifiedTime(LocalDateTime.now());
        return resumeRepository.save(resume);
    }

    public Resume fullUpdate(UUID resID, FullResumeForm resumeForm){
        Resume resume = findById(resID);
        resume.getHeader().setName(resumeForm.name());
        resume.getHeader().setEmail(resumeForm.email());
        resume.getHeader().setNumber(resumeForm.phoneNumber());
        resume.getEducation().setSchoolName(resumeForm.schoolName());
        resume.getEducation().setRelevantCoursework(resumeForm.relevantCoursework());
        resume.setExperiences(resumeForm.experiences());
        resume.setSections(resumeForm.sections());
        resume.setLastModifiedTime(LocalDateTime.now());
        return resumeRepository.save(resume);
    }

    public Resume changeName(UUID resID, String newName){
        Resume changingResume = findById(resID);
        try{
            changingResume.setName(newName);
            changingResume.setLastModifiedTime(LocalDateTime.now());
            return resumeRepository.save(changingResume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if(cause instanceof ConstraintViolationException violationException &&
                    violationException.getConstraintName().equals("uk_same_user_resume_name")){
                throw new DuplicateNameException("You already have a resume with this name");
            }
            //Should never get here
        }
        return changingResume;
    }

    public Resume copyResume(UUID resID){
        Resume copiedResume = findById(resID);
        Resume newResume = new Resume(copiedResume);
        return resumeRepository.save(newResume);

    }



}
