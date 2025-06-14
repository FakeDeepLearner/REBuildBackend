package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.forms.resume_forms.VersionInclusionForm;
import com.rebuild.backend.repository.ResumeRepository;
import com.rebuild.backend.repository.ResumeVersionRepository;
import com.rebuild.backend.utils.OptionalValueAndErrorResult;
import com.rebuild.backend.utils.ResumeGetUtility;
import com.rebuild.backend.utils.converters.ObjectConverter;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
@Transactional(readOnly = true)
public class ResumeVersioningService {

    private final ResumeRepository resumeRepository;

    private final ResumeGetUtility getUtility;

    private final ObjectConverter objectConverter;

    private final ResumeVersionRepository versionRepository;

    private final EntityManager entityManager;

    public ResumeVersioningService(ResumeRepository resumeRepository, ResumeGetUtility getUtility,
                                   ObjectConverter objectConverter,
                                   ResumeVersionRepository versionRepository, EntityManager entityManager) {
        this.resumeRepository = resumeRepository;
        this.getUtility = getUtility;
        this.objectConverter = objectConverter;
        this.versionRepository = versionRepository;
        this.entityManager = entityManager;
    }

    private List<ResumeVersion> findVersionsByIdAndLimit(UUID resumeId, int limit){
        return entityManager.createNamedQuery("ResumeVersion.findAllByResumeIdWithLimit", ResumeVersion.class).
                setParameter("resumeId", resumeId).setMaxResults(limit).getResultList();
    }


    @Transactional
    public ResumeVersion snapshotCurrentData(User user, int index, VersionInclusionForm inclusionForm){
        Resume copiedResume = getUtility.findByUserResumeIndex(user, index);
        ResumeVersion newVersion = createSnapshot(copiedResume, inclusionForm);
        int currentVersionCount = copiedResume.getVersionCount();
        if(currentVersionCount < Resume.MAX_VERSION_COUNT){
            copiedResume.setVersionCount(copiedResume.getVersionCount() + 1);
        }
        return versionRepository.save(newVersion);
    }

    @Transactional
    public OptionalValueAndErrorResult<Resume> switchToAnotherVersion(User user,
                                                                      int resume_index, int version_index){
        Resume switchingResume = getUtility.findByUserResumeIndex(user, resume_index);
        Header oldHeader = switchingResume.getHeader();
        Education oldEducation = switchingResume.getEducation();
        List<Experience> oldExperiences = switchingResume.getExperiences();
        List<ResumeSection> oldSections = switchingResume.getSections();

        ResumeVersion versionToSwitch = findVersionsByIdAndLimit(switchingResume.getId(), version_index).getLast();
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
    public void deleteVersion(User user, int resumeIndex, int versionIndex){
        Resume deletingResume = getUtility.findByUserResumeIndex(user, resumeIndex);
        deletingResume.setVersionCount(deletingResume.getVersionCount() - 1);
        ResumeVersion versionToDelete = findVersionsByIdAndLimit(deletingResume.getId(), versionIndex).getLast();
        versionRepository.delete(versionToDelete);
        resumeRepository.save(deletingResume);
    }
}
