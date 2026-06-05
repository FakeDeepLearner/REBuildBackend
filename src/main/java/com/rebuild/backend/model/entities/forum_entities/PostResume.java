package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.responses.resume_responses.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "post_resumes", indexes = {
        @Index(columnList = "header_id"),
        @Index(columnList = "education_id"),
        @Index(columnList = "experience_id"),
        @Index(columnList = "user_id")
})
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
//@RequiredArgsConstructor
public class PostResume extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true,
    mappedBy = "postResume")
    private PostResumeHeader resumeHeader;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true,
    mappedBy = "postResume")
    private PostResumeEducation resumeEducation;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "postResume")
    private Set<PostResumeExperience> resumeExperiences;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "postResume")
    private Set<PostResumeProject> resumeProjects;

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_post_id"))
    @JsonIgnore
    private ForumPost associatedPost;

    public PostResume(@NonNull Resume originalResume){
        ResumeEducation originalResumeEducation = originalResume.getResumeEducation();
        ResumeHeader originalResumeHeader = originalResume.getResumeHeader();
        Set<ResumeExperience> originalResumeExperiences = originalResume.getResumeExperiences();
        Set<ResumeProject> originalResumeProjects = originalResume.getResumeProjects();
        // We are creating new objects here
        // because we do not want them to be a reference to the original ones.
        this.resumeEducation = ResumeEducation.copy(originalResumeEducation, this);
        this.resumeHeader = ResumeHeader.copy(originalResumeHeader, this);
        this.resumeExperiences = originalResumeExperiences.stream().map(resumeExperience ->
                ResumeExperience.copy(resumeExperience, this)).collect(Collectors.toSet());
        this.resumeProjects = originalResumeProjects.stream().map(resumeProject ->
                        ResumeProject.copy(resumeProject, this)).collect(Collectors.toSet());
    }

    private List<ExperienceResponse> determineExperienceResponses(){

        if (this.resumeExperiences == null || this.resumeExperiences.isEmpty()){
            return Collections.emptyList();
        }

        List<PostResumeExperience> experienceList = new ArrayList<>(this.getResumeExperiences());

        Comparator<PostResumeExperience> resumeExperienceComparator =
                Comparator.comparing(PostResumeExperience::getEndDate,
                                Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(PostResumeExperience::getStartDate, Comparator.reverseOrder());

        experienceList.sort(resumeExperienceComparator);

        return experienceList.stream().map(PostResumeExperience::toResponse).toList();



    }

    private List<ProjectResponse> determineProjectResponses(){
        if (this.resumeProjects == null || this.resumeProjects.isEmpty()){
            return Collections.emptyList();
        }
        List<PostResumeProject> resumeProjectList = new ArrayList<>(this.getResumeProjects());

        Comparator<PostResumeProject> projectComparator =
                Comparator.comparing(PostResumeProject::getEndDate,
                                Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(PostResumeProject::getStartDate, Comparator.reverseOrder());

        resumeProjectList.sort(projectComparator);
        return resumeProjectList.stream().map(PostResumeProject::toResponse).toList();
    }


    public ResumeResponse toResponse()
    {
        HeaderResponse headerResponse = this.resumeHeader == null ? null : this.resumeHeader.toResponse();
        EducationResponse educationResponse = this.resumeEducation == null ? null : this.resumeEducation.toResponse();

        return new ResumeResponse(headerResponse, educationResponse,
                determineExperienceResponses(), determineProjectResponses());
    }
}
