package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.*;
import com.rebuild.backend.model.exceptions.BelongingException;
import com.rebuild.backend.model.forms.resume_forms.VersionCreationForm;
import com.rebuild.backend.model.forms.resume_forms.VersionSwitchPreferencesForm;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeVersionRepository;
import com.rebuild.backend.utils.ResumeObtainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ResumeVersioningService {

    private final ResumeRepository resumeRepository;

    private final ResumeObtainer getUtility;

    private final ResumeVersionRepository versionRepository;

    public ResumeVersioningService(ResumeRepository resumeRepository, ResumeObtainer getUtility,
                                   ResumeVersionRepository versionRepository) {
        this.resumeRepository = resumeRepository;
        this.getUtility = getUtility;
        this.versionRepository = versionRepository;
    }

    private ResumeVersion findVersionByParameters(User user, UUID resumeId, UUID versionId)
    {
        return versionRepository.findByIdAndAssociatedResume_IdAndAssociatedResume_User(versionId, resumeId, user).orElseThrow(
                () -> new BelongingException("This version either does not exist or does not belong to this resume")
        );
    }


    @Transactional
    public ResumeVersion snapshotCurrentData(User user, UUID resumeId, VersionCreationForm inclusionForm){
        Resume copiedResume = getUtility.findByUserAndIdWithAllInfo(user, resumeId);
        ResumeVersion newVersion = createSnapshot(copiedResume, inclusionForm);
        copiedResume.setVersionCount(copiedResume.getVersionCount() + 1);
        return versionRepository.save(newVersion);
    }

    @Transactional
    public Resume switchToAnotherVersion(User user,
                                         UUID resumeId, UUID versionId,
                                         VersionSwitchPreferencesForm versionSwitchPreferencesForm){
        ResumeVersion versionToSwitch = findVersionByParameters(user, resumeId, versionId);
        Resume switchingResume = getUtility.findByUserAndIdWithAllInfo(user, resumeId);

        handleVersionSwitch(versionToSwitch.getAssociatedResume(), versionToSwitch, versionSwitchPreferencesForm);
        versionRepository.save(versionToSwitch);
        return resumeRepository.save(switchingResume);

    }

    private void handleVersionSwitch(Resume resume, ResumeVersion versionToSwitch,
                                     VersionSwitchPreferencesForm preferencesForm){
        if(versionToSwitch.getVersionedName() != null && preferencesForm.includeName()){
            String tempName = resume.getName();
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

        if(versionToSwitch.getVersionedExperiences() != null && !preferencesForm.experienceIds().isEmpty()){
            List<Experience> newExperiences = getExperiences(resume, versionToSwitch,
                    preferencesForm.makeExperienceCopies(), preferencesForm.experienceIds());
            resume.setExperiences(newExperiences);
        }

        if(versionToSwitch.getVersionedEducation() != null && !preferencesForm.projectIds().isEmpty()){
            List<Project> newProjects = getProjects(resume, versionToSwitch,
                    preferencesForm.makeProjectCopies(), preferencesForm.projectIds());
            resume.setProjects(newProjects);
        }

    }


    private List<Project> getProjects(Resume resume, ResumeVersion versionToSwitch,
                                      boolean makeCopies, List<UUID> identifiersToSelect){
        if (!makeCopies) {
            List<Project> oldResumeProjects = resume.getProjects().stream()
                    .peek(project -> {
                        project.setResume(null);
                        project.setVersion(versionToSwitch);
                    }).toList();

            List<Project> versionedProjects = versionToSwitch.getVersionedProjects().stream().
                    filter(project -> identifiersToSelect.contains(project.getId())).
                    peek(project -> {
                        project.setVersion(null);
                        project.setResume(resume);
                    }).toList();

            versionToSwitch.setVersionedProjects(oldResumeProjects);
            return versionedProjects;
        }
        else
        {
            List<Project> versionedProjects = versionToSwitch.getVersionedProjects();

            return versionedProjects.stream().
                    filter(project -> identifiersToSelect.contains(project.getId())).
                    map(Project::copy)
                    .toList();
        }
    }

    private List<Experience> getExperiences(Resume resume, ResumeVersion versionToSwitch,
                                                   boolean makeCopies, List<UUID> identifiersToSelect){

        if (!makeCopies) {
            List<Experience> oldResumeExperiences = resume.getExperiences().stream()
                    .peek(experience -> {
                        experience.setResume(null);
                        experience.setVersion(versionToSwitch);
                    }).toList();

            List<Experience> versionedExperiences = versionToSwitch.getVersionedExperiences().stream().
                    filter(experience -> identifiersToSelect.contains(experience.getId())).
                    peek(experience -> {
                        experience.setVersion(null);
                        experience.setResume(resume);
                    }).toList();

            versionToSwitch.setVersionedExperiences(oldResumeExperiences);
            return versionedExperiences;
        }
        else
        {
            List<Experience> versionedExperiences = versionToSwitch.getVersionedExperiences();

            return versionedExperiences.stream().
                    filter(experience -> identifiersToSelect.contains(experience.getId())).
                    map(Experience::copy)
                    .toList();
        }
    }

    private Education getEducation(Resume resume, ResumeVersion versionToSwitch, boolean makeCopy) {
        // If we don't want to make a copy of the versioned education, we simply swap the version with the resume
        // and the ResumeVersion. The exact same logic holds for the methods below as well.
        if (!makeCopy) {

            Education oldResumeEducation = resume.getEducation();
            oldResumeEducation.setVersion(versionToSwitch);
            oldResumeEducation.setResume(null);

            Education versionedEducation = versionToSwitch.getVersionedEducation();
            versionedEducation.setVersion(null);
            versionedEducation.setResume(resume);

            /*
             * We set the versioned attribute into a copy of the old one rather than the original one,
             * because we want to avoid a potential bug where the identifier of the versioned education and
             * the new one on the resume will be the same, since updating the resume now takes inplace instead
             * of a simple attribute set. Since the copying code creates a new attribute, it avoids those bugs.
             * The exact same logic follows for the getHeader and the getExperiences functions as well.
             * */
            versionToSwitch.setVersionedEducation(oldResumeEducation);
            return versionedEducation;
        }
        //If we instead want to make copies, we just make a copy of the versioned education and return it.
        else
        {
            return Education.copy(versionToSwitch.getVersionedEducation());
        }
    }

    private Header getHeader(Resume resume, ResumeVersion versionToSwitch, boolean makeCopy) {
        if (!makeCopy) {
            Header oldResumeHeader = resume.getHeader();
            oldResumeHeader.setVersion(versionToSwitch);
            oldResumeHeader.setResume(null);

            Header versionedHeader = versionToSwitch.getVersionedHeader();
            versionedHeader.setVersion(null);
            versionedHeader.setResume(resume);

            versionToSwitch.setVersionedHeader(oldResumeHeader);

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
                resume.getEducation(), inclusionForm.includeEducation(), newVersion);
        createVersionedExperiences(
                resume.getExperiences(), inclusionForm.includeExperience(), newVersion);
        createVersionedProjects(resume.getProjects(), inclusionForm.includeProjects(), newVersion);
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
        newHeader.setVersion(resumeVersion);
        resumeVersion.setVersionedHeader(newHeader);
    }


    private void createVersionedEducation(Education originalEducation, boolean shouldBeNull,
                                         ResumeVersion resumeVersion){
        if(shouldBeNull){
            return;
        }
        Education newEducation = Education.copy(originalEducation);
        newEducation.setVersion(resumeVersion);
        resumeVersion.setVersionedEducation(newEducation);

    }

    private void createVersionedExperiences(List<Experience> originalExperiences,
                                           boolean shouldBeNull,
                                           ResumeVersion resumeVersion){
        if(shouldBeNull){
            return;
        }
        List<Experience> newExperiences = originalExperiences.stream().map(
                Experience::copy).peek(experience -> experience.setVersion(resumeVersion))
                .toList();

        resumeVersion.setVersionedExperiences(newExperiences);
    }

    private void createVersionedProjects(List<Project> originalProjects, boolean shouldBeNull,
                                         ResumeVersion resumeVersion){
        if(shouldBeNull){
            return;
        }

        List<Project> newProjects = originalProjects.stream().map(
                Project::copy).peek(project -> project.setVersion(resumeVersion))
                .toList();
        resumeVersion.setVersionedProjects(newProjects);
    }


    @Transactional
    public void deleteVersion(User user, UUID resumeId, UUID versionId){
        ResumeVersion versionToDelete = findVersionByParameters(user, resumeId, versionId);
        Resume deletingResume = versionToDelete.getAssociatedResume();
        versionRepository.delete(versionToDelete);
        deletingResume.setVersionCount(deletingResume.getVersionCount() - 1);
        resumeRepository.save(deletingResume);
    }
}
