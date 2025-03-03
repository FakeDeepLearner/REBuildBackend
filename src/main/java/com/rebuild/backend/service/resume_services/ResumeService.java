package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.repository.ResumeRepository;

import com.rebuild.backend.repository.ResumeVersionRepository;
import com.rebuild.backend.utils.YearMonthStringOperations;
import com.rebuild.backend.utils.converters.ObjectConverter;
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
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private final ResumeVersionRepository versionRepository;

    private final ObjectConverter objectConverter;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         ResumeVersionRepository versionRepository,
                         ObjectConverter objectConverter) {
        this.resumeRepository = resumeRepository;
        this.versionRepository = versionRepository;
        this.objectConverter = objectConverter;
    }

    @Transactional
    public Resume changeHeaderInfo(UUID resID,
                                   HeaderForm headerForm){
        Resume resume = findById(resID);
        Header header = resume.getHeader();
        header.setEmail(headerForm.email());
        header.setNumber(headerForm.number());
        header.setFirstName(headerForm.firstName());
        header.setLastName(headerForm.lastName());
        return resumeRepository.save(resume);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> createNewResumeFor(String resume_name, User user){
        if(user.maxResumeLimitReached()){
            return OptionalValueAndErrorResult.of("You have reached the maximum number of " +
                    "resumes you can create as a free user", PAYMENT_REQUIRED);
        }
        else{
            try {
                Resume newResume = new Resume(resume_name, user);
                user.getResumes().add(newResume);
                Resume savedResume = resumeRepository.save(newResume);
                return OptionalValueAndErrorResult.of(savedResume, CREATED);
            }
            catch (DataIntegrityViolationException e){
                Throwable cause = e.getCause();
                if(cause instanceof ConstraintViolationException violationException &&
                        Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                    return OptionalValueAndErrorResult.of("You already have a resume with this name", CONFLICT);
                }
            }

        }
        return OptionalValueAndErrorResult.empty();
    }


    public boolean resumeBelongsToUser(UUID resumeID, UUID userID){
        return resumeRepository.countByIdAndUserId(resumeID, userID) > 0;
    }

    //TODO: Throw a proper exception here and handle it properly
    public Resume findById(UUID id){
        return resumeRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> changeExperienceInfo(UUID resID, UUID expID,
                                           ExperienceForm experienceForm){
        Resume resume = findById(resID);
        //These variables are guaranteed to be properly initialized after the try block executes
        Experience removedExperience = null;
        int removedIndex = 0;
        try {
            YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
            YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
            Experience newExperience = new Experience(experienceForm.companyName(), experienceForm.technologies(),
                    experienceForm.location(), start, end, experienceForm.bullets());
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
            if(removedExperience == null){
                return OptionalValueAndErrorResult.of(resume, "Experience not found", NOT_FOUND);
            }
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            resume.addExperience(removedIndex, removedExperience);
            if (cause instanceof ConstraintViolationException violationException
                    && Objects.equals(violationException.getConstraintName(), "uk_resume_company")){
                return OptionalValueAndErrorResult.of(resume,
                        "A resume can't have more than 1 experience with the same company", CONFLICT);
            }
        }
        return OptionalValueAndErrorResult.of(resume, "An unexpected error occurred",
                INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public Resume changeEducationInfo(UUID resID, EducationForm educationForm){
        Resume resume = findById(resID);
        Education education = resume.getEducation();
        education.setRelevantCoursework(educationForm.relevantCoursework());
        education.setSchoolName(educationForm.schoolName());
        education.setLocation(educationForm.location());
        education.setStartDate(YearMonthStringOperations.getYearMonth(educationForm.startDate()));
        education.setEndDate(YearMonthStringOperations.getYearMonth(educationForm.endDate()));
        return resumeRepository.save(resume);
    }

    @Transactional
    public Header createNewHeader(UUID resID, HeaderForm headerForm){
        Resume resume = findById(resID);
        Header newHeader = new Header(headerForm.number(), headerForm.firstName(),
                headerForm.lastName(), headerForm.email());
        resume.setHeader(newHeader);
        newHeader.setResume(resume);
        resumeRepository.save(resume);
        return newHeader;

    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> createNewExperience(UUID resID,
                                                                   ExperienceForm experienceForm){
        Resume resume = findById(resID);
        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        Experience newExperience = new Experience(experienceForm.companyName(),
                experienceForm.technologies(), experienceForm.location(),
                start, end, experienceForm.bullets());
        try {
            resume.addExperience(newExperience);
            newExperience.setResume(resume);
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            //Remove the element that was added last (the one added in the try block above)
            resume.getExperiences().removeLast();
            //No loose ends
            newExperience.setResume(null);
            if (cause instanceof ConstraintViolationException violationException
                    && Objects.equals(violationException.getConstraintName(), "uk_resume_company")){
                return OptionalValueAndErrorResult.of(resume,
                        "A resume can't have more than 1 experience with a company", CONFLICT);
            }
        }
        return OptionalValueAndErrorResult.of(resume, "An unexpected error occurred", INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public Resume createNewEducation(UUID resID, EducationForm educationForm){
        Resume resume = findById(resID);
        YearMonth startDate = YearMonthStringOperations.getYearMonth(educationForm.startDate());
        YearMonth endDate = YearMonthStringOperations.getYearMonth(educationForm.endDate());
        Education education = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(),
                startDate, endDate);
        resume.setEducation(education);
        education.setResume(resume);
        return resumeRepository.save(resume);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> createNewSection(UUID resID,
                                                                SectionForm sectionForm){
        Resume resume = findById(resID);
        ResumeSection newSection = new ResumeSection(sectionForm.title());

        List<ResumeSectionEntry> transformedEntries = objectConverter.
                extractResumeSectionEntries(sectionForm.entryForms(), newSection);
        newSection.setEntries(transformedEntries);
        try {
            resume.addSection(newSection);
            newSection.setResume(resume);
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            resume.getSections().removeLast();
            newSection.setResume(null);
            if (cause instanceof ConstraintViolationException violationException
                    && Objects.equals(violationException.getConstraintName(), "uk_resume_section")){
                return OptionalValueAndErrorResult.of(resume,
                        "A resume can't have more than 1 section with the same title", CONFLICT);
            }

        }
        return OptionalValueAndErrorResult.of(resume, "An unexpected error occurred", INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public void deleteById(UUID id){
        resumeRepository.deleteById(id);
    }

    @Transactional
    public Resume deleteEducation(UUID resID){
        Resume resume = findById(resID);
        resume.setEducation(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteExperience(UUID resID, UUID expID){
        Resume resume = findById(resID);
        resume.getExperiences().removeIf(experience -> experience.getId().equals(expID));
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteSection(UUID resID, UUID sectionID){
        Resume resume = findById(resID);
        resume.getSections().removeIf(section -> section.getId().equals(sectionID));
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteHeader(UUID resID){
        Resume resume = findById(resID);
        resume.setHeader(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> setExperiences(Resume resume, List<Experience> newExperiences){
        List<Experience> oldExperiences = resume.getExperiences();
        try {
            resume.setExperiences(newExperiences);
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            //Restore the old experiences in memory
            resume.setExperiences(oldExperiences);
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_resume_company")){
                return  OptionalValueAndErrorResult.of(resume,
                        "A resume can't have more than 1 experience with a company", CONFLICT);
            }
        }
        return OptionalValueAndErrorResult.of(resume, "An unexpected error occurred", INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public Resume setHeader(Resume resume, Header newHeader){
        resume.setHeader(newHeader);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume setEducation(Resume resume, Education newEducation){
        resume.setEducation(newEducation);
        return resumeRepository.save(resume);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> setSections(Resume resume, List<ResumeSection> newSections){
        List<ResumeSection> oldSections = resume.getSections();
        try {
            resume.setSections(newSections);
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            resume.setSections(oldSections);
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_resume_section")){
               return  OptionalValueAndErrorResult.of(resume,
                       "The new sections can't have more than 1 section with the same title", CONFLICT);
            }

        }
        return OptionalValueAndErrorResult.of(resume, "An unexpected error occurred", INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> fullUpdate(Resume resume, FullResumeForm resumeForm){
        Header oldHeader = resume.getHeader();
        Education oldEducation = resume.getEducation();
        List<Experience> oldExperiences = resume.getExperiences();
        List<ResumeSection> oldSections = resume.getSections();
        try {

            //We can't modify the resume's fields directly here, as that would also modify the variables that
            // we declared outside the try block, causing a bug.
            Header newHeader = new Header(resumeForm.headerForm().number(),
                    resumeForm.headerForm().firstName(),
                    resumeForm.headerForm().lastName(), resumeForm.headerForm().email());
            newHeader.setResume(resume);
            resume.setHeader(newHeader);

            Education newEducation = new Education(resumeForm.educationForm().schoolName(),
                    resumeForm.educationForm().relevantCoursework(),
                    resumeForm.educationForm().location(),
                    YearMonthStringOperations.getYearMonth(resumeForm.educationForm().startDate()),
                    YearMonthStringOperations.getYearMonth(resumeForm.educationForm().endDate()));
            newEducation.setResume(resume);
            resume.setEducation(newEducation);
            resume.setExperiences(objectConverter.extractExperiences(resumeForm.experiences(), resume));
            resume.setSections(objectConverter.extractResumeSections(resumeForm.sections(), resume));
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
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
                        return OptionalValueAndErrorResult.of(resume,
                                "The new sections can't have more than 1 section with the same title", CONFLICT);
                    case "uk_resume_company":
                        return OptionalValueAndErrorResult.of(resume,
                                "The new experiences can't have more than 1 experience with the same company", CONFLICT);
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return OptionalValueAndErrorResult.of(resume, "An unexpected error occurred", INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> changeName(UUID resID, String newName){
        Resume changingResume = findById(resID);
        String oldName = changingResume.getName();
        try{
            changingResume.setName(newName);
            Resume savedResume = resumeRepository.save(changingResume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            changingResume.setName(oldName);
            if(cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                return OptionalValueAndErrorResult.of(changingResume, "You already have a resume with this name", CONFLICT);
            }
            //Should never get here
        }
        return OptionalValueAndErrorResult.of(changingResume, "An unexpected error occurred", INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> copyResume(UUID resID, String newName){
        Resume copiedResume = findById(resID);
        if(newName.equals(copiedResume.getName())){
            return OptionalValueAndErrorResult.
                    of("The new resume must have a different name than the original one", CONFLICT);
        }
        try {
            Resume newResume = new Resume(copiedResume, newName);
            Resume savedResume = resumeRepository.save(newResume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if(cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")){
                return OptionalValueAndErrorResult.of("The new resume must have a different name than the original one", CONFLICT);
            }
            //Should never get here
        }
        return OptionalValueAndErrorResult.empty();

    }

    @Transactional
    public ResumeVersion snapshotCurrentData(UUID resume_id){
        Resume copiedResume = findById(resume_id);
        ResumeVersion newVersion = new ResumeVersion(copiedResume.getHeader(), copiedResume.getEducation(),
                copiedResume.getExperiences(), copiedResume.getSections());
        copiedResume.getSavedVersions().add(newVersion);
        newVersion.setAssociatedResume(copiedResume);
        return versionRepository.save(newVersion);
    }

    @Transactional
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
            Resume savedResume = resumeRepository.save(switchingResume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
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
                        return OptionalValueAndErrorResult.of(switchingResume,
                                "The new sections can't have more than 1 section with the same title", CONFLICT);
                    case "uk_resume_company":
                        return  OptionalValueAndErrorResult.of(switchingResume,
                                "The new experiences can't have more than 1 experience with the same company", CONFLICT);
                    case null:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + violationException.getConstraintName());
                }
            }
        }
        return OptionalValueAndErrorResult.of(switchingResume, "An unexpected error occurred", INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public void deleteVersion(UUID version_id){
        versionRepository.deleteById(version_id);
    }



}
