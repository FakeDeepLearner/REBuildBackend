package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.repository.ResumeRepository;

import com.rebuild.backend.repository.ResumeVersionRepository;
import com.rebuild.backend.utils.ResumeGetUtility;
import com.rebuild.backend.utils.YearMonthStringOperations;
import com.rebuild.backend.utils.converters.ObjectConverter;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ResumeGetUtility getUtility;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         ResumeVersionRepository versionRepository,
                         ObjectConverter objectConverter,
                         ResumeGetUtility getUtility) {
        this.resumeRepository = resumeRepository;
        this.versionRepository = versionRepository;
        this.objectConverter = objectConverter;
        this.getUtility = getUtility;
    }

    @Transactional
    public Resume changeHeaderInfo(User changingUser, int resumeIndex,
                                   HeaderForm headerForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
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

    public Resume findByUserIndex(User user, int index){
        return getUtility.findByUserResumeIndex(user, index);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> changeExperienceInfo(
            User changingUser, int resumeIndex, int experienceIndex,
                                           ExperienceForm experienceForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
        //These variables are guaranteed to be properly initialized after the try block executes
        Experience changingExperience = resume.getExperiences().get(experienceIndex);
        int removedIndex = 0;
        try {
            YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
            YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
            changingExperience.setLocation(experienceForm.location());
            changingExperience.setEndDate(end);
            changingExperience.setStartDate(start);
            changingExperience.setBullets(experienceForm.bullets());
            changingExperience.setTechnologyList(experienceForm.technologies());
            changingExperience.setCompanyName(experienceForm.companyName());
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            resume.addExperience(removedIndex, changingExperience);
            // undoAdder.removeUndoState(resID);
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
    public Resume changeEducationInfo(User changingUser, int resumeIndex, EducationForm educationForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
        Education education = resume.getEducation();
        education.setRelevantCoursework(educationForm.relevantCoursework());
        education.setSchoolName(educationForm.schoolName());
        education.setLocation(educationForm.location());
        education.setStartDate(YearMonthStringOperations.getYearMonth(educationForm.startDate()));
        education.setEndDate(YearMonthStringOperations.getYearMonth(educationForm.endDate()));
        return resumeRepository.save(resume);
    }

    @Transactional
    public Header createNewHeader(User changingUser, int resumeIndex, HeaderForm headerForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
        Header newHeader = new Header(headerForm.number(), headerForm.firstName(),
                headerForm.lastName(), headerForm.email());
        resume.setHeader(newHeader);
        newHeader.setResume(resume);
        resumeRepository.save(resume);
        return newHeader;

    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> createNewExperience(User changingUser, int resumeIndex,
                                                                   ExperienceForm experienceForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
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
            // undoAdder.removeUndoState(resID);
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
    public Resume createNewEducation(User changingUser, int resumeIndex, EducationForm educationForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
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
    public OptionalValueAndErrorResult<Resume> createNewSection(User user, int resumeIndex,
                                                                SectionForm sectionForm){
        Resume resume = getUtility.findByUserResumeIndex(user, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
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
            // undoAdder.removeUndoState(resID);
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
    public Resume deleteEducation(User changingUser, int resumeIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
        resume.setEducation(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteExperience(User changingUser, int resumeIndex, int experienceIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
        resume.getExperiences().remove(experienceIndex);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteSection(User user, int resumeIndex, int sectionIndex){
        Resume resume = getUtility.findByUserResumeIndex(user, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
        resume.getSections().remove(sectionIndex);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteHeader(User changingUser, int resumeIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        // undoAdder.addUndoResumeState(resID, resume);
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
    public OptionalValueAndErrorResult<Resume> changeName(User changingUser, int resumeIndex, String newName){
        Resume changingResume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
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
    public OptionalValueAndErrorResult<Resume> copyResume(User user, int index, String newName){
        Resume copiedResume = getUtility.findByUserResumeIndex(user, index);
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


}
