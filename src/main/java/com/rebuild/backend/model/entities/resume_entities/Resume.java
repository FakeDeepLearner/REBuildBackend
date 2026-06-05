package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.responses.resume_responses.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;
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

    @Column(name = "preview_url")
    private String previewUrl = null;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resume")
    private ResumeHeader resumeHeader = null;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resume")
    private ResumeEducation resumeEducation = null;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    private Set<ResumeExperience> resumeExperiences = new HashSet<>();


    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    private Set<ResumeProject> resumeProjects = new HashSet<>();

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
        this.resumeExperiences = new HashSet<>();
        this.resumeEducation = new ResumeEducation();
        this.resumeHeader = new ResumeHeader();
        this.name = resume_name;
        user.getResumes().add(this);
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
        ).collect(Collectors.toCollection(HashSet::new));
        this.resumeProjects = originalResume.getResumeProjects().stream().map(
                ResumeProject::new
        ).peek(project -> project.setResume(this)).
                collect(Collectors.toCollection(HashSet::new));
        //Necessary in order for cascading to work properly
        this.user.getResumes().add(this);
        this.resumeEducation.setResume(this);
        this.resumeHeader.setResume(this);
    }

    private List<ExperienceResponse> determineExperienceResponses(){

        if (this.resumeExperiences == null || this.resumeExperiences.isEmpty()){
            return Collections.emptyList();
        }

        List<ResumeExperience> experienceList = new ArrayList<>(this.getResumeExperiences());

        Comparator<ResumeExperience> resumeExperienceComparator =
                Comparator.comparing(ResumeExperience::getEndDate,
                        Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(ResumeExperience::getStartDate, Comparator.reverseOrder());

        experienceList.sort(resumeExperienceComparator);

        return experienceList.stream().map(ResumeExperience::toResponse).toList();



    }

    private List<ProjectResponse> determineProjectResponses(){
        if (this.resumeProjects == null || this.resumeProjects.isEmpty()){
            return Collections.emptyList();
        }
        List<ResumeProject> resumeProjectList = new ArrayList<>(this.getResumeProjects());

        Comparator<ResumeProject> projectComparator =
                Comparator.comparing(ResumeProject::getEndDate,
                                Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(ResumeProject::getStartDate, Comparator.reverseOrder());

        resumeProjectList.sort(projectComparator);
        return resumeProjectList.stream().map(ResumeProject::toResponse).toList();
    }

    public ResumeResponse toResponse()
    {
        HeaderResponse headerResponse = this.resumeHeader == null ? null : this.resumeHeader.toResponse();
        EducationResponse educationResponse = this.resumeEducation == null ? null : this.resumeEducation.toResponse();

        return new ResumeResponse(headerResponse, educationResponse,
                determineExperienceResponses(), determineProjectResponses());
    }

}
