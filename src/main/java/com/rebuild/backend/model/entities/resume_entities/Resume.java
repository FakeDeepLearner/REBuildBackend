package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.resume_responses.ResumeResponse;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
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
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Resume{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String name;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resume")
    private Header header;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resume")
    private Education education;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    private List<Experience> experiences = new ArrayList<>();


    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    private List<Project> projects = new ArrayList<>();

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

    private Instant creationTime = Instant.now();

    private Instant lastModifiedTime = Instant.now();

    public Resume(@NonNull String resume_name, @NonNull User user){
        this.user = user;
        this.experiences = new ArrayList<>();
        this.education = new Education();
        this.header = new Header();
        this.name = resume_name;
    }

    public Resume(@NonNull Resume originalResume, @NonNull String newName){


        this.name = newName;
        this.user = originalResume.getUser();
        // We are creating new objects here,
        // because we do not want them to be a reference to the original ones.
        this.education = Education.copy(originalResume.getEducation());
        this.header = Header.copy(originalResume.getHeader());
        this.experiences = originalResume.getExperiences().stream().map(
                Experience::copy
        ).peek(experience -> experience.setResume(this)).
                collect(Collectors.toCollection(ArrayList::new));
        this.projects = originalResume.getProjects().stream().map(
                Project::copy
        ).peek(project -> project.setResume(this)).
                collect(Collectors.toCollection(ArrayList::new));
        //Necessary in order for cascading to work properly
        this.user.getResumes().add(this);
        this.education.setResume(this);
        this.header.setResume(this);
        this.creationTime = Instant.now();
        // Technically, the LocalDataTime.now() call will be different from the one above,
        // and we want these dates to match initially
        this.lastModifiedTime = this.creationTime;
    }

    public ResumeResponse toResponse()
    {
        return new ResumeResponse(this.header.toResponse(), this.education.toResponse(),
                this.experiences.stream().map(Experience::toResponse).toList(),
                this.projects.stream().map(Project::toResponse).toList());
    }

}
