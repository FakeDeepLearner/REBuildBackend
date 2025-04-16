package com.rebuild.backend.service.resume_services;


import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.repository.ResumeRepository;

import com.rebuild.backend.repository.ResumeVersionRepository;
import com.rebuild.backend.utils.UndoAdder;
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

    private final UndoAdder undoAdder;

    @Autowired
    public ResumeService(ResumeRepository resumeRepository,
                         ResumeVersionRepository versionRepository,
                         ObjectConverter objectConverter,
                         UndoAdder undoAdder) {
        this.resumeRepository = resumeRepository;
        this.versionRepository = versionRepository;
        this.objectConverter = objectConverter;
        this.undoAdder = undoAdder;
    }

    @Transactional
    public Resume changeHeaderInfo(UUID resID,
                                   HeaderForm headerForm){
        Resume resume = findById(resID);
        undoAdder.addUndoResumeState(resID, resume);
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
        undoAdder.addUndoResumeState(resID, resume);
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
            undoAdder.removeUndoState(resID);
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
        undoAdder.addUndoResumeState(resID, resume);
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
        undoAdder.addUndoResumeState(resID, resume);
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
        undoAdder.addUndoResumeState(resID, resume);
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
            undoAdder.removeUndoState(resID);
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
        undoAdder.addUndoResumeState(resID, resume);
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
        undoAdder.addUndoResumeState(resID, resume);
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
            undoAdder.removeUndoState(resID);
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
        undoAdder.addUndoResumeState(resID, resume);
        resume.setEducation(null);
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteExperience(UUID resID, UUID expID){
        Resume resume = findById(resID);
        undoAdder.addUndoResumeState(resID, resume);
        resume.getExperiences().removeIf(experience -> experience.getId().equals(expID));
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteSection(UUID resID, UUID sectionID){
        Resume resume = findById(resID);
        undoAdder.addUndoResumeState(resID, resume);
        resume.getSections().removeIf(section -> section.getId().equals(sectionID));
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume deleteHeader(UUID resID){
        Resume resume = findById(resID);
        undoAdder.addUndoResumeState(resID, resume);
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
    public ResumeVersion snapshotCurrentData(UUID resume_id, VersionInclusionForm inclusionForm){
        Resume copiedResume = findById(resume_id);
        ResumeVersion newVersion = createSnapshot(copiedResume, inclusionForm);
        int currentVersionCount = copiedResume.getVersionCount();
        if(currentVersionCount < Resume.MAX_VERSION_COUNT){
            copiedResume.setVersionCount(copiedResume.getVersionCount() + 1);
        }
        else{
            ResumeVersion oldestRepository = versionRepository.findOldestVersionByResumeId(resume_id).orElse(null);
            assert oldestRepository != null;
            versionRepository.delete(oldestRepository);
        }
        return versionRepository.save(newVersion);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> switchToAnotherVersion(UUID resume_id, UUID version_id){
        Resume switchingResume = findById(resume_id);
        Header oldHeader = switchingResume.getHeader();
        Education oldEducation = switchingResume.getEducation();
        List<Experience> oldExperiences = switchingResume.getExperiences();
        List<ResumeSection> oldSections = switchingResume.getSections();

        ResumeVersion versionToSwitch = versionRepository.findById(version_id).orElse(null);
        assert versionToSwitch != null;
        try {
            handleVersionSwitch(switchingResume, versionToSwitch);
            Resume savedResume = resumeRepository.save(switchingResume);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch(DataIntegrityViolationException e) {
            // If we see an error, restore the old versions in memory. The databse will take care of
            // rolling back the transaction
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

    private void handleVersionSwitch(Resume resume, ResumeVersion versionToSwitch){
        if(versionToSwitch.getVersionedName() != null){
            resume.setName(versionToSwitch.getVersionedName());
        }

        if(versionToSwitch.getVersionedHeader() != null){
            VersionedHeader versionedHeader = versionToSwitch.getVersionedHeader();
            Header newHeader = new Header(versionedHeader.getNumber(),
                    versionedHeader.getFirstName(), versionedHeader.getLastName(), versionedHeader.getEmail());
            newHeader.setResume(resume);
            resume.setHeader(newHeader);
        }

        if(versionToSwitch.getVersionedEducation() != null){
            VersionedEducation versionedEducation = versionToSwitch.getVersionedEducation();
            Education newEducation = new Education(versionedEducation.getSchoolName(), versionedEducation.getRelevantCoursework(),
                    versionedEducation.getLocation(), versionedEducation.getStartDate(), versionedEducation.getEndDate());
            newEducation.setResume(resume);
            resume.setEducation(newEducation);
        }

        if(versionToSwitch.getVersionedExperiences() != null){
            List<VersionedExperience> versionedExperiences = versionToSwitch.getVersionedExperiences();
            List<Experience> newExperiences = versionedExperiences.stream().map(
                    versionedExperience -> {
                        Experience newExperience = new Experience(versionedExperience.getCompanyName(),
                                versionedExperience.getTechnologyList(), versionedExperience.getLocation(),
                                versionedExperience.getStartDate(), versionedExperience.getEndDate(),
                                versionedExperience.getBullets());
                        newExperience.setResume(resume);
                        return newExperience;
                    }

            ).toList();
            resume.setExperiences(newExperiences);
        }

        if(versionToSwitch.getVersionedSections() != null){
            List<VersionedSection> versionedSections = versionToSwitch.getVersionedSections();

            /*
            * Iterate over the versioned sections,
            * and transform them into regular sections by iterating over each of their versioned entries
            * and transforming them into regular entries. Then, associate each entry with a section,
            * and then finally associate those entries collectively to the newly created normal section.
            * (Double lambdas are fun, yay)
            */
            List<ResumeSection> newSections = versionedSections.stream().map(
                    versionedSection -> {
                        ResumeSection newSection = new ResumeSection(versionedSection.getTitle());
                        List<ResumeSectionEntry> transformedEntries = versionedSection.getEntries().
                                stream().map(
                                        rawEntry -> {
                                            ResumeSectionEntry newEntry = new ResumeSectionEntry(
                                                    rawEntry.getTitle(),
                                                    rawEntry.getToolsUsed(),
                                                    rawEntry.getLocation(),
                                                    rawEntry.getStartDate(),
                                                    rawEntry.getEndDate(),
                                                    rawEntry.getBullets()
                                            );
                                            newEntry.setAssociatedSection(newSection);
                                            return newEntry;
                                        }
                                ).toList();
                        newSection.setEntries(transformedEntries);
                        return newSection;
                    }
            ).toList();
            resume.setSections(newSections);
        }
    }

    private ResumeVersion createSnapshot(Resume resume, VersionInclusionForm inclusionForm){
        ResumeVersion newVersion = new ResumeVersion();
        VersionedHeader newHeader = objectConverter.createVersionedHeader(resume.getHeader(),
                inclusionForm.includeHeader(), newVersion);
        VersionedEducation newEducation = objectConverter.createVersionedEducation(
                resume.getEducation(), inclusionForm.includeEducation(), newVersion
        );
        List<VersionedExperience> experiences = objectConverter.createVersionedExperiences(
                resume.getExperiences(), inclusionForm.includeExperience(), newVersion
        );
        List<VersionedSection> sections = objectConverter.createVersionedSections(
                resume.getSections(), inclusionForm.includeSections(), newVersion
        );
        String versionedName = inclusionForm.includeName() ? resume.getName() : null;
        newVersion.setVersionedName(versionedName);
        newVersion.setVersionedHeader(newHeader);
        newVersion.setVersionedEducation(newEducation);
        newVersion.setVersionedExperiences(experiences);
        newVersion.setVersionedSections(sections);
        newVersion.setAssociatedResume(resume);
        return newVersion;
    }


    @Transactional
    public void deleteVersion(UUID resume_id, UUID version_id){
        Resume deletingResume = findById(resume_id);
        deletingResume.setVersionCount(deletingResume.getVersionCount() - 1);
        resumeRepository.save(deletingResume);
        versionRepository.deleteById(version_id);
    }


}
