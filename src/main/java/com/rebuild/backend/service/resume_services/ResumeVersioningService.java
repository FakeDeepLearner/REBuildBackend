package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.forms.resume_forms.VersionInclusionForm;
import com.rebuild.backend.model.forms.resume_forms.VersionSwitchPreferencesForm;
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
                                                                      int resume_index, int version_index,
                                                                      VersionSwitchPreferencesForm versionSwitchPreferencesForm){
        Resume switchingResume = getUtility.findByUserResumeIndex(user, resume_index);
        Header oldHeader = switchingResume.getHeader();
        Education oldEducation = switchingResume.getEducation();
        List<Experience> oldExperiences = switchingResume.getExperiences();
        List<ResumeSection> oldSections = switchingResume.getSections();

        ResumeVersion versionToSwitch = findVersionsByIdAndLimit(switchingResume.getId(), version_index).getLast();
        assert versionToSwitch != null;
        try {
            handleVersionSwitch(switchingResume, versionToSwitch, versionSwitchPreferencesForm);
            Resume savedResume = resumeRepository.save(switchingResume);
            versionRepository.save(versionToSwitch);
            return OptionalValueAndErrorResult.of(savedResume, OK);
        }
        catch(DataIntegrityViolationException e) {
            // If we see an error, restore the old versions in memory. The database will take care of
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

    private void handleVersionSwitch(Resume resume, ResumeVersion versionToSwitch,
                                     VersionSwitchPreferencesForm preferencesForm){
        if(versionToSwitch.getVersionedName() != null){
            String tempName =  resume.getName();
            resume.setName(versionToSwitch.getVersionedName());
            versionToSwitch.setVersionedName(tempName);
        }

        if(versionToSwitch.getVersionedHeader() != null && preferencesForm.includeHeader()){
            Header newHeader = getHeader(resume, versionToSwitch, preferencesForm.makeHeaderCopy());
            resume.setHeader(newHeader);
        }

        if(versionToSwitch.getVersionedEducation() != null && preferencesForm.includeEducation()){
            Education newEducation = getEducation(resume, versionToSwitch, preferencesForm.makeEducationCopy());
            resume.setEducation(newEducation);
        }

        if(versionToSwitch.getVersionedExperiences() != null && !preferencesForm.experienceIndices().isEmpty()){
            List<Experience> newExperiences = getExperiences(resume, versionToSwitch,
                    preferencesForm.makeExperienceCopies(), preferencesForm.experienceIndices());
            resume.setExperiences(newExperiences);
        }

        if(versionToSwitch.getVersionedSections() != null && !preferencesForm.sectionIndices().isEmpty()){
            List<ResumeSection> newSections = getSections(resume, versionToSwitch,
                    preferencesForm.makeSectionCopies(), preferencesForm.sectionIndices());
            resume.setSections(newSections);
        }
    }

    private static List<Experience> getExperiences(Resume resume, ResumeVersion versionToSwitch,
                                                   boolean makeCopies, List<Integer> indicesToSelect){
        if (!makeCopies) {
            List<Experience> oldResumeExperiences = resume.getExperiences();

            List<Experience> versionedExperiences = versionToSwitch.getVersionedExperiences();
            versionToSwitch.setVersionedExperiences(oldResumeExperiences);
            return versionedExperiences;
        }
        else
        {
            List<Experience> versionedExperiences = versionToSwitch.getVersionedExperiences();

            return indicesToSelect.stream()
                    .map(versionedExperiences::get)
                    .map(Experience::copy).toList();
        }
    }

    private static Education getEducation(Resume resume, ResumeVersion versionToSwitch, boolean makeCopy) {
        if (!makeCopy) {

            Education oldResumeEducation = resume.getEducation();

            Education versionedEducation = versionToSwitch.getVersionedEducation();

            versionToSwitch.setVersionedEducation(oldResumeEducation);
            return versionedEducation;
        }
        else
        {
            return Education.copy(versionToSwitch.getVersionedEducation());
        }
    }

    private static Header getHeader(Resume resume, ResumeVersion versionToSwitch, boolean makeCopy) {
        if (!makeCopy) {
            Header oldResumeHeader = resume.getHeader();

            Header versionedHeader = versionToSwitch.getVersionedHeader();

            versionToSwitch.setVersionedHeader(oldResumeHeader);

            return versionedHeader;
        }
        else
        {
            return Header.copy(versionToSwitch.getVersionedHeader());

        }
    }


    private static List<ResumeSection> getSections(Resume resume, ResumeVersion versionToSwitch,
                                                   boolean makeCopies, List<Integer> indicesToSelect) {
        if (!makeCopies) {
            List<ResumeSection> oldResumeSections = resume.getSections();

            List<ResumeSection> versionedSections = versionToSwitch.getVersionedSections();
            versionToSwitch.setVersionedSections(oldResumeSections);
            return versionedSections;
        }

        else {
            List<ResumeSection> versionedSections = versionToSwitch.getVersionedSections();
            return indicesToSelect.stream().
                    map(versionedSections::get).
                    map(ResumeSection::copy).toList();
        }
    }

    private ResumeVersion createSnapshot(Resume resume, VersionInclusionForm inclusionForm){
        ResumeVersion newVersion = new ResumeVersion();
        Header newHeader = objectConverter.createVersionedHeader(resume.getHeader(),
                inclusionForm.includeHeader(), newVersion);
        Education newEducation = objectConverter.createVersionedEducation(
                resume.getEducation(), inclusionForm.includeEducation(), newVersion
        );
        List<Experience> experiences = objectConverter.createVersionedExperiences(
                resume.getExperiences(), inclusionForm.includeExperience(), newVersion
        );
        List<ResumeSection> sections = objectConverter.createVersionedSections(
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
