package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

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
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    private List<PostResumeExperience> resumeExperiences;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "postResume")
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    private List<PostResumeProject> resumeProjects;

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
        List<ResumeExperience> originalResumeExperiences = originalResume.getResumeExperiences();
        List<ResumeProject> originalResumeProjects = originalResume.getResumeProjects();
        // We are creating new objects here
        // because we do not want them to be a reference to the original ones.
        this.resumeEducation = ResumeEducation.sensitiveCopy(originalResumeEducation, this);
        this.resumeHeader = ResumeHeader.sensitiveCopy(originalResumeHeader, this);
        this.resumeExperiences = originalResumeExperiences.stream().map(resumeExperience ->
                ResumeExperience.sensitiveCopy(resumeExperience, this)).toList();
        this.resumeProjects = originalResumeProjects.stream().map(resumeProject ->
                        ResumeProject.sensitiveCopy(resumeProject, this)).
                toList();
    }
}
