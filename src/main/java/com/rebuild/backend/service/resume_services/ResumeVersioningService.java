package com.rebuild.backend.service.resume_services;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.forms.resume_forms.VersionCreationForm;
import com.rebuild.backend.model.forms.resume_forms.VersionSwitchPreferencesForm;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeVersionRepository;
import com.rebuild.backend.utils.ResumeGetUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ResumeVersioningService {

    private final ResumeRepository resumeRepository;

    private final ResumeGetUtility getUtility;

    private final ResumeVersionRepository versionRepository;

    private final ResumeService resumeService;

    public ResumeVersioningService(ResumeRepository resumeRepository, ResumeGetUtility getUtility,
                                   ResumeVersionRepository versionRepository,
                                   ResumeService resumeService) {
        this.resumeRepository = resumeRepository;
        this.getUtility = getUtility;
        this.versionRepository = versionRepository;
        this.resumeService = resumeService;
    }

    private ResumeVersion findByResumeAndVersionId(UUID versionId, Resume resume)
    {
        return versionRepository.findByIdAndAssociatedResume(versionId, resume).orElseThrow(
                () -> new BelongingException("This version either does not exist or does not belong to this resume")
        );
    }


    @Transactional
    public ResumeVersion snapshotCurrentData(User user, UUID resumeId, VersionCreationForm inclusionForm){
        Resume copiedResume = getUtility.findByUserResumeId(user, resumeId);
        ResumeVersion newVersion = createSnapshot(copiedResume, inclusionForm);
        int currentVersionCount = copiedResume.getVersionCount();
        if(currentVersionCount < Resume.MAX_VERSION_COUNT){
            copiedResume.setVersionCount(copiedResume.getVersionCount() + 1);
        }
        return versionRepository.save(newVersion);
    }

    @Transactional
    public Resume switchToAnotherVersion(User user,
                                         UUID resumeId, UUID versionId,
                                         VersionSwitchPreferencesForm versionSwitchPreferencesForm){
        Resume switchingResume = getUtility.findByUserResumeId(user, resumeId);

        ResumeVersion versionToSwitch = findByResumeAndVersionId(versionId, switchingResume);
        handleVersionSwitch(switchingResume, versionToSwitch, versionSwitchPreferencesForm);
        versionRepository.save(versionToSwitch);
        return resumeRepository.save(switchingResume);

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
            resumeService.setHeader(resume, newHeader);
        }

        if(versionToSwitch.getVersionedEducation() != null && preferencesForm.includeEducation()){
            Education newEducation = getEducation(resume, versionToSwitch, preferencesForm.makeEducationCopy());
            resumeService.setEducation(resume, newEducation);
        }

        if(versionToSwitch.getVersionedExperiences() != null && !preferencesForm.experienceIds().isEmpty()){
            List<Experience> newExperiences = getExperiences(resume, versionToSwitch,
                    preferencesForm.makeExperienceCopies(), preferencesForm.experienceIds());
            resumeService.setExperiences(resume, newExperiences);
        }

    }

    private List<Experience> getExperiences(Resume resume, ResumeVersion versionToSwitch,
                                                   boolean makeCopies, List<UUID> identifiersToSelect){
        if (!makeCopies) {
            List<Experience> oldResumeExperiences = resume.getExperiences();

            List<Experience> versionedExperiences = versionToSwitch.getVersionedExperiences();
            versionToSwitch.setVersionedExperiences(oldResumeExperiences);
            return versionedExperiences;
        }
        else
        {
            List<Experience> versionedExperiences = versionToSwitch.getVersionedExperiences();
            Set<UUID> convertedSet = new HashSet<>(identifiersToSelect);

            return versionedExperiences.stream().
                    filter(experience -> convertedSet.contains(experience.getId())).toList();
        }
    }

    private Education getEducation(Resume resume, ResumeVersion versionToSwitch, boolean makeCopy) {
        // If we don't want to make a copy of the versioned education, we simply swap the version with the resume
        // and the ResumeVersion. The exact same logic holds for the methods below as well.
        if (!makeCopy) {

            Education oldResumeEducation = resume.getEducation();

            Education versionedEducation = versionToSwitch.getVersionedEducation();

            /*
             * We set the versioned attribute into a copy of the old one rather than the original one,
             * because we want to avoid a potential bug where the identifier of the versioned education and
             * the new one on the resume will be the same, since updating the resume now takes inplace instead
             * of a simple attribute set. Since the copying code creates a new attribute, it avoids those bugs.
             * The exact same logic follows for the getHeader and the getExperiences functions as well.
             * */
            versionToSwitch.setVersionedEducation(Education.copy(oldResumeEducation));
            return versionedEducation;
        }
        else
        {
            return Education.copy(versionToSwitch.getVersionedEducation());
        }
    }

    private Header getHeader(Resume resume, ResumeVersion versionToSwitch, boolean makeCopy) {
        if (!makeCopy) {
            Header oldResumeHeader = resume.getHeader();

            Header versionedHeader = versionToSwitch.getVersionedHeader();

            versionToSwitch.setVersionedHeader(Header.copy(oldResumeHeader));

            return versionedHeader;
        }
        else
        {
            return Header.copy(versionToSwitch.getVersionedHeader());

        }
    }


    private ResumeVersion createSnapshot(Resume resume, VersionCreationForm inclusionForm){
        ResumeVersion newVersion = new ResumeVersion();
        createVersionedHeader(resume.getHeader(),
                inclusionForm.includeHeader(), newVersion);
        createVersionedEducation(
                resume.getEducation(), inclusionForm.includeEducation(), newVersion
        );
        createVersionedExperiences(
                resume.getExperiences(), inclusionForm.includeExperience(), newVersion
        );
        String versionedName = inclusionForm.includeName() ? resume.getName() : null;
        newVersion.setVersionedName(versionedName);
        newVersion.setAssociatedResume(resume);
        resume.getVersions().add(newVersion);
        return newVersion;
    }


    private void createVersionedHeader(Header originalHeader, boolean shouldBeNull,
                                      ResumeVersion resumeVersion){
        if(shouldBeNull){
            return;
        }

        Header newHeader = Header.copy(originalHeader);
        resumeVersion.setVersionedHeader(newHeader);
    }


    private void createVersionedEducation(Education originalEducation, boolean shouldBeNull,
                                         ResumeVersion resumeVersion){
        if(shouldBeNull){
            return;
        }
        Education newEducation = Education.copy(originalEducation);
        resumeVersion.setVersionedEducation(newEducation);

    }

    private void createVersionedExperiences(List<Experience> originalExperiences,
                                           boolean shouldBeNull,
                                           ResumeVersion resumeVersion){
        if(shouldBeNull){
            return;
        }
        List<Experience> newExperiences = originalExperiences.stream().map(
                Experience::copy).toList();

        resumeVersion.setVersionedExperiences(newExperiences);
    }


    @Transactional
    public void deleteVersion(User user, UUID resumeId, UUID versionId){
        Resume deletingResume = getUtility.findByUserResumeId(user, resumeId);
        ResumeVersion versionToDelete = findByResumeAndVersionId(versionId, deletingResume);
        versionRepository.delete(versionToDelete);
        deletingResume.setVersionCount(deletingResume.getVersionCount() - 1);
        resumeRepository.save(deletingResume);
    }
}
