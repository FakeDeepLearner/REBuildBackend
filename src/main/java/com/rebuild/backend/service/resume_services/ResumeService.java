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
        Resume copyResume = Resume.deepCopy(resume);
        Experience changingExperience = resume.getExperiences().get(experienceIndex);
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
        catch (RuntimeException e){
            return OptionalValueAndErrorResult.of(copyResume, "An unexpected error occurred",
                    INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    public Resume changeEducationInfo(User changingUser, int resumeIndex, EducationForm educationForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
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
        Header newHeader = new Header(headerForm.number(), headerForm.firstName(),
                headerForm.lastName(), headerForm.email());
        resume.setHeader(newHeader);
        resumeRepository.save(resume);
        return newHeader;

    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> createNewExperience(User changingUser, int resumeIndex,
                                                                   ExperienceForm experienceForm,
                                                                   Integer experiencesIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        Resume copyResume = Resume.deepCopy(resume);
        YearMonth start = YearMonthStringOperations.getYearMonth(experienceForm.startDate());
        YearMonth end = YearMonthStringOperations.getYearMonth(experienceForm.endDate());
        Experience newExperience = new Experience(experienceForm.companyName(),
                experienceForm.technologies(), experienceForm.location(),
                start, end, experienceForm.bullets());
        try {
            if (experiencesIndex == null) {
                resume.addExperience(newExperience);
            }
            else {
                resume.addExperience(experiencesIndex, newExperience);
            }
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (RuntimeException e){
            return OptionalValueAndErrorResult.of(copyResume,
                    "An unexpected error occurred", INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    public Resume createNewEducation(User changingUser, int resumeIndex, EducationForm educationForm){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        YearMonth startDate = YearMonthStringOperations.getYearMonth(educationForm.startDate());
        YearMonth endDate = YearMonthStringOperations.getYearMonth(educationForm.endDate());
        Education education = new Education(educationForm.schoolName(), educationForm.relevantCoursework(),
                educationForm.location(),
                startDate, endDate);
        resume.setEducation(education);
        return resumeRepository.save(resume);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> createNewSection(User user, int resumeIndex,
                                                                SectionForm sectionForm, Integer sectionsIndex){
        Resume resume = getUtility.findByUserResumeIndex(user, resumeIndex);
        Resume copyResume  = Resume.deepCopy(resume);
        List<ResumeSectionEntry> transformedEntries = objectConverter.
                extractResumeSectionEntries(sectionForm.entryForms());
        ResumeSection newSection = new ResumeSection(transformedEntries, sectionForm.title());
        try {
            if (sectionsIndex == null){
                resume.addSection(newSection);
            }
            else {
                resume.addSection(sectionsIndex, newSection);
            }
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (RuntimeException e){
            return OptionalValueAndErrorResult.of(copyResume,
                    "An unexpected error occurred", INTERNAL_SERVER_ERROR);

        }

    }

    @Transactional
    public void deleteById(UUID id){
        resumeRepository.deleteById(id);
    }

    @Transactional
    public Resume deleteEducation(User changingUser, int resumeIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        resume.setEducation(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteExperience(User changingUser, int resumeIndex, int experienceIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        resume.getExperiences().remove(experienceIndex);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteSection(User user, int resumeIndex, int sectionIndex){
        Resume resume = getUtility.findByUserResumeIndex(user, resumeIndex);
        resume.getSections().remove(sectionIndex);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteHeader(User changingUser, int resumeIndex){
        Resume resume = getUtility.findByUserResumeIndex(changingUser, resumeIndex);
        resume.setHeader(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> setExperiences(Resume resume, List<Experience> newExperiences){
        Resume copyResume = Resume.deepCopy(resume);
        try {
            resume.setExperiences(newExperiences);
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (RuntimeException e){
            return OptionalValueAndErrorResult.of(copyResume,
                    "An unexpected error occurred", INTERNAL_SERVER_ERROR);
        }
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
        Resume copyResume = Resume.deepCopy(resume);
        try {
            resume.setSections(newSections);
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch (RuntimeException e){
            return OptionalValueAndErrorResult.of(copyResume,
                    "An unexpected error occurred", INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> fullUpdate(Resume resume, FullResumeForm resumeForm){
        Resume copyResume = Resume.deepCopy(resume);
        try {

            //We can't modify the resume's fields directly here, as that would also modify the variables that
            // we declared outside the try block, causing a bug.
            Header newHeader = new Header(resumeForm.headerForm().number(),
                    resumeForm.headerForm().firstName(),
                    resumeForm.headerForm().lastName(), resumeForm.headerForm().email());
            resume.setHeader(newHeader);

            Education newEducation = new Education(resumeForm.educationForm().schoolName(),
                    resumeForm.educationForm().relevantCoursework(),
                    resumeForm.educationForm().location(),
                    YearMonthStringOperations.getYearMonth(resumeForm.educationForm().startDate()),
                    YearMonthStringOperations.getYearMonth(resumeForm.educationForm().endDate()));
            resume.setEducation(newEducation);
            resume.setExperiences(objectConverter.extractExperiences(resumeForm.experiences(), resume));
            resume.setSections(objectConverter.extractResumeSections(resumeForm.sections(), resume));
            Resume savedResume = resumeRepository.save(resume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch(RuntimeException e){
            return OptionalValueAndErrorResult.of(copyResume,
                    "An unexpected error occurred", INTERNAL_SERVER_ERROR);
        }

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
