package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.exceptions.conflict_exceptions.DuplicateResumeNameException;
import com.rebuild.backend.exceptions.resume_exceptions.MaxResumesReachedException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeCompanyConstraintException;
import com.rebuild.backend.exceptions.resume_exceptions.ResumeSectionConstraintException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.dtos.error_dtos.OptionalValueAndErrorResult;
import com.rebuild.backend.model.forms.resume_forms.FullResumeForm;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.repository.ResumeRepository;

import com.rebuild.backend.repository.ResumeVersionRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public OptionalValueAndErrorResult<Resume> createNewResumeFor(String resume_name, User user){
        if(user.maxResumeLimitReached()){
            return new OptionalValueAndErrorResult<>(Optional.empty(),
                    Optional.of("You have reached the maximum number of resumes you can create as a free user"))
        }
        else{
            try {
                Resume newResume = new Resume(resume_name, user);
                user.getResumes().add(newResume);
                return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(newResume)),
                        Optional.empty());
            }
            catch (DataIntegrityViolationException e){
                Throwable cause = e.getCause();
                if(cause instanceof ConstraintViolationException violationException &&
                        Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                    return new OptionalValueAndErrorResult<>(Optional.empty(),
                            Optional.of("You already have a resume with this name"));
                }
            }

        }
        // Should never get there
        return new OptionalValueAndErrorResult<>(Optional.empty(), Optional.empty());
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
        //These variables are guaranteed to be properly initialized after the try block executes
        Experience removedExperience = null;
        int removedIndex = 0;
        try {
            Experience newExperience = new Experience(newCompanyName, newTechnologies, newDuration, newBullets);
            List<Experience> experiences = resume.getExperiences();
            int indexCounter = 0;
            for(Experience experience : experiences){
                if(experience.getId().equals(expID)){
                    removedExperience = experiences.remove(indexCounter);
                    removedIndex = indexCounter;
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
            resume.addExperience(removedIndex, removedExperience);
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

    public OptionalValueAndErrorResult<Resume> createNewExperience(UUID resID, String companyName,
                                          List<String> technologies,
                                          Duration duration, List<String> bullets){
        Resume resume = findById(resID);
        Experience newExperience = new Experience(companyName, technologies, duration, bullets);
        try {
            resume.addExperience(newExperience);
            newExperience.setResume(resume);
            return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(resume)), Optional.empty());
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            //Remove the element that was added last (the one added in the try block above)
            resume.getExperiences().removeLast();
            //No loose ends
            newExperience.setResume(null);
            if (cause instanceof ConstraintViolationException violationException
                    && Objects.equals(violationException.getConstraintName(), "uk_resume_company")){
                return new OptionalValueAndErrorResult<>(Optional.empty(),
                        Optional.of("A resume can't have more than 1 experience with a company"));
            }
        }
        return new OptionalValueAndErrorResult<>(Optional.empty(), Optional.empty());
    }

    public Resume createNewEducation(UUID resID, String schoolName, List<String> courseWork){
        Resume resume = findById(resID);
        Education education = new Education(schoolName, courseWork);
        resume.setEducation(education);
        education.setResume(resume);
        return resumeRepository.save(resume);
    }

    public OptionalValueAndErrorResult<Resume> createNewSection(UUID resID, String sectionTitle, List<String> sectionBullets){
        Resume resume = findById(resID);
        ResumeSection newSection = new ResumeSection(sectionTitle, sectionBullets);
        try {
            resume.addSection(newSection);
            newSection.setResume(resume);
            return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(resume)),
                    Optional.empty());
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            resume.getSections().removeLast();
            newSection.setResume(null);
            if (cause instanceof ConstraintViolationException violationException
                    && Objects.equals(violationException.getConstraintName(), "uk_resume_section")){
                return new OptionalValueAndErrorResult<>(Optional.empty(),
                        Optional.of("A resume can't have more than 1 section with the same title"));
            }

        }
        return new OptionalValueAndErrorResult<>(Optional.empty(), Optional.empty());
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

    public OptionalValueAndErrorResult<Resume> setExperiences(Resume resume, List<Experience> newExperiences){
        List<Experience> oldExperiences = resume.getExperiences();
        try {
            resume.setExperiences(newExperiences);
            return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(resume)),
                    Optional.empty());
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            //Restore the old experiences in memory
            resume.setExperiences(oldExperiences);
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_resume_company")){
                return new OptionalValueAndErrorResult<>(Optional.empty(),
                        Optional.of("The new experiences can't have more than 1 experience with a company"));
            }
        }
        return new OptionalValueAndErrorResult<>(Optional.empty(), Optional.empty());
    }

    public Resume setHeader(Resume resume, Header newHeader){
        resume.setHeader(newHeader);
        return resumeRepository.save(resume);
    }

    public Resume setEducation(Resume resume, Education newEducation){
        resume.setEducation(newEducation);
        return resumeRepository.save(resume);
    }

    public OptionalValueAndErrorResult<Resume> setSections(Resume resume, List<ResumeSection> newSections){
        List<ResumeSection> oldSections = resume.getSections();
        try {
            resume.setSections(newSections);
            return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(resume)),
                    Optional.empty());
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            resume.setSections(oldSections);
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_resume_section")){
               return new OptionalValueAndErrorResult<>(Optional.empty(),
                       Optional.of("The new sections can't have more than 1 section with the same title"));
            }

        }
        return new OptionalValueAndErrorResult<>(Optional.empty(), Optional.empty());
    }

    public OptionalValueAndErrorResult<Resume> fullUpdate(Resume resume, FullResumeForm resumeForm){
        Header oldHeader = resume.getHeader();
        Education oldEducation = resume.getEducation();
        List<Experience> oldExperiences = resume.getExperiences();
        List<ResumeSection> oldSections = resume.getSections();
        try {
            //We can directly operate on the result of the getter like this, because java returns by reference
            resume.getHeader().setName(resumeForm.name());
            resume.getHeader().setEmail(resumeForm.email());
            resume.getHeader().setNumber(resumeForm.phoneNumber());
            resume.getEducation().setSchoolName(resumeForm.schoolName());
            resume.getEducation().setRelevantCoursework(resumeForm.relevantCoursework());
            resume.setExperiences(resumeForm.experiences());
            resume.setSections(resumeForm.sections());
            return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(resume)),
                    Optional.empty());
        }
        catch(DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            resume.setExperiences(oldExperiences);
            resume.setHeader(oldHeader);
            resume.setSections(oldSections);
            resume.setEducation(oldEducation);
            if (cause instanceof ConstraintViolationException violationException) {
                switch (violationException.getConstraintName()) {
                    case "uk_resume_section":
                        return new OptionalValueAndErrorResult<>(Optional.of(resume),
                                Optional.of("The new sections can't have more than 1 section with the same title"));
                    case "uk_resume_company":
                        return new OptionalValueAndErrorResult<>(Optional.of(resume),
                                Optional.of("The new experiences can't have more than 1 experience with the same company"));
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return new OptionalValueAndErrorResult<>(Optional.of(resume), Optional.empty());
    }

    public OptionalValueAndErrorResult<Resume> changeName(UUID resID, String newName){
        Resume changingResume = findById(resID);
        String oldName = changingResume.getName();
        try{
            changingResume.setName(newName);
            return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(changingResume)),
                    Optional.empty());
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            changingResume.setName(oldName);
            if(cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                return new OptionalValueAndErrorResult<>(Optional.of(changingResume),
                        Optional.of("You already have a resume with this name"));
            }
            //Should never get here
        }
        return new OptionalValueAndErrorResult<>(Optional.of(changingResume),
                Optional.empty());
    }

    public OptionalValueAndErrorResult<Resume> copyResume(UUID resID, String newName){
        Resume copiedResume = findById(resID);
        try {
            Resume newResume = new Resume(copiedResume, newName);
            return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(newResume)),
                    Optional.empty());
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if(cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                return new OptionalValueAndErrorResult<>(Optional.empty(),
                        Optional.of("The new resume must have a different name than the original one"));
            }
            //Should never get here
        }
        return new OptionalValueAndErrorResult<>(Optional.empty(),
                Optional.empty());

    }

    public ResumeVersion snapshotCurrentData(UUID resume_id){
        Resume copiedResume = findById(resume_id);
        ResumeVersion newVersion = new ResumeVersion(copiedResume.getHeader(), copiedResume.getEducation(),
                copiedResume.getExperiences(), copiedResume.getSections());
        copiedResume.getSavedVersions().add(newVersion);
        newVersion.setAssociatedResume(copiedResume);
        return versionRepository.save(newVersion);
    }

    public OptionalValueAndErrorResult<Resume> switchToAnotherVersion(UUID resume_id, UUID version_id){
        Resume switchingResume = findById(resume_id);
        Header oldHeader = switchingResume.getHeader();
        Education oldEducation = switchingResume.getEducation();
        List<Experience> oldExperiences = switchingResume.getExperiences();
        List<ResumeSection> oldSections = switchingResume.getSections();

        /*
        * This avoids looking up the database again, saving time in most cases
        * Additionally, UUID.equals is very fast, since it just compares the most
        * and least significant bits rather than the entire string, so this algorithm is actually
        * efficient unless we have a very large number of versions
        * */

        ResumeVersion versionToSwitch = switchingResume.getSavedVersions().
                stream().filter(version -> version.getId().equals(version_id)).findFirst().orElse(
                        new ResumeVersion(switchingResume.getHeader(), switchingResume.getEducation(),
                                switchingResume.getExperiences(), switchingResume.getSections())
                );

        try {

            switchingResume.setHeader(versionToSwitch.getVersionedHeader());
            switchingResume.setEducation(versionToSwitch.getVersionedEducation());
            switchingResume.setExperiences(versionToSwitch.getVersionedExperiences());
            switchingResume.setSections(versionToSwitch.getVersionedSections());
            return new OptionalValueAndErrorResult<>(Optional.of(resumeRepository.save(switchingResume)),
                    Optional.empty());
        }
        catch(DataIntegrityViolationException e) {
            Throwable cause = e.getCause();
            switchingResume.setExperiences(oldExperiences);
            switchingResume.setSections(oldSections);
            switchingResume.setEducation(oldEducation);
            switchingResume.setHeader(oldHeader);
            if (cause instanceof ConstraintViolationException violationException) {
                switch (violationException.getConstraintName()) {
                    case "uk_resume_section":
                        return new OptionalValueAndErrorResult<>(Optional.of(switchingResume),
                                Optional.of("The new sections can't have more than 1 section with the same title"));
                    case "uk_resume_company":
                        return new OptionalValueAndErrorResult<>(Optional.of(switchingResume),
                                Optional.of("The new experiences can't have more than 1 experience with the same company"));
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return new OptionalValueAndErrorResult<>(Optional.of(switchingResume), Optional.empty());
    }

    public void deleteVersion(UUID version_id){
        versionRepository.deleteById(version_id);
    }

    public HomePageData loadHomePageInformation(User user, int pageNumber, int pageSize){
        Pageable pageableResult = PageRequest.of(pageNumber, pageSize,
                Sort.by("creationDate").descending().
                        and(Sort.by("lastModifiedDate").descending()));
        Page<Resume> resultingPage = resumeRepository.findAllById(user.getId(), pageableResult);
        return new HomePageData(resultingPage.getContent(), resultingPage.getNumber(), resultingPage.getTotalElements(),
                resultingPage.getTotalPages(), pageSize, user.getProfile());
    }

}
