package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.responses.resume_responses.ResumeResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "resumes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_same_user_resume_name", columnNames = {"user_id", "name"})
}, indexes = {
        @Index(columnList = "header_id"),
        @Index(columnList = "education_id"),
        @Index(columnList = "experience_id"),
        @Index(columnList = "user_id"),
        @Index(columnList = "user_id, id")
})
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Resume extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false)
    @NonNull
    private String name;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resume")
    private ResumeHeader resumeHeader;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resume")
    private ResumeEducation resumeEducation;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    private List<ResumeExperience> resumeExperiences = new ArrayList<>();


    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    private List<ResumeProject> resumeProjects = new ArrayList<>();

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_user_id"))
    @JsonIgnore
    private User user;

    @Column(name = "version_count", nullable = false)
    private int versionCount = 0;

    public Resume(@NonNull String resume_name, @NonNull User user){
        this.user = user;
        this.resumeExperiences = new ArrayList<>();
        this.resumeEducation = new ResumeEducation();
        this.resumeHeader = new ResumeHeader();
        this.name = resume_name;
    }

    public Resume(@NonNull Resume originalResume, @NonNull String newName){


        this.name = newName;
        this.user = originalResume.getUser();
        // We are creating new objects here
        // because we do not want them to be a reference to the original ones.
        this.resumeEducation = ResumeEducation.copy(originalResume.getResumeEducation());
        this.resumeHeader = ResumeHeader.copy(originalResume.getResumeHeader());
        this.resumeExperiences = originalResume.getResumeExperiences().stream().map(
                resumeExperience -> new ResumeExperience(resumeExperience, this)
        ).collect(Collectors.toCollection(ArrayList::new));
        this.resumeProjects = originalResume.getResumeProjects().stream().map(
                ResumeProject::new
        ).peek(project -> project.setResume(this)).
                collect(Collectors.toCollection(ArrayList::new));
        //Necessary in order for cascading to work properly
        this.user.getResumes().add(this);
        this.resumeEducation.setResume(this);
        this.resumeHeader.setResume(this);
    }

    public ResumeResponse toResponse()
    {
        return new ResumeResponse(this.resumeHeader.toResponse(), this.resumeEducation.toResponse(),
                this.resumeExperiences.stream().map(ResumeExperience::toResponse).toList(),
                this.resumeProjects.stream().map(ResumeProject::toResponse).toList());
    }

}
