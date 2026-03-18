package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.resume_forms.ResumeCreationForm;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
@NamedEntityGraph(name = Resume.GRAPH_NAME,
    attributeNodes = {
        @NamedAttributeNode(value = "header"),
        @NamedAttributeNode(value = "education"),
        @NamedAttributeNode(value = "experiences"),
        @NamedAttributeNode(value = "projects"),
        @NamedAttributeNode(value = "user")
    })
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Indexed
public class Resume implements Serializable {

    public static final String GRAPH_NAME = "Resume.fullData";

    @Serial
    private static final long serialVersionUID = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    @GenericField(searchable = Searchable.YES)
    private UUID id;

    @Column(name = "name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    @FullTextField(searchable =  Searchable.YES)
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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
    orphanRemoval = true, mappedBy = "associatedResume")
    @OrderBy("createdDate DESC")
    private List<ResumeVersion> versions = new ArrayList<>();

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

    @GenericField(searchable = Searchable.YES, sortable = Sortable.YES)
    private Instant creationTime = Instant.now();

    @GenericField(sortable = Sortable.YES, searchable = Searchable.YES)
    private Instant lastModifiedTime = Instant.now();

    public Resume(@NonNull String resume_name, @NonNull User user){
        this.user = user;
        this.experiences = new ArrayList<>();
        this.education = new Education();
        this.header = new Header();
        this.name = resume_name;
    }

    public Resume(@NonNull Resume originalResume, @NonNull ResumeCreationForm creationForm){


        this.name = creationForm.newName();
        this.user = originalResume.getUser();
        // We are creating new objects here,
        // because we do not want them to be a reference to the original ones.
        this.education = Education.copy(originalResume.getEducation());
        this.header = Header.copy(originalResume.getHeader());
        this.experiences = originalResume.getExperiences().stream().map(
                Experience::copy
        ).peek(experience -> experience.setResume(this)).toList();
        this.projects = originalResume.getProjects().stream().map(
                Project::copy
        ).peek(project -> project.setResume(this)).toList();
        //Necessary in order for cascading to work properly
        this.user.getResumes().add(this);
        this.education.setResume(this);
        this.header.setResume(this);
        this.creationTime = Instant.now();
        // Technically, the LocalDataTime.now() call will be different from the one above,
        // and we want these dates to match initially
        this.lastModifiedTime = this.creationTime;

        if (creationForm.copyVersions())
        {
            for (ResumeVersion resumeVersion : originalResume.getVersions()) {
                this.versions.add(ResumeVersion.copy(resumeVersion));
            }

        }
    }

    public Resume(ResumeVersion version, String newName, User user)
    {
        this.user = user;
        this.name = newName;
        this.education = Education.copy(version.getVersionedEducation());
        this.header = Header.copy(version.getVersionedHeader());
        this.experiences = version.getVersionedExperiences().stream().map(
                Experience::copy
        ).peek(experience -> experience.setResume(this)).toList();
        this.projects = version.getVersionedProjects().stream().map(
                Project::copy
        ).peek(project -> project.setResume(this)).toList();

        this.user.getResumes().add(this);
        this.education.setResume(this);
        this.header.setResume(this);
        this.creationTime = Instant.now();
        // Technically, the LocalDataTime.now() call will be different from the one above,
        // and we want these dates to match initially
        this.lastModifiedTime = this.creationTime;

    }

}
