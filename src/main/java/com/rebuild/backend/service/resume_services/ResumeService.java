package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.exceptions.conflict_exceptions.DuplicateResumeNameException;
import com.rebuild.backend.exceptions.resume_exceptions.MaxResumesReachedException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeCompanyConstraintException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeSectionConstraintException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.resume_forms.FullResumeForm;
import com.rebuild.backend.repository.ResumeRepository;

import com.rebuild.backend.repository.ResumeVersionRepository;
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

    private final ResumeVersionRepository versionRepository;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository, ResumeVersionRepository versionRepository) {
        this.resumeRepository = resumeRepository;
        this.versionRepository = versionRepository;
    }

    public Resume changeHeaderInfo(UUID resID, String newName, String newEmail, PhoneNumber newPhoneNumber){
        Resume resume = findById(resID);
        resume.getHeader().setEmail(newEmail);
        resume.getHeader().setNumber(newPhoneNumber);
        resume.getHeader().setName(newName);
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
                        Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                    throw new DuplicateResumeNameException("You already have a resume with this name");
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
        return resumeRepository.save(resume);
    }

    public Header createNewHeader(UUID resID, String name, String email, PhoneNumber phoneNumber){
        Resume resume = findById(resID);
        Header newHeader = new Header(phoneNumber, name, email);
        resume.setHeader(newHeader);
        newHeader.setResume(resume);
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
            resumeRepository.save(resume);
            return newExperience;
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException
                    && Objects.equals(violationException.getConstraintName(), "uk_resume_company")){
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
        resumeRepository.save(resume);
        return education;
    }

    public ResumeSection createNewSection(UUID resID, String sectionTitle, List<String> sectionBullets){
        Resume resume = findById(resID);
        try {
            ResumeSection newSection = new ResumeSection(sectionTitle, sectionBullets);
            resume.addSection(newSection);
            newSection.setResume(resume);
            resumeRepository.save(resume);
            return newSection;
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException
                    && Objects.equals(violationException.getConstraintName(), "uk_resume_company")){
                throw new ResumeSectionConstraintException("A resume can't have more than 1 section with the same title");
            }
            throw e;
        }
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

    public void deleteSection(UUID resID, UUID sectionID){
        Resume resume = findById(resID);
        resume.getSections().removeIf(section -> section.getId().equals(sectionID));
        resumeRepository.save(resume);
    }
    

    public void deleteHeader(UUID resID){
        Resume resume = findById(resID);
        resume.setHeader(null);
        resumeRepository.save(resume);
    }

    public Resume setExperiences(Resume resume, List<Experience> newExperiences){
        try {
            resume.setExperiences(newExperiences);
            return resumeRepository.save(resume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_resume_section")){
                throw new ResumeSectionConstraintException("The new sections can't have more than 1 section with the same title");
            }
            throw e;
        }
    }

    public Resume setHeader(Resume resume, Header newHeader){
        resume.setHeader(newHeader);
        return resumeRepository.save(resume);
    }

    public Resume setEducation(Resume resume, Education newEducation){
        resume.setEducation(newEducation);
        return resumeRepository.save(resume);
    }

    public Resume setSections(Resume resume, List<ResumeSection> newSections){
        try {
            resume.setSections(newSections);
            return resumeRepository.save(resume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_resume_section")){
                throw new ResumeSectionConstraintException("The new sections can't have more than 1 section with the same title");
            }
            throw e;
        }
    }

    public Resume fullUpdate(Resume resume, FullResumeForm resumeForm){
        try {
            resume.getHeader().setName(resumeForm.name());
            resume.getHeader().setEmail(resumeForm.email());
            resume.getHeader().setNumber(resumeForm.phoneNumber());
            resume.getEducation().setSchoolName(resumeForm.schoolName());
            resume.getEducation().setRelevantCoursework(resumeForm.relevantCoursework());
            resume.setExperiences(resumeForm.experiences());
            resume.setSections(resumeForm.sections());
            return resumeRepository.save(resume);
        }
        catch(DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                switch (violationException.getConstraintName()){
                    case "uk_resume_section": throw new ResumeSectionConstraintException("The new sections can't have more than 1 section with the same title");
                    case "uk_resume_company": throw new ResumeCompanyConstraintException("The experiences can't have more than experience with the same company");
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return resume;
    }

    public Resume changeName(UUID resID, String newName){
        Resume changingResume = findById(resID);
        try{
            changingResume.setName(newName);
            return resumeRepository.save(changingResume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if(cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                throw new DuplicateResumeNameException("You already have a resume with this name");
            }
            //Should never get here
        }
        return changingResume;
    }

    public Resume copyResume(UUID resID, String newName){
        Resume copiedResume = findById(resID);
        try {
            Resume newResume = new Resume(copiedResume, newName);
            return resumeRepository.save(newResume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if(cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                throw new DuplicateResumeNameException("The copied resume must have a " +
                        "different name than the original one");
            }
            //Should never get here
        }
        return copiedResume;

    }

    public ResumeVersion snapshotCurrentData(UUID resume_id){
        Resume copiedResume = findById(resume_id);
        ResumeVersion newVersion = new ResumeVersion(copiedResume.getHeader(), copiedResume.getEducation(),
                copiedResume.getExperiences(), copiedResume.getSections());
        copiedResume.getSavedVersions().add(newVersion);
        newVersion.setAssociatedResume(copiedResume);
        return versionRepository.save(newVersion);
    }

    public Resume switchToAnotherVersion(UUID resume_id, UUID version_id){
        Resume switchingResume = findById(resume_id);
        //This avoids looking up the database again, saving time in most cases
        //Also, we need to (in the future) verify that this version does actually belong to this resume.
        ResumeVersion versionToSwitch = switchingResume.getSavedVersions().
                stream().filter(version -> version.getId().equals(version_id)).findFirst().orElse(null);
        assert versionToSwitch != null;
        try {
            switchingResume.setHeader(versionToSwitch.getVersionedHeader());
            switchingResume.setEducation(versionToSwitch.getVersionedEducation());
            switchingResume.setExperiences(versionToSwitch.getVersionedExperiences());
            switchingResume.setSections(versionToSwitch.getVersionedSections());
        }
        catch(DataIntegrityViolationException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException) {
                switch (violationException.getConstraintName()) {
                    case "uk_resume_section":
                        throw new ResumeSectionConstraintException("The sections from this version can't have more than 1 section with the same title");
                    case "uk_resume_company":
                        throw new ResumeCompanyConstraintException("The experiences from this version can't have more than experience with the same company");
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return switchingResume;
    }

    public void deleteVersion(UUID version_id){
        versionRepository.deleteById(version_id);
    }

}
